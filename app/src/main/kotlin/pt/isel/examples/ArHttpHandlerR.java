package pt.isel.examples;

import java.util.Map;
import java.util.Optional;

@FunctionalInterface
interface ArHttpHandlerR {
    Optional<?> handle(
            Map<String, String> routeArgs,
            Map<String, String> queryArgs,
            Map<String, String> bodyArgs
    );
}
