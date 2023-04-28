package pt.isel.autorouter;

import org.cojen.maker.ClassMaker;
import org.cojen.maker.FieldMaker;
import org.cojen.maker.MethodMaker;
import org.cojen.maker.Variable;
import pt.isel.autorouter.annotations.ArBody;
import pt.isel.autorouter.annotations.ArQuery;
import pt.isel.autorouter.annotations.ArRoute;
import pt.isel.autorouter.annotations.AutoRouter;
import pt.isel.autorouter.exceptions.ArTypeAnnotationNotFoundException;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Stream;

public class AutoRouterDynamic {

    public static Stream<ArHttpRoute> autorouterDynamic(Object controller) {
        // Filter methods that have autoroute annotation and have an <Optional> return type
        Stream<Method> methods = Arrays
                .stream(controller.getClass().getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(AutoRouter.class)
                        && m.getReturnType() == Optional.class);
        // For each method, create an ArHttpRoute instance
        return methods.map(m -> {
            try {
                String functionName = m.getName();
                ArVerb method = m.getAnnotation(AutoRouter.class).method();
                String path = m.getAnnotation(AutoRouter.class).value();
                // build handler class dynamically and instantiate it
                Class<?> classHandler = buildHandler(controller.getClass(), m).finish();
                Object handler = classHandler.getDeclaredConstructor(controller.getClass()).newInstance(controller);
                return new ArHttpRoute(functionName, method, path, (ArHttpHandler) handler);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | ArTypeAnnotationNotFoundException|
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Builds a class that implements the ArHttpHandler interface dynamically.
     * @param routerClass - the class of the router that will be injected in the handler.
     * @param method - the method that will be invoked in the handler.
     * @return the ClassMaker instance that represents the class that implements the functional **ArHttpHandler**
     * interface which was created dynamically.
     */
    public static ClassMaker buildHandler(Class<?> routerClass, Method method) throws ArTypeAnnotationNotFoundException {
        //Criaçáo da classe -- public class buildHttpHandlerSearch implements ArHttpHandler
        ClassMaker clazzMaker = ClassMaker.begin()
                .public_()
                .implement(ArHttpHandler.class);

        // Create the router field -> private Router router;
        FieldMaker routerMaker = clazzMaker.addField(routerClass, "router")
                .public_();

        // Create the constructor
        MethodMaker ctor = clazzMaker
                .addConstructor(routerClass)
                .public_();

        // Invoke the super constructor
        ctor.invokeSuperConstructor();

        // Set the router field in the constructor -> this.router = router;
        ctor.field(routerMaker.name()).set(ctor.param(0));

        // Implementation of the handle method present in the ArHttpHandler interface
        MethodMaker handlerMaker = clazzMaker.addMethod(Optional.class, "handle", Map.class, Map.class, Map.class)
                .public_()
                .override();

        Map<String, ParameterInfo> mapArgs = new LinkedHashMap<>();
        // For each parameter ArType annotation, save information about the parameter type and
        // the map it should be retrieved from
        for (Parameter param : method.getParameters()) {
            // String paramName = "classroom"
            String paramName = param.getName();
            // Class<?> paramType = java.lang.String
            Class<?> paramType = param.getType();
            if (param.isAnnotationPresent(ArRoute.class)) {
                mapArgs.put(paramName, new ParameterInfo(paramType, handlerMaker.param(0)));
            } else if (param.isAnnotationPresent(ArQuery.class)) {
                mapArgs.put(paramName, new ParameterInfo(paramType, handlerMaker.param(1)));
            } else if (param.isAnnotationPresent(ArBody.class)) {
                mapArgs.put(paramName, new ParameterInfo(paramType, handlerMaker.param(2)));
            }else{
                throw new ArTypeAnnotationNotFoundException("Annotatiosn Required");
            }

        }
        ArrayList<Object> args = new ArrayList<>();
        // For each parameter, get its value from the corresponding map
        for (Map.Entry<String, ParameterInfo> entry : mapArgs.entrySet()) {
            // String paramName = "classroom"
            String paramName = entry.getKey();
            // ParameterInfo(java.lang.String, routeArgs)
            ParameterInfo paramInfo = entry.getValue();
            Class<?> type = paramInfo.type();
            Variable map = paramInfo.map();
            System.out.println("Nome do parametro"+ paramName);
            System.out.println("Nome do tipo " + type);
            if (isPrimitiveOrStringType(type)) {
                // args.add(map.invoke("get", paramName).cast(type));
                Variable simpleTypeInstance = getValueAndConvertToType(handlerMaker, type, map, paramName);
                args.add(simpleTypeInstance);
            } else {
                // int nr = Integer.valueOf(bodyArgs.get("nr"))
                // String name = bodyArgs.get("name")
                // int group = Integer.valueOf(bodyArgs.get("group"))
                // int semester = Integer.valueOf(bodyArgs.get("semester"))
                // new Student(nr, name, group, semester)
                Variable complexTypeInstance = buildNewComplexInstance(handlerMaker, type, map);
                args.add(complexTypeInstance);
            }
        }
        // router.search(classroom)
        var result = handlerMaker.field(routerMaker.name()).invoke(method.getName(), args.toArray());
        // return Optional.of(result) (inside the handler)
        handlerMaker.return_(result.cast(Optional.class));
        return clazzMaker;
    }

    /**
     * Builds a new instance of a complex type (non-primitive or String) dynamically.
     * @param handlerMaker - the MethodMaker instance that represents the handle method being implemented.
     * @param clazz - the class of the complex type that will be instantiated.
     * @param map - the map that contains the values that will be used to instantiate the complex type.
     * @return the Variable instance that represents the new instance of the complex type to be generated.
     */
    private static Variable buildNewComplexInstance(
            MethodMaker handlerMaker,
            Class<?> clazz,
            Variable map
    ) {
        Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
        ArrayList<Object> args = new ArrayList<>();

        for (Parameter constructorParam : constructor.getParameters()) {
            // Get a constructor param name: Ex: nr
            String paramName = constructorParam.getName();
            // Get a constructor param type
            // : Ex: int
            Class<?> type = constructorParam.getType();
            Variable simpleTypeInstance = getValueAndConvertToType(handlerMaker, type, map, paramName);
            args.add(simpleTypeInstance);
       }
        // return new Student(nr, name, group, semester);
        return handlerMaker.new_(clazz, args.toArray());
    }

    private static Variable getValueAndConvertToType(MethodMaker handlerMaker, Class<?> type, Variable map, String paramName) {
        Variable stringValue = map.invoke("get", paramName);
        if (type != String.class) {
            return convertToPrimitiveType(handlerMaker, type, stringValue);
        } else {
            return stringValue.cast(String.class);
        }
    }

    private static Variable convertToPrimitiveType(MethodMaker handlerMaker, Class<?> type, Variable stringValue) {
        // String stringValue = bodyArgs.get("nr")
        // return Integer.parseInt(value)
        // return type.invoke("parse" + capitalize(type.classType().getSimpleName()), stringValue);
        if (type == int.class || type== Integer.class) return handlerMaker.var(Integer.class).invoke("parseInt", stringValue.cast(String.class));
        if (type == long.class) return handlerMaker.var(Long.class).invoke("parseLong", stringValue.cast(String.class));
        if (type == float.class) return handlerMaker.var(Float.class).invoke("parseFloat", stringValue.cast(String.class));
        if (type == double.class) return handlerMaker.var(Double.class).invoke("parseDouble", stringValue.cast(String.class));
        if (type == boolean.class || type==Boolean.class) return handlerMaker.var(Boolean.class).invoke("parseBoolean", stringValue.cast(String.class));
        if (type == byte.class) return handlerMaker.var(Byte.class).invoke("parseByte", stringValue.cast(String.class));
        if (type == short.class) return handlerMaker.var(Short.class).invoke("parseShort", stringValue.cast(String.class));
        System.out.println(type);
        throw new RuntimeException("Unknown type: " + type);
    }

    private static boolean isPrimitiveOrStringType(Class<?> clazz) {
        System.out.println(clazz);
        if (Boolean.class == clazz || boolean.class == clazz) return true;
        if (Byte.class == clazz || byte.class == clazz) return true;
        if (Short.class == clazz || short.class == clazz) return true;
        if (Integer.class == clazz || int.class == clazz) return true;
        if (Long.class == clazz || long.class == clazz) return true;
        if (Float.class == clazz || float.class == clazz) return true;
        if (Double.class == clazz || double.class == clazz) return true;
        return String.class == clazz;
    }
}

