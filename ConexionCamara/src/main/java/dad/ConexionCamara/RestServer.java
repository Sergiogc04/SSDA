package dad.ConexionCamara;



import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class RestServer extends AbstractVerticle {

    private Map<Integer, CamaraDeteccion> detecciones = new HashMap<>();
    private Map<Integer, EstadoActuador> estadosActuador = new HashMap<>();
    private Gson gson;

    public void start(Promise<Void> startFuture) {
        // Creating some synthetic data
        createSomeData(10);

        // Instantiating a Gson serialize object using specific date format
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

        // Defining the router object
        Router router = Router.router(vertx);

        // Handling any server startup result
        vertx.createHttpServer().requestHandler(router::handle).listen(8080, result -> {
            if (result.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(result.cause());
            }
        });

        // CamaraDeteccion endpoints
        router.route("/api/detecciones*").handler(BodyHandler.create());
        router.get("/api/detecciones").handler(this::getAllDetecciones);
        router.get("/api/detecciones/:id").handler(this::getDeteccionById);
        router.post("/api/detecciones").handler(this::addDeteccion);
        router.delete("/api/detecciones/:id").handler(this::deleteDeteccion);
        router.put("/api/detecciones/:id").handler(this::updateDeteccion);

        // EstadoActuador endpoints
        router.route("/api/estados-actuador*").handler(BodyHandler.create());
        router.get("/api/estados-actuador").handler(this::getAllEstadosActuador);
        router.get("/api/estados-actuador/:id").handler(this::getEstadoActuadorById);
        router.post("/api/estados-actuador").handler(this::addEstadoActuador);
        router.delete("/api/estados-actuador/:id").handler(this::deleteEstadoActuador);
        router.put("/api/estados-actuador/:id").handler(this::updateEstadoActuador);
    }

    // CamaraDeteccion handlers
    private void getAllDetecciones(RoutingContext routingContext) {
        routingContext.response()
            .putHeader("content-type", "application/json; charset=utf-8")
            .setStatusCode(200)
            .end(gson.toJson(detecciones.values()));
    }

    private void getDeteccionById(RoutingContext routingContext) {
        int id = Integer.parseInt(routingContext.request().getParam("id"));
        if (detecciones.containsKey(id)) {
            routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .setStatusCode(200)
                .end(gson.toJson(detecciones.get(id)));
        } else {
            routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .setStatusCode(404)
                .end();
        }
    }

    private void addDeteccion(RoutingContext routingContext) {
        final CamaraDeteccion deteccion = gson.fromJson(routingContext.getBodyAsString(), CamaraDeteccion.class);
        detecciones.put(deteccion.getId(), deteccion);
        routingContext.response()
            .setStatusCode(201)
            .putHeader("content-type", "application/json; charset=utf-8")
            .end(gson.toJson(deteccion));
    }

    private void deleteDeteccion(RoutingContext routingContext) {
        int id = Integer.parseInt(routingContext.request().getParam("id"));
        if (detecciones.containsKey(id)) {
            CamaraDeteccion deteccion = detecciones.remove(id);
            routingContext.response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(gson.toJson(deteccion));
        } else {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end();
        }
    }

    private void updateDeteccion(RoutingContext routingContext) {
        int id = Integer.parseInt(routingContext.request().getParam("id"));
        if (detecciones.containsKey(id)) {
            CamaraDeteccion deteccion = gson.fromJson(routingContext.getBodyAsString(), CamaraDeteccion.class);
            detecciones.put(id, deteccion);
            routingContext.response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(gson.toJson(deteccion));
        } else {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end();
        }
    }

    // EstadoActuador handlers
    private void getAllEstadosActuador(RoutingContext routingContext) {
        routingContext.response()
            .putHeader("content-type", "application/json; charset=utf-8")
            .setStatusCode(200)
            .end(gson.toJson(estadosActuador.values()));
    }

    private void getEstadoActuadorById(RoutingContext routingContext) {
        int id = Integer.parseInt(routingContext.request().getParam("id"));
        if (estadosActuador.containsKey(id)) {
            routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .setStatusCode(200)
                .end(gson.toJson(estadosActuador.get(id)));
        } else {
            routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .setStatusCode(404)
                .end();
        }
    }

    private void addEstadoActuador(RoutingContext routingContext) {
        final EstadoActuador estado = gson.fromJson(routingContext.getBodyAsString(), EstadoActuador.class);
        estadosActuador.put(estado.getId(), estado);
        routingContext.response()
            .setStatusCode(201)
            .putHeader("content-type", "application/json; charset=utf-8")
            .end(gson.toJson(estado));
    }

    private void deleteEstadoActuador(RoutingContext routingContext) {
        int id = Integer.parseInt(routingContext.request().getParam("id"));
        if (estadosActuador.containsKey(id)) {
            EstadoActuador estado = estadosActuador.remove(id);
            routingContext.response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(gson.toJson(estado));
        } else {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end();
        }
    }

    private void updateEstadoActuador(RoutingContext routingContext) {
        int id = Integer.parseInt(routingContext.request().getParam("id"));
        if (estadosActuador.containsKey(id)) {
            EstadoActuador estado = gson.fromJson(routingContext.getBodyAsString(), EstadoActuador.class);
            estadosActuador.put(id, estado);
            routingContext.response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(gson.toJson(estado));
        } else {
            routingContext.response()
                .setStatusCode(404)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end();
        }
    }

    private void createSomeData(int number) {
        Random rnd = new Random();
        String[] diasSemana = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        
        IntStream.range(0, number).forEach(elem -> {
            int id = elem + 1;
            // Create CamaraDeteccion data
            detecciones.put(id, new CamaraDeteccion(
                id,
                rnd.nextInt(10) + 1, // id_camara
                rnd.nextInt(20),     // num_personas
                diasSemana[rnd.nextInt(diasSemana.length)], // dia_semana
                rnd.nextInt(100),    // contador_semanal
                new Timestamp(System.currentTimeMillis())   // timestamp
            ));
            
            // Create EstadoActuador data
            estadosActuador.put(id, new EstadoActuador(
                id,
                rnd.nextInt(10) + 1, // id_actuador
                rnd.nextBoolean(),   // estado
                new Timestamp(System.currentTimeMillis())   // timestamp
            ));
        });
    }
}
