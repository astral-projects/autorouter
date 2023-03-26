package pt.isel.autorouter;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;

@FunctionalInterface
public interface ArHttpHandler {
    Optional<?> handle(
        Map<String, String> routeArgs,
        Map<String, String> queryArgs,
        Map<String, String> bodyArgs
    ) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;
}