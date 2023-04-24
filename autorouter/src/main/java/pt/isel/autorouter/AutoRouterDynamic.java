package pt.isel.autorouter;

import org.cojen.maker.ClassMaker;
import org.cojen.maker.FieldMaker;
import org.cojen.maker.MethodMaker;
import org.cojen.maker.Variable;
import pt.isel.autorouter.annotations.ArBody;
import pt.isel.autorouter.annotations.ArQuery;
import pt.isel.autorouter.annotations.ArRoute;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Stream;

public class AutoRouterDynamic {

    public static Stream<ArHttpRoute> autorouterDynamic(Object controller) {
        return Stream.empty();
    }

    public static ClassMaker buildHandler(Class<?> routerClass, Method method) {
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
            // routeArgs.get("classroom");
            args.add(value.invoke("get", key).cast(type));
        }
        System.out.println(method.getName());
        var result = handlerMaker.field(routerMaker.name()).invoke(method.getName(), args.toArray());
        handlerMaker.return_(result.cast(Optional.class));
        return clazzMaker;
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
