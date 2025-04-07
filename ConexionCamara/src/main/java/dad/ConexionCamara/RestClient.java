package dad.ConexionCamara;

import dad.ConexionCamara.Entities.CamaraDeteccion;
import dad.ConexionCamara.Entities.EstadoActuador;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class RestClient extends AbstractVerticle {

    public RestClientUtil restClientUtil;

    public void start(Promise<Void> startFuture) {
        WebClientOptions options = new WebClientOptions().setUserAgent("RestClientApp/2.0.2.1");
        options.setKeepAlive(false);
        restClientUtil = new RestClientUtil(WebClient.create(vertx, options));

        /* --------------- Operaciones para Detecciones por Cámara --------------- */
        Promise<CamaraDeteccion[]> resDeteccionesCamara = Promise.promise();
        resDeteccionesCamara.future().onComplete(complete -> {
            if (complete.succeeded()) {
                System.out.println("Detecciones por Cámara (ID=1):");
                for (CamaraDeteccion deteccion : complete.result()) {
                    System.out.println(deteccion.toString());
                }
            } else {
                System.out.println("Error al obtener detecciones: " + complete.cause().toString());
            }
        });
        restClientUtil.getRequest(8080, "http://localhost", "api/detecciones/1", 
                CamaraDeteccion[].class, resDeteccionesCamara);

        /* --------------- Operaciones para Estado de Actuador --------------- */
        Promise<EstadoActuador> resEstadoActuador = Promise.promise();
        resEstadoActuador.future().onComplete(complete -> {
            if (complete.succeeded()) {
                System.out.println("Estado del Actuador (ID=1):");
                System.out.println(complete.result().toString());
            } else {
                System.out.println("Error al obtener estado del actuador: " + complete.cause().toString());
            }
        });
        restClientUtil.getRequest(8080, "http://localhost", "api/estados-actuador/1", 
                EstadoActuador.class, resEstadoActuador);

        /* --------------- Operaciones para Alertas de Parada Llena --------------- */
        Promise<JsonObject> resAlertaParada = Promise.promise();
        resAlertaParada.future().onComplete(complete -> {
            if (complete.succeeded()) {
                System.out.println("Alerta de Parada Llena (ID Actuador=1):");
                System.out.println(complete.result().toString());
            } else {
                System.out.println("Error al obtener alerta de parada: " + complete.cause().toString());
            }
        });
        restClientUtil.getRequest(8080, "http://localhost", "api/alertas/parada-llena/1", 
                JsonObject.class, resAlertaParada);
    }
}