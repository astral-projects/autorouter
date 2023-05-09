package pt.isel.autorouter;

import kotlin.Pair;
import org.cojen.maker.ClassMaker;
import org.cojen.maker.FieldMaker;
import org.cojen.maker.MethodMaker;
import org.cojen.maker.Variable;
import pt.isel.autorouter.annotations.ArBody;
import pt.isel.autorouter.annotations.ArQuery;
import pt.isel.autorouter.annotations.ArRoute;
import pt.isel.autorouter.annotations.AutoRouter;
import pt.isel.autorouter.exceptions.ArTypeAnnotationNotFoundException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Stream;

public class AutoRouterDynamic {

    /**
     * Creates a stream of ArHttpRoute instances for the given controller.
     * @param controller - the controller that will be used to create the ArHttpRoute instances.
     */
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

        Map<String, DynamicParameterInfo> mapArgs = new LinkedHashMap<>();
        // For each parameter ArType annotation, save information about the parameter type and
        // the map it should be retrieved from
        for (Parameter param : method.getParameters()) {
            // String paramName = "classroom"
            String paramName = param.getName();
            // Class<?> paramType = java.lang.String
            Class<?> paramType = param.getType();
            if (param.isAnnotationPresent(ArRoute.class)) {
                mapArgs.put(paramName, new DynamicParameterInfo(paramType, handlerMaker.param(0)));
            } else if (param.isAnnotationPresent(ArQuery.class)) {
                mapArgs.put(paramName, new DynamicParameterInfo(paramType, handlerMaker.param(1)));
            } else if (param.isAnnotationPresent(ArBody.class)) {
                mapArgs.put(paramName, new DynamicParameterInfo(paramType, handlerMaker.param(2)));
            } else {
                throw new ArTypeAnnotationNotFoundException("Parameter " + param.getName() + " has no Ar type annotation");
            }

        }
        ArrayList<Object> args = new ArrayList<>();
        // For each parameter, get its value from the corresponding map
        for (Map.Entry<String, DynamicParameterInfo> entry : mapArgs.entrySet()) {
            // String paramName = "classroom"
            String paramName = entry.getKey();
            // DynamicParameterInfo(java.lang.String, routeArgs)
            DynamicParameterInfo paramInfo = entry.getValue();
            Class<?> type = paramInfo.type();
            Variable map = paramInfo.map();
            args.add(getValueAndConvertToType(handlerMaker, paramName, type, map));
        }
        // router.search(classroom)
        var result = handlerMaker.field(routerMaker.name()).invoke(method.getName(), args.toArray());
        // return Optional.of(result) (inside the handler)
        handlerMaker.return_(result.cast(Optional.class));
        return clazzMaker;
    }

    /**
     * Converts to a simple or complex type, depending on the type of the parameter being processed.
     * @param handlerMaker - the MethodMaker instance that represents the handle method being implemented.
     * @param paramName - the name of the parameter being processed.
     * @param type - the type of the parameter being processed.
     * @param map - the map from which the parameter value will be retrieved.
     * @return the Variable instance that represents the new instance of the complex type to be generated.
     */
    private static Variable getValueAndConvertToType(MethodMaker handlerMaker, String paramName, Class<?> type, Variable map) {
        if (type == String.class || primitiveWrapperConverter.containsKey(type)) {
            return getStringOrPrimitive(handlerMaker, paramName, type, map);
        } else {
            // int nr = Integer.valueOf(bodyArgs.get("nr"))
            // String name = bodyArgs.get("name")
            // int group = Integer.valueOf(bodyArgs.get("group"))
            // int semester = Integer.valueOf(bodyArgs.get("semester"))
            // new Student(nr, name, group, semester)
            return buildNewComplexInstance(handlerMaker, type, map);
        }
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
        // For each constructor parameter:
        for (Parameter constructorParam : constructor.getParameters()) {
            // Get a constructor param name: Ex: nr
            String paramName = constructorParam.getName();
            // Get a constructor param type: Ex: int
            Class<?> type = constructorParam.getType();
            // At this point, this instance could be of complex or simple type
            Variable typeInstance = getValueAndConvertToType(handlerMaker, paramName, type, map);
            args.add(typeInstance);
       }
        // return new Student(nr, name, group, semester);
        return handlerMaker.new_(clazz, args.toArray());
    }

    private static Variable getStringOrPrimitive(MethodMaker handlerMaker, String paramName, Class<?> type, Variable map) {
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
        Pair<Class<?>, String> converter = primitiveWrapperConverter.get(type);
        if (converter == null) {
            throw new RuntimeException("Type does not have a wrapper: " + type);
        }
        Class<?> wrapperClass = converter.getFirst();
        String methodName = converter.getSecond();
        return handlerMaker.var(wrapperClass).invoke(methodName, stringValue.cast(String.class));
    }

    private static final Map<Class<?>, Pair<Class<?>, String>> primitiveWrapperConverter = new HashMap<>() {{
        put(int.class, new Pair<>(Integer.class, "parseInt"));
        put(Integer.class, new Pair<>(Integer.class, "parseInt"));
        put(long.class, new Pair<>(Integer.class, "parseLong"));
        put(Long.class, new Pair<>(Integer.class, "parseLong"));
        put(char.class, new Pair<>(Character.class, "charAt"));
        put(Character.class, new Pair<>(Character.class, "charAt"));
        put(float.class, new Pair<>(Float.class, "parseFloat"));
        put(Float.class, new Pair<>(Float.class, "parseFloat"));
        put(double.class, new Pair<>(Double.class, "parseDouble"));
        put(Double.class, new Pair<>(Double.class, "parseDouble"));
        put(boolean.class, new Pair<>(Boolean.class, "parseBoolean"));
        put(Boolean.class, new Pair<>(Boolean.class, "parseBoolean"));
        put(byte.class, new Pair<>(Byte.class, "parseByte"));
        put(Byte.class, new Pair<>(Byte.class, "parseByte"));
        put(short.class, new Pair<>(Short.class, "parseShort"));
        put(Short.class, new Pair<>(Short.class, "parseShort"));
    }};
}

