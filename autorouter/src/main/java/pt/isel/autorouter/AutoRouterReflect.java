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
        // Get values for ArHttpRoute parameters:
        String functionName = m.getName();
        ArVerb method = m.getAnnotation(AutoRouter.class).method();
        String path = m.getAnnotation(AutoRouter.class).value();
        // Create a list to store retrieved values of annotated parameter with Ar type annotation
        List<Object> args = new ArrayList<>();
        // Implement functional interface method
        ArHttpHandler handler = (routeArgs, queryArgs, bodyArgs) -> {
            // The next map reduces the use of if-else conditions in order to retrieve
            // the correspondent map of a given Ar annotation.
            Map<Class<?>, Map<String, String>> requestArgs = new HashMap<>();
            requestArgs.put(ArRoute.class, routeArgs);
            requestArgs.put(ArQuery.class, queryArgs);
            requestArgs.put(ArBody.class, bodyArgs);
            // Iterate through all parameters
            System.out.println("Method: " + Arrays.toString(m.getParameters()));
            for (Parameter param : m.getParameters()) {
                Object value = findParameterValueWithArAnnotation(param, requestArgs);
                // Added retrieved value to the array to be sent to the current method
                args.add(value);
            }
            System.out.println(args);
            try {
                // Args needs to be converted to Object[]
                return (Optional<?>) m.invoke(target, args.toArray());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };
        return new ArHttpRoute(functionName, method, path, handler);
    }

    // Find the correct annotation for the current parameter
    // and return the converted value
    private static Object findParameterValueWithArAnnotation(
            Parameter param,
            Map<Class<?>, Map<String, String>> requestArgs
    ) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        // Iterate through all the annotations of the current parameter
        for (Annotation annotation : param.getAnnotations()) {
            // Assert if current annotation is of an Ar type
            if (requestArgs.containsKey(annotation.annotationType())) {
                // Retrieve annotation correspondent map.
                // Ex: @ArRoute -> routeArgs
                Map<String, String> mapArgs = requestArgs.get(annotation.annotationType());
                // Retrieve correspondent value from the parameter name.
                // Ex: classroom -> l41d
                String stringValue = mapArgs.get(param.getName());
                // Check parameter type class
                if (isPrimitiveOrStringType(param.getType())) {
                    return convertStringToPrimitiveType(param.getType(), stringValue);
                } else {
                    // Get declared constructors
                    Constructor<?>[] constructors = param.getType().getDeclaredConstructors();
                    // Check if a parameter type has a constructor
                    return constructors.length == 0 ? null : createNewInstance(param.getType(), constructors[0] ,mapArgs);
                }
            }
        }
        // If no annotation is found in this parameter, throw exception
        throw new RuntimeException("Ar type annotation was not found in the " + param.getName() + " parameter");
    }

    private static boolean isPrimitiveOrStringType(Class<?> clazz) {
        if (Boolean.class == clazz || boolean.class == clazz) return true;
        if (Byte.class == clazz || byte.class == clazz) return true;
        if (Short.class == clazz || short.class == clazz) return true;
        if (Integer.class == clazz || int.class == clazz) return true;
        if (Long.class == clazz || long.class == clazz) return true;
        if (Float.class == clazz || float.class == clazz) return true;
        if (Double.class == clazz || double.class == clazz) return true;
        return String.class == clazz;
    }

    private static Object convertStringToPrimitiveType(Class<?> clazz, String value) {
        // This code could be simplified by using reflection, but the authors didn't
        // want to introduce more overhead by using it unnecessarily
        if (Boolean.class == clazz || boolean.class == clazz) return Boolean.parseBoolean(value);
        if (Byte.class == clazz || byte.class == clazz) return Byte.parseByte(value);
        if (Short.class == clazz || short.class == clazz) return Short.parseShort(value);
        if (Integer.class == clazz || int.class == clazz) return Integer.parseInt(value);
        if (Long.class == clazz || long.class == clazz) return Long.parseLong(value);
        if (Float.class == clazz || float.class == clazz) return Float.parseFloat(value);
        if (Double.class == clazz || double.class == clazz) return Double.parseDouble(value);
        return value;
    }

    private static Object createNewInstance(
            Class<?> receivedClass,
            Constructor<?> constructor,
            Map<String, String> argsValues
    ) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        // Assert if the current constructor name equals the received class name.
        List<Object> args = new ArrayList<>();
        // Convert the string value of the parameters to their corresponding type
        for (Parameter constructorParam : constructor.getParameters()) {
            // Get constructor param name: Ex: nr
            String name = constructorParam.getName();
            Object value = convertStringToPrimitiveType(constructorParam.getType(), argsValues.get(name));
            args.add(value);
        }
        // Change constructor accessibility to public
        constructor.setAccessible(true);
        // Return a new created instance of the received class with all parameter types correctly placed
        return constructor.newInstance(args.toArray());
    }
}