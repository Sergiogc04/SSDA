package dad.ConexionCamara.mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.MqttClientOptions;

import java.util.Arrays;
import java.util.List;

public class MqttClientVerticle extends AbstractVerticle {

    private MqttClient mqttClient;
    private static final int DELAY_MS = 1000; // 1 segundo entre publicaciones

    @Override
    public void start(Promise<Void> startFuture) {
        MqttClientOptions options = new MqttClientOptions()
            .setAutoKeepAlive(true)
            .setCleanSession(true);

        mqttClient = MqttClient.create(vertx, options);

        mqttClient.connect(1883, "192.168.153.74", connectResult -> {
            if (connectResult.succeeded()) {
                System.out.println("✅ Conectado al broker MQTT");

                // Lista de cámaras a consultar
                List<Integer> camaras = Arrays.asList(1, 2, 3);

                // Enviar periódicamente el estado de cada cámara con delay
                vertx.setPeriodic(5000, id -> {
                    enviarConDelay(camaras, 0);
                });

                startFuture.complete();
            } else {
                System.err.println("❌ Error de conexión MQTT: " + connectResult.cause().getMessage());
                startFuture.fail(connectResult.cause());
            }
        });
    }

    private void enviarConDelay(List<Integer> camaras, int index) {
        if (index >= camaras.size()) {
            return;
        }

        int camaraId = camaras.get(index);
        enviarEstadoPorCamara(camaraId);

        // Programar el siguiente envío con delay
        vertx.setTimer(DELAY_MS, timerId -> {
            enviarConDelay(camaras, index + 1);
        });
    }

    private void enviarEstadoPorCamara(int idCamara) {
        WebClient client = WebClient.create(vertx);

        client.get(8080, "localhost", "/api/alertas/parada-llena/" + idCamara)
            .send(ar -> {
                if (ar.succeeded()) {
                    JsonObject estado = ar.result().bodyAsJsonObject();

                    if (estado != null) {
                        String topic = "grupo" + (idCamara - 1); // Ej. grupo0 para camara 1
                        
                        // Extraer valores y formatear mensaje
                        int numPersonas = estado.getInteger("numPersonas", 0);
                        boolean estadoActuador = estado.getBoolean("estadoActuador", false);
                        String mensajeSimplificado = numPersonas + "," + estadoActuador;
                        
                        System.out.println("📤 Enviando a " + topic + ": " + mensajeSimplificado);

                        mqttClient.publish(
                            topic,
                            Buffer.buffer(mensajeSimplificado),
                            MqttQoS.AT_LEAST_ONCE,
                            false,
                            false
                        );
                        
                    } else {
                        System.out.println("⚠️ No se encontró estado para cámara " + idCamara);
                    }
                } else {
                    System.err.println("❌ Error consultando estado cámara " + idCamara + ": " + ar.cause().getMessage());
                }
            });
    }
}