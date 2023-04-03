package pt.isel.autorouter.getters;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractGetter implements Getter {
    private final Parameter param;

    protected AbstractGetter(Parameter param) {
        this.param = param;
    }

    protected Object getValueFromMap(Map<String, String> map) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        String stringValue = map.get(param.getName());
        System.out.println(stringValue);
        if (isPrimitiveOrStringType(param.getType())) {
            System.out.print(" - is primitive");
            return convertStringToPrimitiveType(param.getType(), stringValue);
        } else {
            // Get declared constructors
            Constructor<?>[] constructors = param.getType().getDeclaredConstructors();
            System.out.print(" - is not primitive" + param.getType());
            // Check if a parameter type has a constructor
            return constructors.length == 0 ? null : createNewInstance(constructors[0], map);
        }
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

    private static Object convertStringToPrimitiveType(Class<?> clazz, String value) {
        // This code could be simplified by using reflection, but the authors didn't
        // want to introduce more overhead by using it unnecessarily
        if (Boolean.class == clazz || boolean.class == clazz) return Boolean.parseBoolean(value);
        if (Byte.class == clazz || byte.class == clazz) return Byte.parseByte(value);
        if (Short.class == clazz || short.class == clazz) return Short.parseShort(value);
        if (Integer.class == clazz || int.class == clazz) return Integer.parseInt(value);
        if (Long.class == clazz || long.class == clazz) return Long.parseLong(value);
        if (Float.class == clazz || float.class == clazz) return Float.parseFloat(value);
        if (Double.class == clazz || double.class == clazz) return Double.parseDouble(value);
        return value;
    }

    private static Object createNewInstance(
            Constructor<?> constructor,
            Map<String, String> argsValues
    ) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        System.out.println(argsValues);
        // Assert if the current constructor name equals the received class name.
        List<Object> args = new ArrayList<>();
        // Convert the string value of the parameters to their corresponding type
        for (Parameter constructorParam : constructor.getParameters()) {
            // Get constructor param name: Ex: nr
            String name = constructorParam.getName();
            System.out.println(constructorParam.getType() + " -> " + argsValues.get(name));
            Object value = convertStringToPrimitiveType(constructorParam.getType(), argsValues.get(name));
            args.add(value);
        }
        // Change constructor accessibility to public
        constructor.setAccessible(true);
        // Return a new created instance of the received class with all parameter types correctly placed
        return constructor.newInstance(args.toArray());
    }
}