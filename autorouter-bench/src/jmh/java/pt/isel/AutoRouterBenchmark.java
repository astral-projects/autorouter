package pt.isel;

import org.openjdk.jmh.annotations.*;
import pt.isel.autorouter.ArHttpRoute;
import pt.isel.autorouter.AutoRouterDynamic;
import pt.isel.autorouter.AutoRouterReflect;
import pt.isel.classroom.ClassroomController;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class AutoRouterBenchmark {

    @Param({"reflect", "baseline","dynamic"}) String approach;
    @Param({"empty", "classroom"}) String domain;
    /*@Param({"reflect", "baseline"}) String approach;
    @Param({"empty"}) String domain;*/

    ArHttpRoute addStudentHandler;
    ArHttpRoute searchDynamicHandler;
    ArHttpRoute removeDynamicHandler;

    Stream<ArHttpRoute> routes() {
        Object controller = domain.equals("empty") ? new ClassroomControllerEmpty() : new ClassroomController();
        return switch (approach) {
            case "reflect" -> new AutoRouterReflect().autorouterReflect(controller);
            case "dynamic" -> new AutoRouterDynamic().autorouterDynamic(controller);
            case "baseline" -> domain.equals("empty")
                    ? ClassroomBaselineHandlers.routes(new ClassroomControllerEmpty())
                    : ClassroomBaselineHandlers.routes(new ClassroomController());
            default -> throw new IllegalArgumentException("No autrouter strategy for " + approach);
        };
    }

    @Setup
    public void setup(){
        addStudentHandler = routes().filter(r -> r.funName().equals("addStudent")).findFirst().get();
        searchDynamicHandler = routes().filter(r -> r.funName().equals("search")).findFirst().get();
        removeDynamicHandler = routes().filter(r -> r.funName().equals("removeStudent")).findFirst().get();
    }

    @Benchmark
    public Optional<?> addStudent() {
        return addStudentHandler.handler().handle(
                Map.of("classroom", "i42d", "nr", "7646775"),
                Collections.emptyMap(),
                Map.of("nr", "7646775","name", "Ze Gato", "group", "11","semester", "3")
        );
    }
    @Benchmark
    public Optional<?> removeStudent() {
        return removeDynamicHandler.handler().handle(
                Map.of("classroom", "i41d", "nr", "7236"),
                Collections.emptyMap(),
                Collections.emptyMap()
        );
    }

    @Benchmark
    public Optional<?> search() {
        return searchDynamicHandler.handler().handle(
                Map.of("classroom", "i42d"),
                Collections.emptyMap(),
                Collections.emptyMap()
        );
    }
}
