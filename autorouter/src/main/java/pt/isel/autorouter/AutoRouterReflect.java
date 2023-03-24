package pt.isel.autorouter;

import pt.isel.autorouter.annotations.ArBody;
import pt.isel.autorouter.annotations.ArQuery;
import pt.isel.autorouter.annotations.ArRoute;
import pt.isel.autorouter.annotations.AutoRouter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Stream;

public class AutoRouterReflect {
    public static Stream<ArHttpRoute> autorouterReflect(Object controller) {
        // Filter methods that have autoroute annotation and have an Optional return type
        Stream<Method> methods = Arrays
                .stream(controller.getClass().getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(AutoRouter.class)
                        && m.getReturnType() == Optional.class);
        // For each method, create an HTTP instance
        return methods.map(m -> createArHttpRoute(controller, m));
    }

    private static ArHttpRoute createArHttpRoute(Object target, Method m) {
        // Get values for ArHttpRoute parameters
        String functionName = m.getName();
        ArVerb method = m.getAnnotation(AutoRouter.class).method();
        String path = m.getAnnotation(AutoRouter.class).value();
        // Create a list to store retrieved annotated types
        List<Object> args = new ArrayList<>();
        // Implement functional interface
        ArHttpHandler handler = (routeArgs, queryArgs, bodyArgs) -> {
            Parameter[] params = m.getParameters();
            // Reminder: each parameter can have more than one annotated
            Annotation[][] paramsAnnotations = m.getParameterAnnotations();
            // Iterate through all parameters
            for (int i = 0; i < params.length; i++) {
                // Get current parameter annotations
                String paramName = params[i].getName();
                // TODO - Fix case where a parameter has more than one Ar type annotation
                // TODO - create print function
                // TODO - convert PG code to TDS+
                // TODO - use a map ex: ArRoute -> ::routeArgs
                // Retrieve only existing Ar type annotation of the current parameter

                for (Annotation annotation: paramsAnnotations[i]) {
                    if (annotation instanceof ArRoute) {
                        System.out.println("@ArRoute: " + params[i].getName() + " = " + routeArgs.get(paramName));

                        Object retType = params[i].getParameterizedType();
                        int numb;
                        String ret = routeArgs.get(paramName);
                        if(paramName.equals("nr")){
                            numb = ((Integer.parseInt(ret)));
                            args.add(numb);
                            break;
                        }
                        System.out.println(retType);
                        args.add(routeArgs.get(paramName));
                        break;
                    } else if (annotation instanceof ArBody) {
                        System.out.println("@ArBody: " + params[i].getName() + " = " + bodyArgs.get(paramName));
                        args.add(bodyArgs.get(paramName));
                        break;
                    } else if (annotation instanceof ArQuery) {
                        System.out.println("@ArQuery: " + params[i].getName() + " = " + queryArgs.get(paramName));
                        args.add(queryArgs.get(paramName));
                        break;
                    }
                }
            }
            System.out.println("Method name: " + m.getName());
            System.out.println("Arguments passed to method: " + args);
            try {
                // Args needs to be converted to Object[]
                return (Optional<?>) m.invoke(target, args.toArray());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };
        return new ArHttpRoute(functionName, method, path, handler);
    }
}