package pt.isel.autorouter.getters;

import java.lang.reflect.Parameter;
import java.util.Map;

public class RouteArgsGetter extends AbstractGetter {

    public RouteArgsGetter(Parameter param) {
        super(param);
    }

    @Override
    public Object getArgValue(Map<String, String> routeArgs, Map<String, String> queryArgs, Map<String, String> bodyArgs) {
        return getValueFromMap(routeArgs);
    }
}
