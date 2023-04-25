package pt.isel.autorouter.instance;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class buildAnyInstanceWithClass {
    public Object createInstance(Class<?> clazz, Map<String, String> map) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
        List<Object> values = new ArrayList<>();
        for (Parameter param : constructor.getParameters()) {
            String paramName = param.getName();
            Class<?> paramType = param.getType();
            String paramValue = map.get(paramName);
            values.add(convertStringToPrimitiveType(paramType, paramValue));
        }
        return constructor.newInstance(values.toArray());
    }

    private static Object convertStringToPrimitiveType(Class<?> clazz, String value) {
        if (Boolean.class == clazz || boolean.class == clazz) return Boolean.parseBoolean(value);
        if (Byte.class == clazz || byte.class == clazz) return Byte.parseByte(value);
        if (Short.class == clazz || short.class == clazz) return Short.parseShort(value);
        if (Integer.class == clazz || int.class == clazz) return Integer.parseInt(value);
        if (Long.class == clazz || long.class == clazz) return Long.parseLong(value);
        if (Float.class == clazz || float.class == clazz) return Float.parseFloat(value);
        if (Double.class == clazz || double.class == clazz) return Double.parseDouble(value);
        return value;
    }
}
