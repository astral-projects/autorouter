package pt.isel.autorouter;

import org.cojen.maker.ClassMaker;
import org.cojen.maker.FieldMaker;
import org.cojen.maker.MethodMaker;
import org.cojen.maker.Variable;
import pt.isel.autorouter.annotations.ArBody;
import pt.isel.autorouter.annotations.ArQuery;
import pt.isel.autorouter.annotations.ArRoute;
import pt.isel.autorouter.annotations.AutoRouter;


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
                var h1 = buildHandler(controller.getClass(), m).finish();
                // CLASSROOM() criacao do construtor "primario"//adicionado a nova instacia por exemplo "search"
                var handler = h1.getDeclaredConstructor(controller.getClass()).newInstance(controller);
                return new ArHttpRoute(functionName, method, path,(ArHttpHandler) handler);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static ClassMaker buildHandler(Class<?> routerClass, Method method) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        //Criaçáo da classe -- public class buildHttpHandlerSearch implements ArHttpHandler
        ClassMaker clazzMaker = ClassMaker.begin()
                .public_()
                .implement(ArHttpHandler.class);
        //Criaçao do field Router--   private final ClassroomController router;
        FieldMaker routerMaker = clazzMaker.addField(routerClass, "router")
                .public_();
        /*
         *Construcao do construtor da classe
         */
        MethodMaker ctor = clazzMaker
                .addConstructor(routerClass)
                .public_();
        ctor.invokeSuperConstructor(); //vai buscar o construtor defaul da class base pq nao tem parametros
        //Inicia o field router do constructor  this.router = router;
        ctor.field(routerMaker.name()).set(ctor.param(0));

        //Criacao da assintura do metodo(handler)
        MethodMaker handlerMaker = clazzMaker.addMethod(Optional.class, "handle", Map.class, Map.class, Map.class)
                .public_()
                .override();

        Map<String, ParameterInfo> mapArgs = new LinkedHashMap<>();
        // @ARroute routeargs  @ArQuery queryargs @Arbody bodyargs
        for (Parameter param : method.getParameters()) {
            // var x = param.getType();
            if (param.isAnnotationPresent(ArRoute.class)) {
                mapArgs.put(param.getName(), new ParameterInfo(param.getType(), handlerMaker.param(0)));
            } else if (param.isAnnotationPresent(ArQuery.class)) {
                mapArgs.put(param.getName(), new ParameterInfo(param.getType(), handlerMaker.param(1)));
            } else if (param.isAnnotationPresent(ArBody.class)) {
                mapArgs.put(param.getName(), new ParameterInfo(param.getType(), handlerMaker.param(2)));
            }
        }
        // classroom -> routeargs
        // String Classroom = routeArgs.get("classroom");?
        // TODO(Buscar o get com o reflect ou pelo menos tentar)
        System.out.println(mapArgs.size());
        ArrayList<Object> args = new ArrayList<>();
        for (Map.Entry<String, ParameterInfo> entry : mapArgs.entrySet()) {
            // Example: Key: classroom, Value: routeArgs
            String key = entry.getKey();
            ParameterInfo info = entry.getValue();
            var type = info.type();
            var value = info.map();
            if (isPrimitiveOrStringType(type)) {
                args.add(value.invoke("get", key).cast(type));
            } else {
                // Get declared constructors
                Constructor<?>[] constructors = type.getDeclaredConstructors();
                // Check if a parameter type has a constructor
                /*
                public Student createInstance(Map<String, String> bodyArgs) {
                    int nr = Integer.parseInt(bodyArgs.get("nr"));
                    String name = bodyArgs.get("name");
                    int group = Integer.parseInt(bodyArgs.get("group"));
                    int semester = Integer.parseInt(bodyArgs.get("semester"));
                    return new Student(nr, name, group, semester);
                }*/

                args.add(
                        constructors.length == 0 ? null :
                        buildNewComplexInstance(type, value)
                );
            }
        }
        System.out.println(method.getName());
        var result = handlerMaker.field(routerMaker.name()).invoke(method.getName(), args.toArray());
        handlerMaker.return_(result.cast(Optional.class));
        return clazzMaker;
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

    /**
     * public class buildNewComplexInstance {
     *   public Object createInstance(Student student, Map<String, String> bodyArgs) {
     *   // for each class parameter
     *          var nr = parseInt(bodyArgs.get("nr"));
     *          var name = bodyArgs.get("name");
     *          var group = parseInt(bodyArgs.get("group"));
     *          var semester = parseInt(bodyArgs.get("semester"));
     *       return new Student(nr, name, group, semester);
     *   }
     * }
     */



    public static Object buildNewComplexInstance(Class<?> targetClass, Variable mapVariable) {
        try {
            Constructor<?> constructor = targetClass.getDeclaredConstructors()[0];
            Object[] constructorArgs = new Object[constructor.getParameterTypes().length];
            int index = 0;

            for (Class<?> argType : constructor.getParameterTypes()) {
                String key = constructor.getParameters()[index].getName();
                Variable value = mapVariable.invoke("get", key).cast(String.class);
                if (argType == int.class || argType == Integer.class) {
                    Variable integerClass = Expressions.constant(Integer.class);
                    constructorArgs[index] = integerClass.invoke("parseInt", value).cast(int.class);
                } else if (argType == String.class) {
                    constructorArgs[index] = value;
                }
                // Add more types if needed
                index++;
            }
            return constructor.newInstance(constructorArgs);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

/**
 * public class buildHttpHandlerSearch implements ArHttpHandler {
    private final ClassroomController router;

    public HttpHandlerSearch(ClassroomController router) {
        this.router = router;
    }

    @Override
    public Optional<?> handle(Map<String, String> routeArgs, Map<String, String> queryArgs, Map<String, String> bodyArgs) {
        // for -> args
        String Classroom = routeArgs.get("classroom");
        String Student = queryArgs.get("student");

        return router.search(args[0], args[1]);
    }
}
*/

}
