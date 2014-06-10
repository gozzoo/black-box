package registry;

import javax.inject.Inject;

import org.w3c.dom.*;

public class PropertyConfigurator implements Configurator {
	@Inject Registry registry;
	
	public void configure(Element e) throws Exception {	
        String name = e.getAttribute("name");
        String value = e.getAttribute("value");
        registry.registerInstance(name, value);
    }
}
