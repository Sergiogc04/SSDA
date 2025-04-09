package dad.ConexionCamara;

import dad.ConexionCamara.Api.RestServer_1;
import dad.ConexionCamara.mqtt.MqttClientVerticle;
import io.vertx.core.Vertx;

public class App {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DatabaseService dbService = new DatabaseService(vertx);

        // Desplegar componentes
        vertx.deployVerticle(new RestServer_1(dbService))
            .compose(serverId -> vertx.deployVerticle(new MqttClientVerticle()))
            .onSuccess(res -> {
                System.out.println("✅ Sistema iniciado correctamente");
                System.out.println("📌 Servicios activos:");
                System.out.println("  - REST API en puerto 8080");
                System.out.println("  - Cliente MQTT conectado");
            })
            .onFailure(err -> {
                System.err.println("❌ Error al iniciar sistema: " + err.getMessage());
                System.exit(1);
            });
    }
}