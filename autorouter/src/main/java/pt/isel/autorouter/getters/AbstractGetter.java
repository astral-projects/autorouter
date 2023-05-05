package pt.isel.autorouter.getters;

import kotlin.Pair;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class AbstractGetter implements Getter {
    private final Parameter param;

    protected AbstractGetter(Parameter param) {
        this.param = param;
    }

    // maps
    private static final Map<Parameter, ParameterInfo> parametersMap = new HashMap<>();

    private static final Map<Type, Pair<Constructor<?>, ConstructorParameterInfo[]>> complexParametersMap = new HashMap<>();

    private static final Map<Class<?>, Function<String, Object>> wrapperConvertersMap = Map.of(
            Boolean.class, Boolean::parseBoolean,
            Byte.class, Byte::parseByte,
            Short.class, Short::parseShort,
            Integer.class, Integer::parseInt,
            Long.class, Long::parseLong,
            Float.class, Float::parseFloat,
            Double.class, Double::parseDouble,
            Character.class, s -> s.charAt(0)
    );

    private static final Map<Class<?>, Function<String, Object>> primitiveConvertersMap = Map.of(
            boolean.class, Boolean::parseBoolean,
            byte.class, Byte::parseByte,
            short.class, Short::parseShort,
            int.class, Integer::parseInt,
            long.class, Long::parseLong,
            float.class, Float::parseFloat,
            double.class, Double::parseDouble,
            char.class, s -> s.charAt(0)
    );

    protected Object getValueFromMap(Map<String, String> map) {
        ParameterInfo paramInfo = loadParameterInfo(param);
        String stringValue = map.get(paramInfo.paramName());
        return convertValueToType(paramInfo.converterType(), stringValue, map);
    }

    private ParameterInfo loadParameterInfo(Parameter param) {
        return parametersMap.computeIfAbsent(param, key -> new ParameterInfo(key.getName(), key.getType()));
    }

    private Object convertValueToType(Class<?> type, String stringValue, Map<String, String> map) {
        if (type == String.class) {
            return stringValue;
        } else {
            // Try to convert the string to a primitive type
            Object value = convertStringToPrimitive(type, stringValue);
            if (value != null) {
                return value;
            } else {
                // Parameter is of a complex type
                Pair<Constructor<?>, ConstructorParameterInfo[]> complexTypeInfo =
                        complexParametersMap.computeIfAbsent(type, k -> loadComplexTypeInfo(type));
                Constructor<?> ctor = complexTypeInfo.getFirst();
                ConstructorParameterInfo[] ctorParamsInfoArray = complexTypeInfo.getSecond();
                try {
                    return createComplexInstance(ctor, map, ctorParamsInfoArray);
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    // loads the constructor and its parameters' info using reflection
    private Pair<Constructor<?>, ConstructorParameterInfo[]> loadComplexTypeInfo(Class<?> type) {
        Constructor<?>[] constructors = type.getDeclaredConstructors();
        if (constructors.length == 0) {
            throw new RuntimeException("The class " + type.getName() + " must have one constructor");
        }
        Constructor<?> constructor = constructors[0];
        ConstructorParameterInfo[] constructorParamsInfo = new ConstructorParameterInfo[constructor.getParameterCount()];
        int i = 0;
        for (Parameter ctorParam : constructor.getParameters()) {
            constructorParamsInfo[i++] = new ConstructorParameterInfo(ctorParam.getName(), ctorParam.getType());
        }
        constructor.setAccessible(true);
        return new Pair<>(constructor, constructorParamsInfo);
    }

    private Object convertStringToPrimitive(Class<?> type, String stringValue) {
        Function<String, Object> converter = type.isPrimitive()
                ? primitiveConvertersMap.get(type)
                : wrapperConvertersMap.get(type);
        return converter != null ? converter.apply(stringValue) : null;
    }

    private Object createComplexInstance(
            Constructor<?> constructor,
            Map<String, String> map,
            ConstructorParameterInfo[] constructorParamsInfo
    ) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        List<Object> args = new ArrayList<>();
        for (ConstructorParameterInfo ctorParamInfo : constructorParamsInfo) {
            String paramValue = map.get(ctorParamInfo.paramName());
            Class<?> paramType = ctorParamInfo.converterType();
            args.add(convertValueToType(paramType, paramValue, map));

        }
        return constructor.newInstance(args.toArray());
    }
}