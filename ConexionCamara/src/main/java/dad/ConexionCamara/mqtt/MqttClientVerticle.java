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
    
    @Override
    public void start(Promise<Void> startFuture) {
        MqttClientOptions options = new MqttClientOptions()
            .setAutoKeepAlive(true)
            .setCleanSession(true);
        
        MqttClient mqttClient = MqttClient.create(vertx, options);
        
        mqttClient.connect(1883, "localhost", connectResult -> {
            if (connectResult.succeeded()) {
                System.out.println("Conectado al broker MQTT");
                
                // Suscripción a los temas relevantes
                mqttClient.subscribe("parada/+/personas", MqttQoS.AT_LEAST_ONCE.value());
                mqttClient.subscribe("parada/+/estado", MqttQoS.AT_LEAST_ONCE.value());
                
                // Manejador de mensajes recibidos
                mqttClient.publishHandler(message -> {
                    String topic = message.topicName();
                    String payload = message.payload().toString();
                    
                    System.out.println("Mensaje recibido - Topic: " + topic + " - Payload: " + payload);
                    
                    try {
                        // Usar el método fromString para crear JsonObject
                        JsonObject json = new JsonObject(payload);
                        
                        if (topic.startsWith("parada/") && topic.endsWith("/personas")) {
                            // Procesar detección de personas
                            processDeteccion(json);
                        } else if (topic.startsWith("parada/") && topic.endsWith("/estado")) {
                            // Procesar cambio de estado
                            processEstado(json);
                        }
                    } catch (Exception e) {
                        System.err.println("Error procesando mensaje MQTT: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
                
            } else {
                System.err.println("Error de conexión MQTT: " + connectResult.cause().getMessage());
                startFuture.fail(connectResult.cause());
            }
        });
    }
    
    private void processDeteccion(JsonObject deteccion) {
        // Verificar que los campos requeridos existen
        if (deteccion.containsKey("id_camara") && deteccion.containsKey("num_personas")) {
            WebClient client = WebClient.create(vertx);
            client.post(8080, "localhost", "/api/detecciones")
                .sendJsonObject(deteccion, ar -> {
                    if (ar.succeeded()) {
                        System.out.println("Detección guardada en BD");
                    } else {
                        System.err.println("Error guardando detección: " + ar.cause().getMessage());
                    }
                });
        } else {
            System.err.println("Detección no contiene campos requeridos");
        }
    }
    
    private void processEstado(JsonObject estado) {
        // Usar getInteger() de esta forma
        Integer idActuador = estado.getInteger("id_actuador");
        Boolean estadoActuador = estado.getBoolean("estado");
        
        if (idActuador != null && estadoActuador != null) {
            WebClient client = WebClient.create(vertx);
            client.post(8080, "localhost", "/api/estados-actuador")
                .sendJsonObject(estado, ar -> {
                    if (ar.succeeded()) {
                        System.out.println("Estado actualizado en BD");
                        publishConfirmacion(idActuador);
                    } else {
                        System.err.println("Error actualizando estado: " + ar.cause().getMessage());
                    }
                });
        } else {
            System.err.println("Estado no contiene campos requeridos");
        }
    }
    
    private void publishConfirmacion(int idActuador) {
        // Implementación de confirmación (opcional)
        System.out.println("Estado del actuador " + idActuador + " actualizado correctamente");
    }
}