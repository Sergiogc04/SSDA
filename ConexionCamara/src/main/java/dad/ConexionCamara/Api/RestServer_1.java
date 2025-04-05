package dad.ConexionCamara.Api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import java.time.LocalDateTime;

import dad.ConexionCamara.DatabaseService;

public class RestServer_1 extends AbstractVerticle {
    private final DatabaseService dbService;
    
    public RestServer_1(DatabaseService dbService) {
        this.dbService = dbService;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        Router router = Router.router(vertx);
        
        // Middleware para parsear JSON
        router.route().handler(BodyHandler.create());
        
        // Endpoints existentes
        router.get("/api/camaras").handler(this::handleGetCamaras);
        router.get("/api/detecciones").handler(this::handleGetAllDetecciones);
        router.get("/api/detecciones/:idCamara").handler(this::handleGetDeteccionesByCamara);
        router.post("/api/detecciones").handler(this::handleAddDeteccion);
        router.get("/api/estados-actuador/:idActuador").handler(this::handleGetEstadoActuador);
        router.post("/api/estados-actuador").handler(this::handleAddEstadoActuador);
        
        // Nuevo endpoint para alertas de paradas llenas
        router.get("/api/alertas/parada-llena/:idActuador").handler(this::handleAlertaParadaLlena);
        
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080)
            .onSuccess(server -> {
                System.out.println("Servidor iniciado en puerto 8080");
                startPromise.complete();
            })
            .onFailure(startPromise::fail);
    }

    // --- Métodos existentes (sin cambios) ---
    private void handleGetCamaras(RoutingContext ctx) {
        dbService.getCamaras()
            .onSuccess(camaras -> sendJsonResponse(ctx, 200, camaras))
            .onFailure(err -> sendErrorResponse(ctx, 500, err.getMessage()));
    }

    private void handleGetAllDetecciones(RoutingContext ctx) {
        dbService.getDeteccionesByCamara(null)
            .onSuccess(detecciones -> sendJsonResponse(ctx, 200, detecciones))
            .onFailure(err -> sendErrorResponse(ctx, 500, err.getMessage()));
    }

    private void handleGetDeteccionesByCamara(RoutingContext ctx) {
        try {
            int idCamara = Integer.parseInt(ctx.pathParam("idCamara"));
            dbService.getDeteccionesByCamara(idCamara)
                .onSuccess(detecciones -> sendJsonResponse(ctx, 200, detecciones))
                .onFailure(err -> sendErrorResponse(ctx, 500, err.getMessage()));
        } catch (NumberFormatException e) {
            sendErrorResponse(ctx, 400, "ID de cámara inválido");
        }
    }

    private void handleAddDeteccion(RoutingContext ctx) {
        try {
            JsonObject deteccion = ctx.getBodyAsJson();
            if (deteccion.getInteger("id_camara") == null || 
                deteccion.getInteger("num_personas") == null) {
                sendErrorResponse(ctx, 400, "Campos requeridos: id_camara, num_personas");
                return;
            }
            
            dbService.insertDeteccion(deteccion)
                .onSuccess(result -> sendJsonResponse(ctx, 201, result))
                .onFailure(err -> sendErrorResponse(ctx, 500, err.getMessage()));
        } catch (Exception e) {
            sendErrorResponse(ctx, 400, "Formato de datos inválido");
        }
    }

    private void handleGetEstadoActuador(RoutingContext ctx) {
        try {
            int idActuador = Integer.parseInt(ctx.pathParam("idActuador"));
            dbService.getLastEstadoActuador(idActuador)
                .onSuccess(estado -> sendJsonResponse(ctx, 200, estado))
                .onFailure(err -> {
                    if (err.getMessage().contains("No se encontraron")) {
                        sendErrorResponse(ctx, 404, err.getMessage());
                    } else {
                        sendErrorResponse(ctx, 500, err.getMessage());
                    }
                });
        } catch (NumberFormatException e) {
            sendErrorResponse(ctx, 400, "ID de actuador inválido");
        }
    }

    private void handleAddEstadoActuador(RoutingContext ctx) {
        try {
            JsonObject estado = ctx.getBodyAsJson();
            if (estado.getInteger("id_actuador") == null || 
                estado.getBoolean("estado") == null) {
                sendErrorResponse(ctx, 400, "Campos requeridos: id_actuador, estado");
                return;
            }
            
            dbService.insertEstadoActuador(estado)
                .onSuccess(result -> sendJsonResponse(ctx, 201, result))
                .onFailure(err -> sendErrorResponse(ctx, 500, err.getMessage()));
        } catch (Exception e) {
            sendErrorResponse(ctx, 400, "Formato de datos inválido");
        }
    }

    // --- Nuevo método para alertas de paradas llenas ---
    private void handleAlertaParadaLlena(RoutingContext ctx) {
        try {
            int idActuador = Integer.parseInt(ctx.pathParam("idActuador"));
            
            dbService.getLastEstadoActuador(idActuador)
                .onSuccess(estado -> {
                    if (estado.getBoolean("estado")) {
                        // Crear objeto de alerta
                        JsonObject alerta = new JsonObject()
                            .put("idActuador", idActuador)
                            .put("mensaje", "¡Alerta! La parada está llena (aforo máximo alcanzado)")
                            .put("timestamp", LocalDateTime.now().toString())
                            .put("accionRecomendada", "Redirigir buses a esta parada");
                        
                        sendJsonResponse(ctx, 200, alerta);
                    } else {
                        sendJsonResponse(ctx, 200, 
                            new JsonObject()
                                .put("idActuador", idActuador)
                                .put("estado", false)
                                .put("mensaje", "La parada opera normalmente"));
                    }
                })
                .onFailure(err -> sendErrorResponse(ctx, 500, "Error al verificar el estado: " + err.getMessage()));
        } catch (NumberFormatException e) {
            sendErrorResponse(ctx, 400, "ID de actuador inválido");
        }
    }

    // --- Métodos auxiliares (sin cambios) ---
    private void sendJsonResponse(RoutingContext ctx, int statusCode, Object data) {
        if (data instanceof JsonObject || data instanceof JsonArray) {
            ctx.response()
                .setStatusCode(statusCode)
                .putHeader("Content-Type", "application/json")
                .end(data.toString());
        } else {
            sendErrorResponse(ctx, 500, "Tipo de dato no soportado para JSON");
        }
    }

    private void sendErrorResponse(RoutingContext ctx, int statusCode, String message) {
        ctx.response()
            .setStatusCode(statusCode)
            .putHeader("Content-Type", "application/json")
            .end(new JsonObject().put("error", message).encode());
    }
}