package dad.ConexionCamara.mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;

public class MqttClientVerticle extends AbstractVerticle {

    private MqttClient mqttClient;

    @Override
    public void start(Promise<Void> startFuture) {
        MqttClientOptions options = new MqttClientOptions()
            .setAutoKeepAlive(true)
            .setCleanSession(true);

        mqttClient = MqttClient.create(vertx, options);

        mqttClient.connect(1883, "192.168.153.74", connectResult -> {
            if (connectResult.succeeded()) {
                System.out.println("✅ Conectado al broker MQTT");

                // Aquí puedes hacer un timer para publicar periódicamente o bajo alguna lógica
                vertx.setPeriodic(5000, id -> {
                    enviarEstadoDesdeBDAlActuador(1); // ID del actuador que quieras enviar
                });

                startFuture.complete();
            } else {
                System.err.println("❌ Error de conexión MQTT: " + connectResult.cause().getMessage());
                startFuture.fail(connectResult.cause());
            }
        });
    }

    private void enviarEstadoDesdeBDAlActuador(int idActuador) {
        WebClient client = WebClient.create(vertx);

        client.get(8080, "localhost", "/api/alertas/parada-llena/" + idActuador)
            .send(ar -> {
                if (ar.succeeded()) {
                    JsonObject respuesta = ar.result().bodyAsJsonObject();

                    System.out.println("📤 Publicando a actuador/1/estado: " + respuesta.encode());

                    mqttClient.publish(
                        "actuador/" + idActuador + "/estado",  // El STM debe suscribirse a esto
                        Buffer.buffer(respuesta.encode()),
                        MqttQoS.AT_LEAST_ONCE,
                        false,
                        false
                    );
                } else {
                    System.err.println("❌ Error consultando BD: " + ar.cause().getMessage());
                }
            });
    }
}
