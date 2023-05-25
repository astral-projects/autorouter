package pt.isel.autorouter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class JsonServer implements AutoCloseable {

    final Javalin server = Javalin.create();
    final static ObjectMapper mapper = new ObjectMapper();

    public JsonServer(Stream<ArHttpRoute> routes) {
        routes.forEach(this::addRoute);
    }

    /**
     * Parses body request as Json and return Json back.
     */
    // Object need to bring info that handler returns a Sequence<Sequence<String>>
    // Do an if to check if it is a sequence of sequence of string and if it is need to be registed in different way
    // How? See in moodle
    // if it not do the normal thing
    // Resume: Add more info from ArHttpRoute then came from annotations
    public final JsonServer addRoute(ArHttpRoute route) {
        // TODO("ArHttpRoute change annotations
        // TODO(allow this implementation and other that enables the annotations
        // RECORD DIZ se a o record traz um optional de sequencias de sequencias)
        Handler handler = httpHandlerForRoute(route);
        switch (route.method()) {
            case GET -> server.get(route.path(), handler);
            case POST -> server.post(route.path(), handler);
            case DELETE -> server.delete(route.path(), handler);
            case PUT -> server.put(route.path(), handler);
        }
        return this;
    }

    /**
     * Creates a Javalin Handler for an autorouter ArHttpRoute.
     * Parses body request as Json.
     */
    private static Handler httpHandlerForRoute(ArHttpRoute route) {
        return ctx -> {
            var routeArgs = ctx.pathParamMap();
            var queryArgs = ctx.queryParamMap().entrySet().stream().collect(toMap(e -> e.getKey(), e -> e.getValue().get(0)));
            var bodyArgs = ctx.body().isEmpty() ? null : mapper.readValue(ctx.body(), Map.class);
            var res = route.handler().handle(routeArgs, queryArgs, bodyArgs);
            if(res.isPresent()) {
                ctx.json(res.get());
            } else {
                // Status code 404
                throw new NotFoundResponse();
            }
        };
    }

    public void start(int port) {
        server.start(port);
    }

    @Override
    public void close() {
        server.close();
    }

    @NotNull
    public Javalin javalin() {
        return server;
    }
}
