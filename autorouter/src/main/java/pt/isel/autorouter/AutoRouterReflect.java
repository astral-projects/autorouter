package pt.isel.autorouter;

import pt.isel.autorouter.annotations.ArBody;
import pt.isel.autorouter.annotations.ArQuery;
import pt.isel.autorouter.annotations.ArRoute;
import pt.isel.autorouter.annotations.AutoRouter;
import pt.isel.autorouter.exceptions.ArTypeAnnotationNotFoundException;
import pt.isel.autorouter.getters.BodyArgsGetter;
import pt.isel.autorouter.getters.Getter;
import pt.isel.autorouter.getters.QueryArgsGetter;
import pt.isel.autorouter.getters.RouteArgsGetter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Stream;

public class AutoRouterReflect {

    public static Stream<ArHttpRoute> autorouterReflect(Object controller) {
        // Filter methods that have autoroute annotation and have an <Optional> return type
        Stream<Method> methods = Arrays
                .stream(controller.getClass().getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(AutoRouter.class)
                        && m.getReturnType() == Optional.class);
        // For each method, create an ArHttpRoute instance
        return methods.map(m -> createArHttpRoute(controller, m));
    }
    
    private final static Map<Method, Getter[]> methodParametersMap = new HashMap<>();

    private static ArHttpRoute createArHttpRoute(Object target, Method m) {
        // Get values for ArHttpRoute parameters:
        String functionName = m.getName();
        ArVerb method = m.getAnnotation(AutoRouter.class).method();
        String path = m.getAnnotation(AutoRouter.class).value();
        // Implement functional interface only method
        ArHttpHandler handler = (routeArgs, queryArgs, bodyArgs) -> {
            // Create a list to store retrieved values of annotated parameter with Ar type annotation
            List<Object> args = new ArrayList<>(getMethodParameterValues(m, routeArgs, queryArgs, bodyArgs));
            // Added retrieved value to the array to be sent to the current method
            try {
                // Args needs to be converted to Object[]
                return (Optional<?>) m.invoke(target, args.toArray());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };
        return new ArHttpRoute(functionName, method, path, handler);
    }

    private static List<Object> getMethodParameterValues(
            Method m,
            Map<String, String> routeArgs,
            Map<String, String> queryArgs,
            Map<String, String> bodyArgs
    ) {
        List<Object> args = new ArrayList<>();
        Getter[] parameters = loadMethodParameters(m);
        // For each parameter of the method
        for (Getter getter : parameters) {
            args.add(getter.getArgValue(routeArgs, queryArgs, bodyArgs));
        }
        return args;
    }

    private static Getter[] loadMethodParameters(Method m) {
        return methodParametersMap.computeIfAbsent(m, k -> {
            List<Getter> getters = new ArrayList<>();
            for (Parameter param : m.getParameters()) {
                if (param.isAnnotationPresent(ArRoute.class)) {
                    getters.add(new RouteArgsGetter(param));
                } else if (param.isAnnotationPresent(ArQuery.class)) {
                    getters.add(new QueryArgsGetter(param));
                } else if (param.isAnnotationPresent(ArBody.class)) {
                    getters.add(new BodyArgsGetter(param));
                } else {
                    try {
                        throw new ArTypeAnnotationNotFoundException("param");
                    } catch (ArTypeAnnotationNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            // Return an array of getters, one for each method parameter
            return getters.toArray(new Getter[0]);
        });
    }
}