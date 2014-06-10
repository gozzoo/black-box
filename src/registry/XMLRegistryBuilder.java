package registry;

import java.io.File;
import java.util.*;
import java.util.regex.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;

@SuppressWarnings("rawtypes")
public class XMLRegistryBuilder {
    private Pattern varPattern = Pattern.compile("\\$\\{(.*)\\}");  // ${var}
    private Registry registry;
    private Map<String, Configurator> configuratorMap = new HashMap<>();
    private Map<String, String> notActiveConfiguirators = new HashMap<>();
    
    public XMLRegistryBuilder(String path, Properties p) throws Exception {
        registry = new Registry();    
        registry.registerInstance(registry);
        
        addProperties(p);
        
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(new File(path));
        Node root = doc.getDocumentElement();
        NodeList childNodes = root.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node n = childNodes.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE)
                configElement((Element)n);
        }
    }
  
    private void addProperties(Properties p) throws RegistryException {
        Enumeration<?> keys = p.propertyNames();
        while (keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            String value = p.getProperty(key);
            registry.registerInstance(key, value);
        }
    }
    
    private void configElement(Element e) throws Exception {
        String tag = e.getNodeName();
        
        switch (tag) {
        case "property": 
            configProperty(e);
            break;
        case "instance":
            configInstance(e);
            break;
        case "configurator":
            configConfigurator(e);
            break;
        case "init":
            configInit(e);
            break;
        default:
            configWithConfigurator(tag, e);
        }
    }
    
    private void configWithConfigurator(String tag, Element e) throws Exception {
        Configurator configurator = configuratorMap.get(tag); 
        if (configurator == null) 
            configurator = activateConfigurator(tag);
        if (configurator != null) 
            configurator.configure(e);
        else
            throw new RegistryException("configuration element can not be processed: " + tag);        
    }
    
    private Configurator activateConfigurator(String tag) throws Exception {
        String configuratorName = notActiveConfiguirators.get(tag);
        if (configuratorName != null) {
            addConfigurator(tag, configuratorName);
            return configuratorMap.get(tag); 
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private void addConfigurator(String tag, String configuratorName) throws Exception {
        Class c = Class.forName(configuratorName);
        Configurator configurator = registry.registerProvider(c);
        configuratorMap.put(tag, configurator);
    }
    
    @SuppressWarnings("unchecked")
    public void configInit(Element e) throws Exception {
        String initClass = e.getAttribute("class");
          Class bc = Class.forName(initClass);
          Initializer init = registry.registerProvider(bc);
          //Initializer init = (Initializer)bc.newInstance();
          //registry.decorateInstance(init);
          init.init();
    }
    
    private String replaceParams(String s) throws Exception {
        Matcher m = varPattern.matcher(s);
        if (m.find()) {
            String var = m.group(1);
            Object o = registry.getInstance(var);
            if (o != null) {
                StringBuffer sb = new StringBuffer(s.subSequence(0, m.start()));
                sb.append(o.toString());
                sb.append(s.substring(m.end()));
                return sb.toString();
            }
        }
        return s;
    }
    
    public void configProperty(Element e) throws Exception {    
        String name = e.getAttribute("name");
        String value = e.getAttribute("value");
        value = replaceParams(value);
        registry.registerInstance(name, value);
    }
    
    public void configInstance(Element e) throws Exception {
        String name = e.getAttribute("name");
        String className = e.getAttribute("class");
        if (className != null && !className.equals("")) {
            registerImplementation(name, className);
        } else {
            String builder = e.getAttribute("builder");
            if (builder != null && !builder.equals(""))
                buildInstance(name, builder);
            else
                throw new RegistryException("no implementation for key" + name);
        }
    }
    
    public void configConfigurator(Element e) throws Exception {
        String tag = e.getAttribute("tag");
        String className = e.getAttribute("class");
        notActiveConfiguirators.put(tag, className);
    }
    
    private void registerImplementation(String name, String className) throws Exception {
        Class<?> c = Class.forName(className);
        if (name == null)
            registry.registerImplementation(c);
        else
            registry.registerImplementation(name, c);
    }

    private void buildInstance(String name, String builder) throws Exception {
        Class<?> ibc = Class.forName(builder);
        RegistryInstanceBuilder rib = (RegistryInstanceBuilder)registry.getInstance(ibc);
        Object o = rib.createInstance();
        if (name == null) 
            registry.registerInstance(o);
        else
            registry.registerInstance(name, o);
    }

    public Registry getRegistry() {
        return registry;
    }
}