package pt.isel.examples;

import pt.isel.autorouter.ArHttpHandler;
import pt.isel.classroom.ClassroomController;

import java.util.Map;
import java.util.Optional;

import static java.lang.Integer.parseInt;

public class HttpHandlerDelete implements ArHttpHandler {
    private final ClassroomController router;

    public HttpHandlerDelete(ClassroomController router) {
        this.router = router;
    }

    @Override
    public Optional<?> handle(Map<String, String> routeArgs, Map<String, String> queryArgs, Map<String, String> bodyArgs) {
        // Get the classroom
        String classroom = routeArgs.get("classroom");

        // Get the nr and pass to int
        int nr = parseInt(routeArgs.get("nr"));

        // Remove the student from the classroom
        return router.removeStudent(classroom, nr);
    }
}
