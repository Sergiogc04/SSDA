#include "esp_camera.h"
#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <time.h>

// Configuración WiFi
const char* ssid = "Redmi Note 10 5G";
const char* wifiPassword = "patata88";

// Configuración MQTT
const char* mqtt_server = "192.168.153.74"; // IP de tu PC con Mosquitto
const int mqtt_port = 1883;
const char* mqtt_user = "usuario_mqtt";     // Opcional
const char* mqtt_password = "tu_contraseña"; // Opcional
const char* mqtt_topic = "parada/1/personas";

// Configuración de la cámara
#define CAMERA_MODEL_AI_THINKER
#include "camera_pins.h"

// Parámetros de detección de movimiento
#define WIDTH 320
#define HEIGHT 240
#define BLOCK_SIZE 16
#define W (WIDTH / BLOCK_SIZE)
#define H (HEIGHT / BLOCK_SIZE)
#define BLOCK_DIFF_THRESHOLD 0.2
#define IMAGE_DIFF_THRESHOLD 0.2

// Variables globales
WiFiClient espClient;
PubSubClient mqttClient(espClient);
uint16_t prev_frame[H][W] = {0};
uint16_t current_frame[H][W] = {0};
int personasCount = 0;
unsigned long ultimoContecTime = 0;
unsigned long lastMqttSend = 0;
const long mqttSendInterval = 5000; // Enviar cada 5 segundos

void setup() {
  Serial.begin(115200);
  Serial.setDebugOutput(true);
  Serial.println();
  
  // Configuración de la cámara
  camera_config_t config;
  config.ledc_channel = LEDC_CHANNEL_0;
  config.ledc_timer = LEDC_TIMER_0;
  config.pin_d0 = Y2_GPIO_NUM;
  config.pin_d1 = Y3_GPIO_NUM;
  config.pin_d2 = Y4_GPIO_NUM;
  config.pin_d3 = Y5_GPIO_NUM;
  config.pin_d4 = Y6_GPIO_NUM;
  config.pin_d5 = Y7_GPIO_NUM;
  config.pin_d6 = Y8_GPIO_NUM;
  config.pin_d7 = Y9_GPIO_NUM;
  config.pin_xclk = XCLK_GPIO_NUM;
  config.pin_pclk = PCLK_GPIO_NUM;
  config.pin_vsync = VSYNC_GPIO_NUM;
  config.pin_href = HREF_GPIO_NUM;
  config.pin_sccb_sda = SIOD_GPIO_NUM;
  config.pin_sccb_scl = SIOC_GPIO_NUM;
  config.pin_pwdn = PWDN_GPIO_NUM;
  config.pin_reset = RESET_GPIO_NUM;
  config.xclk_freq_hz = 20000000;
  config.frame_size = FRAMESIZE_QVGA;
  config.pixel_format = PIXFORMAT_GRAYSCALE;
  config.grab_mode = CAMERA_GRAB_WHEN_EMPTY;
  config.fb_location = CAMERA_FB_IN_PSRAM;
  config.jpeg_quality = 12;
  config.fb_count = 1;

  // Inicializar cámara
  esp_err_t err = esp_camera_init(&config);
  if (err != ESP_OK) {
    Serial.printf("Error al iniciar la cámara: 0x%x", err);
    return;
  }

  // Conectar WiFi
  setup_wifi();
  
  // Configurar MQTT
  mqttClient.setServer(mqtt_server, mqtt_port);
  
  // Configurar hora (opcional para timestamps precisos)
  configTime(0, 0, "pool.ntp.org");
}

void setup_wifi() {
  delay(10);
  Serial.println();
  Serial.print("Conectando a ");
  Serial.println(ssid);

  WiFi.begin(ssid, wifiPassword);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi conectado");
  Serial.println("Dirección IP: ");
  Serial.println(WiFi.localIP());
}

void reconnect_mqtt() {
  while (!mqttClient.connected()) {
    Serial.print("Intentando conexión MQTT...");
    
    // Intentar conectar
    if (mqttClient.connect("ESP32CAM_Client", mqtt_user, mqtt_password)) {
      Serial.println("Conectado");
    } else {
      Serial.print("Error, rc=");
      Serial.print(mqttClient.state());
      Serial.println(" reintentando en 5 segundos");
      delay(5000);
    }
  }
}

bool capture_still() {
  camera_fb_t *fb = esp_camera_fb_get();
  if (!fb) {
    Serial.println("Error al capturar imagen");
    return false;
  }

  // Procesar imagen para detección de movimiento
  for (int y = 0; y < H; y++) {
    for (int x = 0; x < W; x++) {
      current_frame[y][x] = 0;
    }
  }

  for (uint32_t i = 0; i < WIDTH * HEIGHT; i++) {
    const uint16_t x = i % WIDTH;
    const uint16_t y = i / WIDTH;
    const uint8_t block_x = x / BLOCK_SIZE;
    const uint8_t block_y = y / BLOCK_SIZE;
    current_frame[block_y][block_x] += fb->buf[i];
  }

  for (int y = 0; y < H; y++) {
    for (int x = 0; x < W; x++) {
      current_frame[y][x] /= (BLOCK_SIZE * BLOCK_SIZE);
    }
  }

  esp_camera_fb_return(fb);
  return true;
}

int motion_detect() {
  int changed = 0;
  for (int y = 0; y < H; y++) {
    for (int x = 0; x < W; x++) {
      float delta = abs(current_frame[y][x] - prev_frame[y][x]) / (prev_frame[y][x] ? prev_frame[y][x] : 1);
      if (delta > BLOCK_DIFF_THRESHOLD) {
        changed++;
      }
    }
  }

  float changeRatio = (float)changed / (H * W);
  return (changeRatio > IMAGE_DIFF_THRESHOLD) ? 1 : 0;
}

void update_frame() {
  for (int y = 0; y < H; y++) {
    for (int x = 0; x < W; x++) {
      prev_frame[y][x] = current_frame[y][x];
    }
  }
}

void processarMovimiento(int motion) {
  static int lastX = -1;
  int xAvg = 0, count = 0;
  
  for (int y = 0; y < H; y++) {
    for (int x = 0; x < W; x++) {
      float delta = abs(current_frame[y][x] - prev_frame[y][x]) / (prev_frame[y][x] ? prev_frame[y][x] : 1);
      if (delta > BLOCK_DIFF_THRESHOLD) {
        xAvg += x;
        count++;
      }
    }
  }

  if (count == 0) return;
  
  xAvg /= count;
  
  if (lastX != -1) {
    if (xAvg - lastX > 3) {
      personasCount++;
      Serial.printf("Persona entrando. Total: %d\n", personasCount);
    } else if (lastX - xAvg > 3) {
      if (personasCount > 0) personasCount--;
      Serial.printf("Persona saliendo. Total: %d\n", personasCount);
    }
  }
  
  lastX = xAvg;
}

void enviarDatosMQTT() {
  // Crear objeto JSON con los datos
  StaticJsonDocument<200> doc;
  doc["id_camara"] = 1; // ID de esta cámara
  doc["num_personas"] = personasCount;
  doc["dia_semana"] = obtenerDiaSemana();
  doc["contador_semanal"] = personasCount; // O implementa tu propio contador
  doc["timestamp"] = obtenerTimestamp();

  // Serializar JSON a string
  char jsonBuffer[512];
  serializeJson(doc, jsonBuffer);

  // Publicar mensaje MQTT
  if (mqttClient.publish(mqtt_topic, jsonBuffer)) {
    Serial.println("Datos enviados por MQTT:");
    Serial.println(jsonBuffer);
  } else {
    Serial.println("Error al enviar datos por MQTT");
  }
}

String obtenerDiaSemana() {
  const char* dias[] = {"Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"};
  time_t now;
  struct tm timeinfo;
  time(&now);
  localtime_r(&now, &timeinfo);
  
  return dias[timeinfo.tm_wday];
}

long obtenerTimestamp() {
  time_t now;
  time(&now);
  return now;
}

void loop() {
  if (!mqttClient.connected()) {
    reconnect_mqtt();
  }
  mqttClient.loop();

  if (!capture_still()) {
    delay(1000);
    return;
  }

  int motion = motion_detect();
  
  if (millis() - ultimoContecTime > 1000 && motion) {
    processarMovimiento(motion);
    ultimoContecTime = millis();
  }

  update_frame();

  // Enviar datos periódicamente o cuando cambie el conteo
  static int lastSentCount = -1;
  if ((millis() - lastMqttSend > mqttSendInterval) || (personasCount != lastSentCount)) {
    enviarDatosMQTT();
    lastSentCount = personasCount;
    lastMqttSend = millis();
  }

  delay(100);
}