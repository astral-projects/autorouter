package pt.isel.autorouter;

import org.cojen.maker.ClassMaker;
import org.cojen.maker.FieldMaker;
import org.cojen.maker.MethodMaker;
import org.cojen.maker.Variable;
import pt.isel.autorouter.annotations.ArBody;
import pt.isel.autorouter.annotations.ArQuery;
import pt.isel.autorouter.annotations.ArRoute;
import pt.isel.autorouter.exceptions.ArTypeAnnotationNotFoundException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


import static com.fasterxml.jackson.databind.type.LogicalType.Map;

public class AutoRouterDynamic {
    public static Stream<ArHttpRoute> autorouterDynamic(Object controller) {
        return Stream.empty();
    }

    public static ClassMaker buildHandler(Class<?> routerClass, Method fun) {
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

        //Criacao da assintura do metoodo(handler)
        MethodMaker handlerMaker = clazzMaker.addMethod(Optional.class, "handler", Map.class, Map.class, Map.class)
                .public_()
                .override();

        // @ARroute routeargs  @ArQuery queryargs @Arbody bodyargs
        Stream<Variable> mapa = Arrays //search 2 parameters /add 2 parmeters
                .stream(fun.getParameters()).map(
                        //Todo: usar uma mapa para assiciar o parametro.getname() ao mapa que queremos
                        param -> {
                           //param.getName()
                            if (param.isAnnotationPresent(ArRoute.class)) {
                                return handlerMaker.param(0); //associacao do mapa ao parametro
                            } else if (param.isAnnotationPresent(ArQuery.class)) {
                                return handlerMaker.param(1);
                            } else if (param.isAnnotationPresent(ArBody.class)) {
                                return handlerMaker.param(2);
                            }
                            try {
                                throw new ArTypeAnnotationNotFoundException(
                                        "Ar type annotation was not found in the " + param.getName() + " parameter");
                            } catch (ArTypeAnnotationNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
        //TODO(Buscar o get com o reflect ou pelo menos tentar )
        return null;
    }

    /*public class buildHttpHandlerSearch implements ArHttpHandler {
    private final ClassroomController router;

    public HttpHandlerSearch(ClassroomController router) {
        this.router = router;
    }

    @Override
    public Optional<?> handle(Map<String, String> routeArgs, Map<String, String> queryArgs, Map<String, String> bodyArgs) {
        String Classroom = routeArgs.get("classroom");
        String Student = queryArgs.get("student");
        return router.search(Classroom, Student);
    }
}

*/

}
