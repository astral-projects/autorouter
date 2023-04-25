package pt.isel.autorouter.instance;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class buildStudentInstanceWithClass {
    public Object createInstance(Class<?> clazz, Map<String, String> bodyArgs) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        int nr = Integer.parseInt(bodyArgs.get("nr"));
        String name = bodyArgs.get("name");
        int group = Integer.parseInt(bodyArgs.get("group"));
        int semester = Integer.parseInt(bodyArgs.get("semester"));
        Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
        return constructor.newInstance(nr, name, group, semester);
    }
}