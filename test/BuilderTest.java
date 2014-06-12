import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Test;

import registry.Registry;
import registry.RegistryBuilder;


public class BuilderTest {
    Registry r;
    
    public BuilderTest() throws Exception {
        Properties p = new Properties();
        RegistryBuilder.build("./test/registry.xml", p);
        r = RegistryBuilder.getRegistry();
    }
    
    @Test
    public void injectedProperty() throws Exception {
        FS inst = r.getInstance(FS.class);
        
        assertNotNull(inst);
        assertNotNull(inst.email);
        assertEquals(inst.email, "abc@yahoo.com");
    }
}
