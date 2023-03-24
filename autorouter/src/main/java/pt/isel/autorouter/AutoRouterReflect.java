package pt.isel.autorouter;

import kotlin.contracts.Returns;
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
                // Iterate through all annotations of the current parameter
                args.add(findParameterAnnotated( paramsAnnotations[i], params[i].getType(), paramName,routeArgs,bodyArgs,queryArgs));
            }
            try {
                // Args needs to be converted to Object[]
                return (Optional<?>) m.invoke(target, args.toArray());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };
        return new ArHttpRoute(functionName, method, path, handler);
    }
    // TODO - Fix case where a parameter has more than one Ar type annotation
    //Find the correct annotation for the current parameter
    // and return the converted value
    private static Object findParameterAnnotated(Annotation[] paramsAnnotations ,
                                                 Class<?> type,
                                                 String paramName,
                                                 Map<String, String> routeArgs,
                                                 Map<String, String> bodyArgs,
                                                 Map<String, String> queryArgs) {
        // Iterate through all annotations of the current parameter
        for (Annotation annotation : paramsAnnotations) {
            if (annotation instanceof ArRoute) {
                return toObject(type, routeArgs.get(paramName));
            } else if (annotation instanceof ArBody) {
                return toObject(type, bodyArgs.get(paramName));
            } else if (annotation instanceof ArQuery) {
                return toObject(type, queryArgs.get(paramName));
            }
        }
        // If no annotation is found, throw exception
        throw new RuntimeException("Annotation not found");
    }
    //atencao aos tipos primitivos com letra minuscula-> int.class
    private static Object toObject( Class clazz, String value ) {
        if(value==null) {
            try {
                return createNewInstance(clazz);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        if( Boolean.class == clazz ) return Boolean.parseBoolean( value );
        if( Byte.class == clazz ) return Byte.parseByte( value );
        if( Short.class == clazz ) return Short.parseShort( value );
        if( Integer.class == clazz || int.class==clazz) return Integer.parseInt( value );
        if( Long.class == clazz ) return Long.parseLong( value );
        if( Float.class == clazz ) return Float.parseFloat( value );
        if( Double.class == clazz ) return Double.parseDouble( value );
        return value;
    }

    private static Object createNewInstance(Class clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        //TODO - Find parameters of specific constructor Studnet
        List<Object> args = new ArrayList<>();
        args.add(2023);
        args.add("GitHubcopilot");
        args.add(20);
        args.add(1);
        //chamar o getConstrucgtors e escolher o primeiro
        Constructor o = clazz.getDeclaredConstructor(new Class[] {int.class,String.class,int.class,int.class});
        o.setAccessible(true);
        return o.newInstance(args.toArray());

    }
}

