package dad.ConexionCamara;


import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class RestClient extends AbstractVerticle {

    public RestClientUtil restClientUtil;

    public void start(Promise<Void> startFuture) {
        WebClientOptions options = new WebClientOptions().setUserAgent("RestClientApp/2.0.2.1");
        options.setKeepAlive(false);
        restClientUtil = new RestClientUtil(WebClient.create(vertx, options));

        /* --------------- Operaciones para CamaraDeteccion --------------- */

        // GET all detecciones
        Promise<CamaraDeteccion[]> resListDetecciones = Promise.promise();
        resListDetecciones.future().onComplete(complete -> {
            if (complete.succeeded()) {
                System.out.println("GetAll Detecciones:");
                Stream.of(complete.result()).forEach(elem -> {
                    System.out.println(elem.toString());
                });
            } else {
                System.out.println(complete.cause().toString());
            }
        });
        restClientUtil.getRequest(8080, "http://localhost", "api/detecciones", 
                CamaraDeteccion[].class, resListDetecciones);

        // GET detección específica
        Promise<CamaraDeteccion> resDeteccion = Promise.promise();
        resDeteccion.future().onComplete(complete -> {
            if (complete.succeeded()) {
                System.out.println("GetOne Detección:");
                System.out.println(complete.result().toString());
            } else {
                System.out.println(complete.cause().toString());
            }
        });
        restClientUtil.getRequest(8080, "http://localhost", "api/detecciones/1", 
                CamaraDeteccion.class, resDeteccion);

        // GET detecciones con parámetros
        Promise<CamaraDeteccion[]> resDeteccionesParams = Promise.promise();
        resDeteccionesParams.future().onComplete(complete -> {
            if (complete.succeeded()) {
                System.out.println("Get Detecciones con Parámetros:");
                Stream.of(complete.result()).forEach(elem -> {
                    System.out.println(elem.toString());
                });
            } else {
                System.out.println(complete.cause().toString());
            }
        });
        Map<String, String> params = new HashMap<>();
        params.put("dia_semana", "Lunes");
        params.put("num_personas_gt", "3");
        restClientUtil.getRequestWithParams(8080, "http://localhost", "api/detecciones", 
                CamaraDeteccion[].class, resDeteccionesParams, params);

        // POST nueva detección
        Promise<CamaraDeteccion> resPostDeteccion = Promise.promise();
        resPostDeteccion.future().onComplete(complete -> {
            if (complete.succeeded()) {
                System.out.println("Post Detección:");
                System.out.println(complete.result().toString());
            } else {
                System.out.println(complete.cause().toString());
            }
        });

        CamaraDeteccion nuevaDeteccion = new CamaraDeteccion(
            0,    // ID será generado por el servidor
            1,    // id_camara
            5,    // num_personas
            "Viernes", // dia_semana
            42,   // contador_semanal
            new Timestamp(System.currentTimeMillis()) // timestamp
        );
        restClientUtil.postRequest(8080, "http://localhost", "api/detecciones",
                nuevaDeteccion, CamaraDeteccion.class, resPostDeteccion);

        /* --------------- Operaciones para EstadoActuador --------------- */

        // GET all estados actuador
        Promise<EstadoActuador[]> resListEstados = Promise.promise();
        resListEstados.future().onComplete(complete -> {
            if (complete.succeeded()) {
                System.out.println("GetAll Estados Actuador:");
                Stream.of(complete.result()).forEach(elem -> {
                    System.out.println(elem.toString());
                });
            } else {
                System.out.println(complete.cause().toString());
            }
        });
        restClientUtil.getRequest(8080, "http://localhost", "api/estados-actuador", 
                EstadoActuador[].class, resListEstados);

        // GET estado actuador específico
        Promise<EstadoActuador> resEstado = Promise.promise();
        resEstado.future().onComplete(complete -> {
            if (complete.succeeded()) {
                System.out.println("GetOne Estado Actuador:");
                System.out.println(complete.result().toString());
            } else {
                System.out.println(complete.cause().toString());
            }
        });
        restClientUtil.getRequest(8080, "http://localhost", "api/estados-actuador/1", 
                EstadoActuador.class, resEstado);

        // POST nuevo estado actuador
        Promise<EstadoActuador> resPostEstado = Promise.promise();
        resPostEstado.future().onComplete(complete -> {
            if (complete.succeeded()) {
                System.out.println("Post Estado Actuador:");
                System.out.println(complete.result().toString());
            } else {
                System.out.println(complete.cause().toString());
            }
        });

        EstadoActuador nuevoEstado = new EstadoActuador(
            0,    // ID será generado por el servidor
            2,    // id_actuador
            true, // estado
            new Timestamp(System.currentTimeMillis()) // timestamp
        );
        restClientUtil.postRequest(8080, "http://localhost", "api/estados-actuador",
                nuevoEstado, EstadoActuador.class, resPostEstado);

        // PUT actualizar estado actuador
        Promise<EstadoActuador> resPutEstado = Promise.promise();
        resPutEstado.future().onComplete(complete -> {
            if (complete.succeeded()) {
                System.out.println("Put Estado Actuador:");
                System.out.println(complete.result().toString());
            } else {
                System.out.println(complete.cause().toString());
            }
        });

        EstadoActuador estadoActualizado = new EstadoActuador(
            1,    // id
            1,    // id_actuador
            false, // estado
            new Timestamp(System.currentTimeMillis()) // timestamp
        );
        restClientUtil.putRequest(8080, "http://localhost", "api/estados-actuador/1",
                estadoActualizado, EstadoActuador.class, resPutEstado);

        // DELETE estado actuador
        Promise<String> resDeleteEstado = Promise.promise();
        resDeleteEstado.future().onComplete(complete -> {
            if (complete.succeeded()) {
                System.out.println("Delete Estado Actuador:");
                System.out.println(complete.result().toString());
            } else {
                System.out.println(complete.cause().toString());
            }
        });

        restClientUtil.deleteRequest(8080, "http://localhost", "api/estados-actuador/2",
                resDeleteEstado);

        /* --------------- Iniciar servidor local --------------- */
        vertx.deployVerticle(RestServer.class.getName(), deploy -> {
            if (deploy.succeeded()) {
                System.out.println("Servidor desplegado correctamente");
            } else {
                System.out.println("Error al desplegar servidor: " + deploy.cause().toString());
            }
        });
    }
}