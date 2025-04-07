package dad.ConexionCamara.mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.mqtt.MqttClient;
import io.vertx.mqtt.messages.MqttPublishMessage;
import io.vertx.mqtt.MqttClientOptions;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class MqttClientVerticle extends AbstractVerticle {
    private Gson gson;
    
    @Override
    public void start(Promise<Void> startFuture) {
        gson = new Gson();
        MqttClient mqttClient = MqttClient.create(vertx, new MqttClientOptions().setAutoKeepAlive(true));
        
        mqttClient.connect(1883, "localhost", connectResult -> {
            if (connectResult.succeeded()) {
                System.out.println("Conectado al broker MQTT");
                
                // Suscripción al topic_2
                mqttClient.subscribe("topic_2", MqttQoS.AT_LEAST_ONCE.value(), subscribeResult -> {
                    if (subscribeResult.succeeded()) {
                        System.out.println("Suscrito correctamente. ClientID: " + mqttClient.clientId());
                    } else {
                        System.out.println("Error en suscripción: " + subscribeResult.cause().getMessage());
                    }
                });
                
                // Manejador de mensajes recibidos
                mqttClient.publishHandler(message -> {
                    System.out.println("\nMensaje recibido:");
                    System.out.println("  Topic: " + message.topicName());
                    System.out.println("  ID mensaje: " + message.messageId());
                    System.out.println("  Contenido (raw): " + message.payload().toString());
                    
                    try {
                        // Intenta parsear como JSON
                        JsonObject json = gson.fromJson(message.payload().toString(), JsonObject.class);
                        System.out.println("  Contenido (JSON): " + json);
                    } catch (JsonSyntaxException e) {
                        // Si no es JSON válido, muestra como texto plano
                        System.out.println("  Contenido (texto): " + message.payload().toString());
                    }
                });
                
                // Publicar mensaje de ejemplo
                mqttClient.publish("topic_1", 
                    Buffer.buffer("{\"ejemplo\":\"mensaje\",\"valor\":123}"), 
                    MqttQoS.AT_LEAST_ONCE, 
                    false, 
                    false);
                
            } else {
                System.out.println("Error de conexión: " + connectResult.cause().getMessage());
            }
        });
    }
}