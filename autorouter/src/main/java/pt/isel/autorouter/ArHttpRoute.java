package pt.isel.autorouter;

// bring one more information if it is a sequence of sequence of string or not
// if it has more one param, then we need to change some things in api ("Make a proposal and modify the api")
public record ArHttpRoute(
        String funName,
        ArVerb method,
        String path,
        ArHttpHandler handler
) {
}
