package registry;

import java.util.Properties;


public class RegistryBuilder {
    private static Registry registry;
    
    public static void build(String configFile, Properties p) throws Exception {
        XMLRegistryBuilder ir = new XMLRegistryBuilder(configFile, p);
        registry  = ir.getRegistry();
    }
    
    public static Registry getRegistry() {
        if (registry == null)
            registry = new Registry();
        return registry;
    }
}
