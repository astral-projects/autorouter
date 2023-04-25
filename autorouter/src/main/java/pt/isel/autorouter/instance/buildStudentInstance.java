package pt.isel.autorouter.instance;

import pt.isel.autorouter.Student;

import java.util.Map;

public class buildStudentInstance {
    public Student createInstance(Map<String, String> bodyArgs) {
        int nr = Integer.parseInt(bodyArgs.get("nr"));
        String name = bodyArgs.get("name");
        int group = Integer.parseInt(bodyArgs.get("group"));
        int semester = Integer.parseInt(bodyArgs.get("semester"));
        return new Student(nr, name, group, semester);
    }
}