package pt.isel.autorouter;

import org.cojen.maker.ClassMaker;

import java.lang.reflect.Method;
import java.util.stream.Stream;

public class AutoRouterDynamic {
    public static Stream<ArHttpRoute> autorouterDynamic(Object controller) {
        return Stream.empty();
    }

    public static ClassMaker buildHandler(Class<?> routerClass, Method fun) {

        throw new UnsupportedOperationException();
    }
}
