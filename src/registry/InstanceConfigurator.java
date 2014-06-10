package registry;

import javax.inject.Inject;

import org.w3c.dom.*;


public class InstanceConfigurator implements Configurator {
	@Inject Registry registry;
	
	public void configure(Element e) throws Exception {
        String name = e.getAttribute("name");
        String className = e.getAttribute("class");
        if (className != null) {
        	registerImplementation(name, className);
        } else {
        	String builder = e.getAttribute("builder");
        	if (builder != null)
        		buildInstance(name, builder);
        	else
        		throw new RegistryException("no implementation for key" + name);
        }
    }
    
    private void registerImplementation(String name, String className) throws Exception {
    	Class<?> c = Class.forName(className);
        if (name == null)
        	registry.registerImplementation(c);
        else
        	registry.registerImplementation(name, c);
    }

    private void buildInstance(String name, String builder) throws Exception {
    	Class<?> bc = Class.forName(builder);
    	RegistryInstanceBuilder<?> ib = (RegistryInstanceBuilder<?>)bc.newInstance();
    	registry.decorateInstance(ib);
    	Object o = ib.createInstance(); 
    	registry.registerInstance(name, o);
    }
}