import static org.junit.Assert.*;

import javax.inject.*;
import org.junit.*;
import registry.*;

public class MainTest {
    Registry r;

    @Before
    public void init() {
        r = new Registry();        
    }
    
    @Test
    public void autoInject() throws Exception {
        B inst = r.getInstance(B.class);
        
        assertNotNull(inst);
        assertNotNull(inst.aField);
    }
    
    @Test
    public void injectImplementation() throws Exception {
        r.registerImplementation(A.class);
        B inst = r.getInstance(B.class);
        
        assertNotNull(inst);
        assertNotNull(inst.aField);
    }
    
    @Test
    public void injectInstance() throws Exception {
        A a = new A();
        r.registerInstance(a);
        B inst = r.getInstance(B.class);
        
        assertNotNull(inst);
        assertSame(a, inst.aField);
    }
    
    @Test
    public void injectNamedInstance() throws Exception {
        A a = new A();
        r.registerInstance("iname", a);
        F inst = r.getInstance(F.class);
        
        assertNotNull(inst);
        assertSame(a, inst.iname);
    }
    
    @Test
    public void injectStringProperty() throws Exception {
        String email = "name@yahoo.com";
        r.registerInstance("email", email);
        FS inst = r.getInstance(FS.class);
        
        assertNotNull(inst);
        assertNotNull(inst.email);
        assertSame(inst.email, email);
    }
    
    @Test
    public void constructorInject() throws Exception {
        E inst = r.getInstance(E.class);
        
        assertNotNull(inst);
        assertNotNull(inst.aField);
    }
    
    @Test
    public void injectInterface() throws Exception {
        r.registerImplementation(AI.class, A.class);
        B inst = r.getInstance(B.class);
        
        assertNotNull(inst);
        assertNotNull(inst.aField);
    }
    
    @Test
    public void injectDefaultImplementationForInterface() throws Exception {
        DI inst = r.getInstance(DI.class);
        assertNotNull(inst);
    }
    
    @Test(expected=RegistryException.class)
    public void injectMissingDefaultImplementationForInterface() throws Exception {
        AI inst = r.getInstance(AI.class);
        assertNull(inst);
    }
    
    @Test
    public void decorateInstance() throws Exception {
        C ci = new C();
        r.decorateInstance(ci);
        
        assertNotNull(ci.bField);
        assertNotNull(ci.bField.aField);
    }
}

interface AI { }

class A implements AI { }

class B {
    @Inject A aField;
}

class C {
    @Inject B bField;
}

@Default(D.class)
interface DI { }

class D implements DI { }

class E {
    A aField;
    
    @Inject 
    public E(A afield) {
        this.aField = afield;
    }
}

class F {
    @Inject @Named A iname;
}

class FS {
    @Inject @Named String email;
}