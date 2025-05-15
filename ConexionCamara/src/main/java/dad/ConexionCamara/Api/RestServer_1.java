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
        
        // Endpoints para Cámaras
        router.get("/api/camara").handler(this::handleGetCamaras);
        router.get("/api/camara/:id").handler(this::handleGetCamaraById);
        router.post("/api/camara").handler(this::handleAddCamara);
        router.put("/api/camara/:id").handler(this::handleUpdateCamara);
        router.delete("/api/camara/:id").handler(this::handleDeleteCamara);
        
        // Endpoints para Actuadores
        router.get("/api/actuators/:id").handler(this::handleGetActuadorById);
        router.post("/api/actuators").handler(this::handleAddActuador);
        router.put("/api/actuators/:id").handler(this::handleUpdateActuador);
        router.delete("/api/actuators/:id").handler(this::handleDeleteActuador);
        
        // Endpoints para Grupos
        router.get("/api/groups").handler(this::handleGetGrupos);
        router.get("/api/groups/:id").handler(this::handleGetGrupoById);
        router.post("/api/groups").handler(this::handleAddGrupo);
        router.put("/api/groups/:id").handler(this::handleUpdateGrupo);
        router.delete("/api/groups/:id").handler(this::handleDeleteGrupo);
        
        // Endpoints existentes para detecciones y estados de actuador
        router.get("/api/detecciones").handler(this::handleGetAllDetecciones);
        router.get("/api/detecciones/:idCamara").handler(this::handleGetDeteccionesByCamara);
        router.post("/api/detecciones").handler(this::handleAddDeteccion);
        router.get("/api/estados-actuador/:idActuador").handler(this::handleGetEstadoActuador);
        router.post("/api/estados-actuador").handler(this::handleAddEstadoActuador);
        
       
        // Nuevo endpoint para alertas de paradas llenas
        router.get("/api/alertas/parada-llena/:idCamara").handler(this::handleAlertaParadaLlena);
        
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
            if (estado.getInteger("id_camara") == null || 
                estado.getBoolean("estado") == null) {
                sendErrorResponse(ctx, 400, "Campos requeridos: id_camara, estado");
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
            int idCamara = Integer.parseInt(ctx.pathParam("idCamara"));
            
            // Obtener las detecciones para la cámara específica
            dbService.getDeteccionesByCamara(idCamara)
                .compose(detecciones -> {
                    // Usamos una variable final dentro del lambda
                    final int numPersonas = detecciones.size() > 0 ? 
                        detecciones.getJsonObject(0).getInteger("num_personas", 0) : 0;
                    
                    // Obtener el estado del actuador asociado a esta cámara
                    return dbService.getLastEstadoActuador(idCamara)
                        .map(estado -> new JsonObject()
                            .put("numPersonas", numPersonas)
                            .put("estadoActuador", estado.getBoolean("estado", false)));
                })
                .onSuccess(respuesta -> {
                    ctx.response()
                       .putHeader("Content-Type", "application/json")
                       .end(respuesta.encode());
                })
                .onFailure(err -> {
                    ctx.response()
                       .setStatusCode(500)
                       .end(new JsonObject().put("error", err.getMessage()).encode());
                });
        } catch (NumberFormatException e) {
            ctx.response()
               .setStatusCode(400)
               .end(new JsonObject().put("error", "ID de cámara inválido").encode());
        }
    }

    
 // --- Métodos para Grupos ---
    private void handleGetGrupos(RoutingContext ctx) {
        dbService.getGrupos()
            .onSuccess(grupos -> sendJsonResponse(ctx, 200, grupos))
            .onFailure(err -> sendErrorResponse(ctx, 500, err.getMessage()));
    }

    private void handleGetGrupoById(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            dbService.getGrupoById(id)
                .onSuccess(grupo -> sendJsonResponse(ctx, 200, grupo))
                .onFailure(err -> {
                    if (err.getMessage().contains("No se encontró")) {
                        sendErrorResponse(ctx, 404, err.getMessage());
                    } else {
                        sendErrorResponse(ctx, 500, err.getMessage());
                    }
                });
        } catch (NumberFormatException e) {
            sendErrorResponse(ctx, 400, "ID de grupo inválido");
        }
    }

    private void handleAddGrupo(RoutingContext ctx) {
        try {
            JsonObject grupo = ctx.getBodyAsJson();
            if (grupo.getString("nombre") == null) {
                sendErrorResponse(ctx, 400, "Campo requerido: nombre");
                return;
            }
            
            dbService.insertGrupo(grupo)
                .onSuccess(result -> sendJsonResponse(ctx, 201, result))
                .onFailure(err -> sendErrorResponse(ctx, 500, err.getMessage()));
        } catch (Exception e) {
            sendErrorResponse(ctx, 400, "Formato de datos inválido");
        }
    }

    private void handleUpdateGrupo(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            JsonObject grupo = ctx.getBodyAsJson();
            
            dbService.updateGrupo(id, grupo)
                .onSuccess(v -> sendJsonResponse(ctx, 200, new JsonObject().put("message", "Grupo actualizado")))
                .onFailure(err -> sendErrorResponse(ctx, 500, err.getMessage()));
        } catch (NumberFormatException e) {
            sendErrorResponse(ctx, 400, "ID de grupo inválido");
        } catch (Exception e) {
            sendErrorResponse(ctx, 400, "Formato de datos inválido");
        }
    }

    private void handleDeleteGrupo(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            
            dbService.deleteGrupo(id)
                .onSuccess(v -> sendJsonResponse(ctx, 200, new JsonObject().put("message", "Grupo eliminado")))
                .onFailure(err -> sendErrorResponse(ctx, 500, err.getMessage()));
        } catch (NumberFormatException e) {
            sendErrorResponse(ctx, 400, "ID de grupo inválido");
        }
    }

    // --- Métodos para Actuadores ---
    private void handleGetActuadorById(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            // Necesitarías implementar getActuadorById en DatabaseService
            dbService.getActuadores()
                .onSuccess(actuadores -> {
                    for (Object obj : actuadores) {
                        JsonObject actuador = (JsonObject) obj;
                        if (actuador.getInteger("id").equals(id)) {
                            sendJsonResponse(ctx, 200, actuador);
                            return;
                        }
                    }
                    sendErrorResponse(ctx, 404, "Actuador no encontrado");
                })
                .onFailure(err -> sendErrorResponse(ctx, 500, err.getMessage()));
        } catch (NumberFormatException e) {
            sendErrorResponse(ctx, 400, "ID de actuador inválido");
        }
    }

    private void handleAddActuador(RoutingContext ctx) {
        try {
            JsonObject actuador = ctx.getBodyAsJson();
            if (actuador.getString("nombre") == null || actuador.getInteger("id_grupo") == null) {
                sendErrorResponse(ctx, 400, "Campos requeridos: nombre, id_grupo");
                return;
            }
            
            dbService.insertActuador(actuador)
                .onSuccess(result -> sendJsonResponse(ctx, 201, result))
                .onFailure(err -> sendErrorResponse(ctx, 500, err.getMessage()));
        } catch (Exception e) {
            sendErrorResponse(ctx, 400, "Formato de datos inválido");
        }
    }

    private void handleUpdateActuador(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            JsonObject actuador = ctx.getBodyAsJson();
            
            dbService.updateActuador(id, actuador)
                .onSuccess(v -> sendJsonResponse(ctx, 200, new JsonObject().put("message", "Actuador actualizado")))
                .onFailure(err -> sendErrorResponse(ctx, 500, err.getMessage()));
        } catch (NumberFormatException e) {
            sendErrorResponse(ctx, 400, "ID de actuador inválido");
        } catch (Exception e) {
            sendErrorResponse(ctx, 400, "Formato de datos inválido");
        }
    }

    private void handleDeleteActuador(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            
            dbService.deleteActuador(id)
                .onSuccess(v -> sendJsonResponse(ctx, 200, new JsonObject().put("message", "Actuador eliminado")))
                .onFailure(err -> sendErrorResponse(ctx, 500, err.getMessage()));
        } catch (NumberFormatException e) {
            sendErrorResponse(ctx, 400, "ID de actuador inválido");
        }
    }

    // --- Métodos para Cámaras ---
    private void handleGetCamaraById(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            dbService.getCamaraById(id)
                .onSuccess(camara -> sendJsonResponse(ctx, 200, camara))
                .onFailure(err -> {
                    if (err.getMessage().contains("No se encontró")) {
                        sendErrorResponse(ctx, 404, err.getMessage());
                    } else {
                        sendErrorResponse(ctx, 500, err.getMessage());
                    }
                });
        } catch (NumberFormatException e) {
            sendErrorResponse(ctx, 400, "ID de cámara inválido");
        }
    }

    private void handleAddCamara(RoutingContext ctx) {
        try {
            JsonObject camara = ctx.getBodyAsJson();
            if (camara.getString("nombre") == null || camara.getInteger("id_grupo") == null) {
                sendErrorResponse(ctx, 400, "Campos requeridos: nombre, id_grupo");
                return;
            }
            
            dbService.insertCamara(camara)
                .onSuccess(result -> sendJsonResponse(ctx, 201, result))
                .onFailure(err -> sendErrorResponse(ctx, 500, err.getMessage()));
        } catch (Exception e) {
            sendErrorResponse(ctx, 400, "Formato de datos inválido");
        }
    }

    private void handleUpdateCamara(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            JsonObject camara = ctx.getBodyAsJson();
            
            dbService.updateCamara(id, camara)
                .onSuccess(v -> sendJsonResponse(ctx, 200, new JsonObject().put("message", "Cámara actualizada")))
                .onFailure(err -> sendErrorResponse(ctx, 500, err.getMessage()));
        } catch (NumberFormatException e) {
            sendErrorResponse(ctx, 400, "ID de cámara inválido");
        } catch (Exception e) {
            sendErrorResponse(ctx, 400, "Formato de datos inválido");
        }
    }

    private void handleDeleteCamara(RoutingContext ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            
            dbService.deleteCamara(id)
                .onSuccess(v -> sendJsonResponse(ctx, 200, new JsonObject().put("message", "Cámara eliminada")))
                .onFailure(err -> sendErrorResponse(ctx, 500, err.getMessage()));
        } catch (NumberFormatException e) {
            sendErrorResponse(ctx, 400, "ID de cámara inválido");
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