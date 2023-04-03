package pt.isel.autorouter.getters;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Map;

public class BodyArgsGetter extends AbstractGetter {

    public BodyArgsGetter(Parameter param) {
        super(param);
    }

    @Override
    public Object getArgValue(Map<String, String> routeArgs, Map<String, String> queryArgs, Map<String, String> bodyArgs) {
        try {
            return getValueFromMap(bodyArgs);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
