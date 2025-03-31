package dad.ConexionCamara;


import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class RestClientLocal extends AbstractVerticle {

    private RestClientUtil restClientUtil;

    public void start(Promise<Void> startFuture) {
        WebClientOptions options = new WebClientOptions().setUserAgent("RestClientApp/2.0.2.1");
        options.setKeepAlive(false);
        restClientUtil = new RestClientUtil(WebClient.create(vertx, options));

        /*
         * Operaciones para CamaraDeteccion
         */
        testCamaraDeteccionOperations();

        /*
         * Operaciones para EstadoActuador
         */
        testEstadoActuadorOperations();

        /*
         * Operaciones combinadas
         */
        testCombinedOperations();
    }

    private void testCamaraDeteccionOperations() {
        // GET all detecciones
        Promise<CamaraDeteccion[]> resListDetecciones = Promise.promise();
        resListDetecciones.future().onComplete(complete -> {
            System.out.println("-----------------------------------------------------------");
            if (complete.succeeded()) {
                System.out.println("Lista de detecciones obtenida");
                if (complete.result() != null) {
                    for (CamaraDeteccion deteccion : complete.result()) {
                        System.out.println(deteccion.toString());
                    }
                } else {
                    System.out.println("Cuerpo vacío");
                }
            } else {
                System.out.println("Error al obtener lista de detecciones");
                System.out.println(complete.cause().toString());
            }
        });
        restClientUtil.getRequest(8080, "http://localhost", "api/detecciones", CamaraDeteccion[].class, resListDetecciones);

        // POST nueva detección
        Promise<CamaraDeteccion> resPostDeteccion = Promise.promise();
        resPostDeteccion.future().onComplete(complete -> {
            System.out.println("-----------------------------------------------------------");
            if (complete.succeeded()) {
                System.out.println("Detección añadida");
                if (complete.result() != null) {
                    System.out.println(complete.result().toString());
                } else {
                    System.out.println("Cuerpo vacío");
                }
            } else {
                System.out.println("Error al añadir detección");
                System.out.println(complete.cause().toString());
            }
        });

        CamaraDeteccion nuevaDeteccion = new CamaraDeteccion(
            100,  // Este ID será sobrescrito por el servidor si se configura auto-incremental
            1,    // id_camara
            5,    // num_personas
            "Lunes", // dia_semana
            25,   // contador_semanal
            new Timestamp(System.currentTimeMillis()) // timestamp
        );
        restClientUtil.postRequest(8080, "http://localhost", "api/detecciones", 
                nuevaDeteccion, CamaraDeteccion.class, resPostDeteccion);

        // GET detección específica
        Promise<CamaraDeteccion> resDeteccion = Promise.promise();
        resDeteccion.future().onComplete(complete -> {
            System.out.println("-----------------------------------------------------------");
            if (complete.succeeded()) {
                System.out.println("Detección obtenida");
                if (complete.result() != null) {
                    System.out.println(complete.result().toString());
                } else {
                    System.out.println("Cuerpo vacío");
                }
            } else {
                System.out.println("Error al obtener detección");
                System.out.println(complete.cause().toString());
            }
        });
        restClientUtil.getRequest(8080, "http://localhost", "api/detecciones/1", CamaraDeteccion.class, resDeteccion);

        // GET detecciones con parámetros
        Promise<CamaraDeteccion[]> resDeteccionesParams = Promise.promise();
        resDeteccionesParams.future().onComplete(complete -> {
            System.out.println("-----------------------------------------------------------");
            if (complete.succeeded()) {
                System.out.println("Detecciones con parámetros obtenidas");
                if (complete.result() != null) {
                    for (CamaraDeteccion deteccion : complete.result()) {
                        System.out.println(deteccion.toString());
                    }
                } else {
                    System.out.println("Cuerpo vacío");
                }
            } else {
                System.out.println("Error al obtener detecciones con parámetros");
                System.out.println(complete.cause().toString());
            }
        });
        Map<String, String> paramsDetecciones = new HashMap<>();
        paramsDetecciones.put("dia_semana", "Lunes");
        restClientUtil.getRequestWithParams(8080, "http://localhost", "api/detecciones", 
                CamaraDeteccion[].class, resDeteccionesParams, paramsDetecciones);

        // DELETE detección
        Promise<String> resDeleteDeteccion = Promise.promise();
        resDeleteDeteccion.future().onComplete(complete -> {
            System.out.println("-----------------------------------------------------------");
            if (complete.succeeded()) {
                System.out.println("Detección eliminada");
                if (complete.result() != null) {
                    System.out.println(complete.result().toString());
                } else {
                    System.out.println("Cuerpo vacío");
                }
            } else {
                System.out.println("Error al eliminar detección");
                System.out.println(complete.cause().toString());
            }
        });
        restClientUtil.deleteRequest(8080, "http://localhost", "api/detecciones/1", resDeleteDeteccion);
    }

    private void testEstadoActuadorOperations() {
        // GET all estados actuador
        Promise<EstadoActuador[]> resListEstados = Promise.promise();
        resListEstados.future().onComplete(complete -> {
            System.out.println("-----------------------------------------------------------");
            if (complete.succeeded()) {
                System.out.println("Lista de estados de actuador obtenida");
                if (complete.result() != null) {
                    for (EstadoActuador estado : complete.result()) {
                        System.out.println(estado.toString());
                    }
                } else {
                    System.out.println("Cuerpo vacío");
                }
            } else {
                System.out.println("Error al obtener lista de estados de actuador");
                System.out.println(complete.cause().toString());
            }
        });
        restClientUtil.getRequest(8080, "http://localhost", "api/estados-actuador", EstadoActuador[].class, resListEstados);

        // POST nuevo estado actuador
        Promise<EstadoActuador> resPostEstado = Promise.promise();
        resPostEstado.future().onComplete(complete -> {
            System.out.println("-----------------------------------------------------------");
            if (complete.succeeded()) {
                System.out.println("Estado de actuador añadido");
                if (complete.result() != null) {
                    System.out.println(complete.result().toString());
                } else {
                    System.out.println("Cuerpo vacío");
                }
            } else {
                System.out.println("Error al añadir estado de actuador");
                System.out.println(complete.cause().toString());
            }
        });

        EstadoActuador nuevoEstado = new EstadoActuador(
            100,  // Este ID será sobrescrito por el servidor si se configura auto-incremental
            1,    // id_actuador
            true, // estado
            new Timestamp(System.currentTimeMillis()) // timestamp
        );
        restClientUtil.postRequest(8080, "http://localhost", "api/estados-actuador", 
                nuevoEstado, EstadoActuador.class, resPostEstado);

        // PUT actualizar estado actuador
        Promise<EstadoActuador> resPutEstado = Promise.promise();
        resPutEstado.future().onComplete(complete -> {
            System.out.println("-----------------------------------------------------------");
            if (complete.succeeded()) {
                System.out.println("Estado de actuador actualizado");
                if (complete.result() != null) {
                    System.out.println(complete.result().toString());
                } else {
                    System.out.println("Cuerpo vacío");
                }
            } else {
                System.out.println("Error al actualizar estado de actuador");
                System.out.println(complete.cause().toString());
            }
        });

        EstadoActuador estadoActualizado = new EstadoActuador(
            2,    // id
            1,    // id_actuador
            false, // estado
            new Timestamp(System.currentTimeMillis()) // timestamp
        );
        restClientUtil.putRequest(8080, "http://localhost", "api/estados-actuador/2", 
                estadoActualizado, EstadoActuador.class, resPutEstado);
    }

    private void testCombinedOperations() {
        // Operaciones combinadas
        Promise<CamaraDeteccion> resPostDeteccion1 = Promise.promise();
        Promise<CamaraDeteccion> resPostDeteccion2 = Promise.promise();
        Promise<EstadoActuador> resPostEstado1 = Promise.promise();
        Promise<EstadoActuador> resPostEstado2 = Promise.promise();

        // Crear varias detecciones
        restClientUtil.postRequest(8080, "http://localhost", "api/detecciones",
                new CamaraDeteccion(0, 1, 3, "Martes", 15, new Timestamp(System.currentTimeMillis())),
                CamaraDeteccion.class, resPostDeteccion1);
        
        restClientUtil.postRequest(8080, "http://localhost", "api/detecciones",
                new CamaraDeteccion(0, 2, 7, "Miércoles", 30, new Timestamp(System.currentTimeMillis())),
                CamaraDeteccion.class, resPostDeteccion2);

        // Crear varios estados de actuador
        restClientUtil.postRequest(8080, "http://localhost", "api/estados-actuador",
                new EstadoActuador(0, 1, true, new Timestamp(System.currentTimeMillis())),
                EstadoActuador.class, resPostEstado1);
        
        restClientUtil.postRequest(8080, "http://localhost", "api/estados-actuador",
                new EstadoActuador(0, 2, false, new Timestamp(System.currentTimeMillis())),
                EstadoActuador.class, resPostEstado2);

        // Combinar todas las operaciones
        CompositeFuture.all(resPostDeteccion1.future(), resPostDeteccion2.future(),
                           resPostEstado1.future(), resPostEstado2.future())
                .onComplete(new Handler<AsyncResult<CompositeFuture>>() {
                    @Override
                    public void handle(AsyncResult<CompositeFuture> event) {
                        System.out.println("-----------------------------------------------------------");
                        System.out.println("Resultados de operaciones combinadas:");
                        System.out.println("Detección 1: " + resPostDeteccion1.future().result());
                        System.out.println("Detección 2: " + resPostDeteccion2.future().result());
                        System.out.println("Estado 1: " + resPostEstado1.future().result());
                        System.out.println("Estado 2: " + resPostEstado2.future().result());
                    }
                });
    }
}