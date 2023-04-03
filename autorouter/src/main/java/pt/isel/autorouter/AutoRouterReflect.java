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

    private static ArHttpRoute createArHttpRoute(Object target, Method m) {
        // Get values for ArHttpRoute parameters:
        String functionName = m.getName();
        ArVerb method = m.getAnnotation(AutoRouter.class).method();
        String path = m.getAnnotation(AutoRouter.class).value();
        // Implement functional interface only method
        ArHttpHandler handler = (routeArgs, queryArgs, bodyArgs) -> {
            // Create a list to store retrieved values of annotated parameter with Ar type annotation
            List<Object> args;
            try {
                args = new ArrayList<>(getMethodArAnnotatedParameterValues(m, routeArgs, queryArgs, bodyArgs));
            } catch (ArTypeAnnotationNotFoundException e) {
                throw new RuntimeException(e);
            }
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

    private static List<Object> getMethodArAnnotatedParameterValues(
            Method m,
            Map<String, String> routeArgs,
            Map<String, String> queryArgs,
            Map<String, String> bodyArgs
    ) throws ArTypeAnnotationNotFoundException {
        List<Object> args = new ArrayList<>();
        // For each parameter of the method
        for (Parameter param : m.getParameters()) {
            args.add(getValue(param, routeArgs, queryArgs, bodyArgs));
        }
        return args;
    }

    private static Object getValue(Parameter param, Map<String, String> routeArgs, Map<String, String> queryArgs, Map<String, String> bodyArgs) throws ArTypeAnnotationNotFoundException {
        Getter getter;
        if (param.isAnnotationPresent(ArRoute.class)) {
            getter = loadRouteArgsGetters(param);
        } else if (param.isAnnotationPresent(ArQuery.class)) {
            getter = loadQueryArgsGetters(param);
        } else if (param.isAnnotationPresent(ArBody.class)) {
            getter = loadBodyArgsGetters(param);
        } else {
            throw new ArTypeAnnotationNotFoundException(
                    "Ar type annotation was not found in the " + param.getName() + " parameter");
        }
        return getter.getArgValue(routeArgs, queryArgs, bodyArgs);
    }

    private final static Map<Parameter, Getter> gettersMap = new HashMap<>();

    private static Getter loadRouteArgsGetters(Parameter param) {
        return gettersMap.computeIfAbsent(param, (k) -> new RouteArgsGetter(k));
    }

    private static Getter loadQueryArgsGetters(Parameter param) {
        return gettersMap.computeIfAbsent(param, (k) -> new QueryArgsGetter(k));
    }

    private static Getter loadBodyArgsGetters(Parameter param) {
        return gettersMap.computeIfAbsent(param, (k) -> new BodyArgsGetter(k));
    }
}