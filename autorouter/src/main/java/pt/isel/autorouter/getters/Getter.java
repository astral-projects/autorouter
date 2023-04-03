package pt.isel.autorouter.getters;

import java.util.Map;

@FunctionalInterface
public interface Getter {
    Object getArgValue(Map<String, String> routeArgs, Map<String, String> queryArgs, Map<String, String> bodyArgs);
}
