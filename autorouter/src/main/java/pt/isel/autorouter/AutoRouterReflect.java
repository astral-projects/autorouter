package pt.isel.autorouter;

import pt.isel.autorouter.annotations.ArBody;
import pt.isel.autorouter.annotations.ArQuery;
import pt.isel.autorouter.annotations.ArRoute;
import pt.isel.autorouter.annotations.AutoRouter;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
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
        // Implement functional interface method
        ArHttpHandler handler = (routeArgs, queryArgs, bodyArgs) -> {
            Parameter[] params = m.getParameters();
            // Reminder: Each parameter can have more than one annotatation
            Annotation[][] paramsAnnotations = m.getParameterAnnotations();
            // Iterate through all parameters
            for (int i = 0; i < params.length; i++) {
                // Get current parameter annotations
                String paramName = params[i].getName();
                // TODO - Fix case where a parameter has more than one Ar type annotation
                // TODO - create print function
                // TODO - convert PG code to TDS+
                // TODO - use a map or something similar ex: ArRoute -> ::routeArgs
                // Iterate through all annotations of the current parameter
                for (Annotation annotation: paramsAnnotations[i]) {
                    if (annotation instanceof ArRoute) {
                        System.out.println("@ArRoute: " + params[i].getName() + " = " + routeArgs.get(paramName));
                        Class<?> retType = params[i].getType();
                        String ret = routeArgs.get(paramName); // routeArgs["nr"] = "4123", for example
                        args.add(convert(retType, ret));
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

    private static Object convert(Class<?> targetType, String text) {
        PropertyEditor editor = PropertyEditorManager.findEditor(targetType);
        editor.setAsText(text);
        return editor.getValue();
    }
}