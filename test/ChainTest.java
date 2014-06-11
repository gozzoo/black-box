import static org.junit.Assert.*;

import org.junit.Test;

import registry.Registry;


public class ChainTest {
    @Test
    public void injectInstanceFromParrentRegistry() throws Exception {
        Registry pr = new Registry(); 
        A a = new A();
        pr.registerInstance(a);

        Registry r = new Registry(pr);
        B inst = r.getInstance(B.class);
        
        assertNotNull(inst);
        assertSame(a, inst.aField);
    }
    
    @Test
    public void shadowingInstanceFromParrentRegistry() throws Exception {
        Registry pr = new Registry(); 
        A a1 = new A();
        pr.registerInstance(a1);

        Registry r = new Registry(pr);
        A a2 = new A();
        r.registerInstance(a2);

        B inst = r.getInstance(B.class);
        
        assertNotNull(inst);
        assertSame(a2, inst.aField);
    }
}
