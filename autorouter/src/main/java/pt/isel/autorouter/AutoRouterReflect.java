package pt.isel.autorouter;

import kotlin.contracts.Returns;
import pt.isel.autorouter.annotations.ArBody;
import pt.isel.autorouter.annotations.ArQuery;
import pt.isel.autorouter.annotations.ArRoute;
import pt.isel.autorouter.annotations.AutoRouter;

import javax.swing.*;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.RecursiveTask;
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
                return toObject(type, routeArgs.get(paramName),routeArgs);
            } else if (annotation instanceof ArBody) {
                return toObject(type, bodyArgs.get(paramName),bodyArgs);
            } else if (annotation instanceof ArQuery) {
                return toObject(type, queryArgs.get(paramName),queryArgs);
            }
        }
        // If no annotation is found, throw exception
        throw new RuntimeException("Annotation not found");
    }
    //atencao aos tipos primitivos com letra minuscula-> int.class
    private static Object toObject( Class clazz, String value , Map<String,String> argumetnsValues) {
        if(value==null) {
            try {
                return createNewInstance(clazz,argumetnsValues);
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

    private static Object createNewInstance(Class clazz,Map<String,String> argumetnsValues) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        //TODO - Find parameters of specific constructor Studnet
        List<Object> args = new ArrayList<>();
        args.add(2023);
        args.add("GitHubcopilot");
        args.add(20);
        args.add(1);
        //Procura do construtor passado como parametro
        //tive de fazer este ciclo pois se nao fizer a procura pelo nome do construtor
        //ao invocar o getConstructor teria de passar logo os tipos de parametros e como nao sabemos tive de fazer este ciclo para procurar o construtor
        var constructors = clazz.getConstructors();
        for(Constructor constructor : constructors) {
            constructor.setAccessible(true);
            //Caso o nome do construtor seja igual ao nome da classe
            if(constructor.getName()==clazz.getName()){
                //Extrair os tipos dos parametros do construtor
                Class<?>[] paramTypes = constructor.getParameterTypes();
                //Converter os valores dos parametros para os tipos dos parametros , aqueles que sao passados como parametro
                var b= castStringToType(paramTypes,argumetnsValues.values().stream().toList());
                //Criar uma nova instancia da classe ja com os parametros corretos e retornar
                return constructor.newInstance(b.toArray());
            }
        }
        throw new RuntimeException("Constructor not found");
        //chamar o getConstrucgtors e escolher o primeiro
        //Constructor o = clazz.getDeclaredConstructor(new Class[] {int.class,String.class,int.class,int.class});
        //o.setAccessible(true);
        //return o.newInstance(args.toArray());

    }
    //Tive de fazer esta funcao pela questao que o getParameterTypes devolve um array de classes
    //e eu preciso de converter os valores dos parametros para os tipos dos parametros que a classe pede
    //entao fiz esta funcao a parte de modo a que eu possa converter os valores dos parametros para os tipos dos parametros
    private static ArrayList<Object>  castStringToType(Class<?>[] t, List<String> values) {
        var args = new ArrayList<>();
        var i=0;
        for(Class<?> type : t){
            var value= values.get(i);
            if (type== java.lang.Integer.class || type == int.class ) {
                     args.add(Integer.parseInt(value));
                } else if (type== Double.class) {
                    args.add(Double.parseDouble(value));
                } else if (type == Boolean.class) {
                    args.add(Boolean.parseBoolean(value));
                } else if (type == Float.class) {
                    args.add(Float.parseFloat(value));
                } else if (type == Long.class) {
                    args.add(Long.parseLong(value));
                } else if (type == Short.class) {
                    args.add(Short.parseShort(value));
                } else if (type == Byte.class) {
                    args.add(Byte.parseByte(value));
                } else if (type == String.class) {
                    args.add(value);
                }
                i+=1;
            }
        return  args;
    }
}



