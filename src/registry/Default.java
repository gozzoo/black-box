package registry;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention((RetentionPolicy.RUNTIME))
public @interface Default {
    Class<?> value();
}

