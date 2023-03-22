package pt.isel.autorouter.annotations;

import pt.isel.autorouter.ArVerb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static pt.isel.autorouter.ArVerb.GET;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoRouter {
    String value();
    ArVerb method() default GET;
}
