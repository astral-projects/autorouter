package pt.isel.autorouter;

// TODO("alter this type to enable sequences of sequences of routes")
public record ArHttpRoute(
        String funName,
        ArVerb method,
        String path, //
        ArHttpHandler handler
) {
}
