package pt.isel.autorouter;

// bring one more information if it is a sequence of sequence of string or not
// if it has more one param, then we need to change some things in api ("Make a proposal and modify the api")
public record ArHttpRoute(
        String funName,
        ArVerb method,
        String path,
        ArHttpHandler handler,
        Boolean isSequence
) {
}

// Object need to bring info that handler returns a Sequence<Sequence<String>>
// Do an if to check if it is a sequence of sequence of string and if it is need to be registed in different way
// How? See in moodle
// if it not do the normal thing
// Resume: Add more info from ArHttpRoute then came from annotations

// TODO("ArHttpRoute change annotations
// TODO(allow this implementation and other that enables the annotations
// RECORD DIZ se a o record traz um optional de sequencias de sequencias)

