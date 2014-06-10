import static org.junit.Assert.*;

import javax.inject.Inject;
import org.junit.Test;
import registry.Registry;


public class StaticTest {
    @Test
    public void decorateStatic() {
        Registry.decorateStatic(G.class);
        assertNotNull(G.aField);
    }
    
    @Test
    public void ecorateSelfStatic() {
        A aField = H.aField;
        assertNotNull(aField);
    }
}

class G {
    @Inject static A aField;
}

class H {
    @Inject static A aField;
    
    static { Registry.decorateStatic(); }
}