package pt.isel.autorouter.getters;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Map;

// classroom -> i41d // classroom -> i42d
public class RouteArgsGetter extends AbstractGetter {

    public RouteArgsGetter(Parameter param) {
        super(param);
    }

    @Override
    public Object getArgValue(Map<String, String> routeArgs, Map<String, String> queryArgs, Map<String, String> bodyArgs) {
        try {
            return getValueFromMap(routeArgs);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
