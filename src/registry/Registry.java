package registry;

import java.lang.reflect.*;
import java.util.*;

import javax.inject.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Registry {
    private Registry parent;
    private Map<String, Object> namedInstanceMap = new HashMap<>();
    private Map<Class, Object> classMap = new HashMap<>();
    
    public Registry() {}
    
    public Registry(Registry parent) {
        this.parent = parent;
    }
    
    public Object getInstance(String key) throws Exception {
        Object instance = namedInstanceMap.get(key);
        if (instance != null)
            return instance;
        if (parent != null)
            return parent.getInstance(key);
        return null;
    }
    
    private <T> T getExistingInstance(Class<T> c) throws Exception {
        T o = (T)classMap.get(c);
        if (o != null) 
            return o;
        return (parent == null) ? null :
            parent.getExistingInstance(c);
    }
    
    public <T> T getInstance(Class<T> c) throws Exception {
        T o = getExistingInstance(c);
        if (o == null)            
               o = createInstance(c);
        return o;
    }
    
    public static void decorateStatic() {
        Registry registry = RegistryBuilder.getRegistry();
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        try {
            String className = stackTrace[2].getClassName();
            Class c = Class.forName(className);
            registry.decorateInstance(null, c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void decorateStatic(Class c) {
        Registry registry = RegistryBuilder.getRegistry();
        try {
            registry.decorateInstance(null, c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void decorateInstance(Object o) throws Exception {
        decorateInstance(o, o.getClass());
    }

    private void decorateInstance(Object o, Class c) throws Exception {
        for (Field f : c.getDeclaredFields()) 
            if (f.getAnnotation(Inject.class) != null)
                injectFieldValue(o, f);
        
        c = c.getSuperclass();
        if (c != null)
            decorateInstance(o, c);
    }
    
    private void injectFieldValue(Object o, Field f) throws Exception {
        String name = getResName(f);        
        Object value = (name != null) ? 
                getInstance(name) :
                getInstance(f.getType());

        if (value == null) {
            String msg = o.getClass().getName() + " has unsatisfied dependency: " +
                            (name.equals("") ?
                                f.getType() + " " + f.getName() : 
                                " key " + name);
            throw new RegistryException(msg);
        } 
        if (!f.getType().isAssignableFrom(value.getClass())) {
            String msg = o.getClass().getName() + " has unsatisfied dependency: class " + 
                    f.getType() + " where class " + value.getClass() + " was found";
            throw new RegistryException(msg);
        }
        
        f.setAccessible(true);
        f.set(o, value);
    }

    private static String getResName(Field f) {
        Named named = f.getAnnotation(Named.class);
        if (named == null) 
            return null;

        String name = named.value();
           if (name.equals(""))
               name = f.getName();
        return name;
    }
    
    private Object[] getConstructorArguments(Class[] params) throws Exception {
        Object[] result = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            result[i] = getInstance(params[i]);
        }
        return result;
    }
    
    private <T> T createInstance(Class<T> c) throws Exception {
        if (c.isInterface()) {
            Default da = c.getAnnotation(Default.class);
            if (da == null)
                throw new RegistryException("No default implementation for interface: " + c.getName());
            c = (Class<T>)da.value();
        }
        T o = (T)newInstance(c);      
        decorateInstance(o, c);
        if (o instanceof Initializer) {
            Initializer i = (Initializer)o;
            i.init();
        }
        if (isSingleton(c))
            classMap.put(c, o);
        return o;
    }
    
    private static boolean isSingleton(Class c) {
        return c.getAnnotation(Singleton.class) != null;
    }

    private Object newInstance(Class<?> c) throws Exception {
        for (Constructor<?> con : c.getConstructors()) {
            if (con.getAnnotation(Inject.class) != null) {
                Class[] params = con.getParameterTypes();
                Object[] args = getConstructorArguments(params);
                con.setAccessible(true);
                return con.newInstance(args);
            }
        }
        Constructor<?> defaultCon = c.getDeclaredConstructor();
        defaultCon.setAccessible(true);
        return defaultCon.newInstance();
    }
    
    public void registerImplementation(Class c) throws RegistryException {
        if (classMap.containsKey(c))
            throw new RegistryException("Key class " + c.getName() + " duplicated");
        classMap.put(c, null);
    }
    
    public void registerImplementation(String key, Class c) throws RegistryException {
        if (namedInstanceMap.containsKey(key))
            throw new RegistryException("Key " + key + " duplicated");
        
        try {
            Object instance = newInstance(c);
            namedInstanceMap.put(key, instance);        
        } catch (Exception e) {
            throw new RegistryException(e.getMessage());
        }
    }
    
    public void registerInstance(String key, Object instance) throws RegistryException {
        if (key == null)
            throw new RegistryException("null key not allowed");
        
        if (namedInstanceMap.containsKey(key))
            throw new RegistryException("Key " + key + " duplicated");
        
        namedInstanceMap.put(key, instance);
    }    

    public void registerImplementation(Class c, Class subClass) throws Exception {
        if (c == null)
            throw new RegistryException("null key not allowed");
        
        if (classMap.containsKey(c))
            throw new RegistryException("Key class " + c.getName() + " duplicated");
        
        Object o = getInstance(subClass);
        classMap.put(c, o);
    }    

    public void registerInstance(Class c, Object instance) throws RegistryException {
        if (classMap.containsKey(c))
            throw new RegistryException("Key class " + c.getName() + " duplicated");
        
        classMap.put(c, instance);
    }
    
    public void registerInstance(Object instance) throws RegistryException {
        Class c = instance.getClass();
        if (classMap.containsKey(c))
            throw new RegistryException("Key class " + c.getName() + " duplicated");
        
        classMap.put(c, instance);
    } 
    
    public void deregisterInstance(Object instance) throws RegistryException {
        classMap.remove(instance.getClass());
    }  
    
    public void deregisterInstance(Class c) throws RegistryException {
        classMap.remove(c);
    } 
    
    public void deregisterImplementation(Class c) throws RegistryException {
        classMap.remove(c);
    } 
    
    public void removeImplementation(Class c) {
        classMap.remove(c);
    }
    
    public void removeInstance(String key) {
        namedInstanceMap.remove(key);
    } 
    
    public <T> T registerProvider(Class<T> c) throws Exception {
        T instance = getInstance(c);
        for (Field f : c.getDeclaredFields()) {
            if (!f.isAnnotationPresent(Provides.class))
                continue;
            Named named = f.getAnnotation(Named.class);
            f.setAccessible(true);
            Object fieldValue = f.get(instance);
            if (named != null) {
                String name = named.value();
                if (name.equals(""))
                    name = f.getName();
                   registerInstance(name, fieldValue);
            } else 
                registerInstance(instance);
        }
        return instance;
    }
}
