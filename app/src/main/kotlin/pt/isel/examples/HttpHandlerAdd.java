package pt.isel.examples;

import pt.isel.autorouter.ArHttpHandler;
import pt.isel.classroom.ClassroomController;
import pt.isel.classroom.Student;

import java.util.Map;
import java.util.Optional;

import static java.lang.Integer.parseInt;

public class HttpHandlerAdd implements ArHttpHandler {
    private final ClassroomController router;

    public HttpHandlerAdd(ClassroomController router) {
        this.router = router;
    }

    @Override
    public Optional<?> handle(Map<String, String> routeArgs, Map<String, String> queryArgs, Map<String, String> bodyArgs) {
        // Get the nr and pass to int
        String nrString = routeArgs.get("nr");
        int nr = parseInt(nrString);

        // Get the classroom
        String classroom = routeArgs.get("classroom");

        // Get the parameters of the class
        String name = bodyArgs.get("name");
        int group = parseInt(bodyArgs.get("group"));    // 2 things, the get group and toInt()
        int semester = parseInt(bodyArgs.get("semester"));

        // Create a new student
        Student student = new Student(nr, name, group, semester);

        // Add the student to the classroom
        return router.addStudent(classroom, nr, student);
    }
}
