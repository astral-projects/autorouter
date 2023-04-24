package pt.isel.examples;

import pt.isel.autorouter.ArHttpHandler;
import pt.isel.classroom.ClassroomController;

import java.util.Map;
import java.util.Optional;

/**
 * Um ciclo que gera codigo, quantas vezes quantos campos temos
 * Ciclo esta nos parametros do handler, 'para cada' temos de fzer um get do mapa
 *                                         foreach
 * BaseLine a imagem do codigo que queiramos gerar, ter muitas intruções para poder identificar o que queremos fazer
 * Sugestao:
 * Separar tudo de forma a ficar perceptivel
 */

/**
 * Para cada metodo do controller vamos gerar uma class diferente. Quantas? Nao sabemos no futuro
 */
public class HttpHandlerSearch implements ArHttpHandler {
    private final ClassroomController router;

    public HttpHandlerSearch(ClassroomController router) {
        this.router = router;
    }

    @Override
    public Optional<?> handle(Map<String, String> routeArgs, Map<String, String> queryArgs, Map<String, String> bodyArgs) {

        // Get the classroom
        String Classroom = routeArgs.get("classroom");

        // Get the student
        String Student = queryArgs.get("student");

        // Search for the student
        return router.search(Classroom, Student);
    }
}


