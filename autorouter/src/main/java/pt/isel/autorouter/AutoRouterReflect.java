package pt.isel.autorouter;

import pt.isel.autorouter.annotations.ArRoute;
import pt.isel.autorouter.annotations.AutoRouter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class AutoRouterReflect {
     public static Stream<ArHttpRoute> autorouterReflect(Object controller) {
        /**
         * TO DO
         * obtain the object class
         * get all declared methods
         * filter methods that have autoroute annotation and have an Optional return type
         *
         * For each method,  create an HTTP instance
         *
         */


         Stream<ArHttpRoute> ArHttpRouteStream =   Arrays.stream(controller.getClass().getMethods()).filter(m->
                 //methods that have autoroute annotation and have an Optional return type
                 m.isAnnotationPresent(AutoRouter.class) &&  m.getReturnType() != Optional.class)
                 .map(m  -> (ArHttpRoute) createArHttpRoute(controller,m ));

         return ArHttpRouteStream;
     }

    private static Object createArHttpRoute(Object target, Method m) {
        String functionName = m.getName();
        ArVerb method = m.getAnnotation(AutoRouter.class).method();
        String path = m.getAnnotation(AutoRouter.class).value();
        //return the annotated ArRoutes  parameters
        Stream<Parameter> parameterAnnotated  =  Arrays.stream(m.getParameters()).filter(it->it.isAnnotationPresent(ArRoute.class));

        String parameter = String.valueOf(parameterAnnotated.filter(it->it.getName()=="classroom").findFirst());

        ArRoute arRoute = m.getAnnotation(ArRoute.class);
        Annotation[][] parameterAnnotations= m.getParameterAnnotations();
        for (Annotation[] annotations : m.getParameterAnnotations()){
             ArRoute[] aas = (ArRoute[])  Arrays.stream(annotations).map(Annotation::annotationType).filter(it-> it==ArRoute.class).toArray();
        }
        //valor do paramentro anotado no caso do classrrom encontrar o 42d
        //String string = routeArgs[arRoute.value()];
        //ArHttpRoute handler = (routeArgs, queryArgs, bodyAgrs) -> {
          //  String classroom = ;//routeArgs.get("classroom");// get annotation
            //return m.invoke(target, m);
        //};
        return Optional.empty();
       // return new ArHttpRoute(functionName, method, path, handler);
    }
}
