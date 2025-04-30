#include "esp_camera.h"
#include <WiFi.h>
#include <ArduinoJson.h>
#include <HTTPClient.h>
#include <time.h>

// Configuración WiFi
const char* ssid = "Redmi Note 10 5G";
const char* wifiPassword = "patata88";

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
uint16_t prev_frame[H][W] = {0};
uint16_t current_frame[H][W] = {0};
int personasCount = 0;
unsigned long ultimoContecTime = 0;
unsigned long lastSendTime = 0;
const long sendInterval = 5000; // Enviar cada 5 segundos

void setup() {
  Serial.begin(115200);
  Serial.setDebugOutput(true);
  Serial.println();

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

  esp_err_t err = esp_camera_init(&config);
  if (err != ESP_OK) {
    Serial.printf("Error al iniciar la cámara: 0x%x", err);
    return;
  }

  setup_wifi();
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

  Serial.println("\n✅ WiFi conectado");
  Serial.println("IP local: ");
  Serial.println(WiFi.localIP());
}

bool capture_still() {
  camera_fb_t *fb = esp_camera_fb_get();
  if (!fb) {
    Serial.println("❌ Error al capturar imagen");
    return false;
  }

  for (int y = 0; y < H; y++)
    for (int x = 0; x < W; x++)
      current_frame[y][x] = 0;

  for (uint32_t i = 0; i < WIDTH * HEIGHT; i++) {
    uint16_t x = i % WIDTH;
    uint16_t y = i / WIDTH;
    uint8_t block_x = x / BLOCK_SIZE;
    uint8_t block_y = y / BLOCK_SIZE;
    current_frame[block_y][block_x] += fb->buf[i];
  }

  for (int y = 0; y < H; y++)
    for (int x = 0; x < W; x++) {
      // Procesamiento si es necesario
    }

  esp_camera_fb_return(fb);
  return true;
}

void loop() {
  if (millis() - lastSendTime > sendInterval) {
    if (capture_still()) {
      personasCount = random(0, 20); // Simulación de conteo
      Serial.print("Personas detectadas: ");
      Serial.println(personasCount);

      HTTPClient http;
      http.begin("http://192.168.153.74:8080/api/detecciones" );
      http.addHeader("Content-Type", "application/json");

      StaticJsonDocument<200> jsonDoc;
      jsonDoc["id_camara"] = 3;
      jsonDoc["num_personas"] = personasCount;

      String requestBody;
      serializeJson(jsonDoc, requestBody);

      int httpResponseCode = http.POST(requestBody);
      if (httpResponseCode > 0) {
        Serial.print("Movimiento detectado: ");
        Serial.println(httpResponseCode);
      } else {
        Serial.print("Error envio insercion: ");
        Serial.println(http.errorToString(httpResponseCode).c_str());
      }

      http.end();

      // Nueva funcionalidad: actualizar actuador si >10 personas
      if (personasCount > 10) {
        HTTPClient http2;
        http2.begin("http://192.168.153.74:8080/api/estados-actuador");
        http2.addHeader("Content-Type", "application/json");

        StaticJsonDocument<200> jsonDoc2;
        jsonDoc2["id_camara"] = 3;
        jsonDoc2["estado"] = true;

        String requestBody2;
        serializeJson(jsonDoc2, requestBody2);

        int httpResponseCode2 = http2.POST(requestBody2);
        if (httpResponseCode2 > 0) {
          Serial.print("Estado en BD actualizado: ");
          Serial.println(httpResponseCode2);
        } else {
          Serial.print("Error al actualizar actuador: ");
          Serial.println(http2.errorToString(httpResponseCode2).c_str());
        }

        http2.end();
      } else {
        HTTPClient http2;
        http2.begin("http://192.168.153.74:8080/api/estados-actuador");
        http2.addHeader("Content-Type", "application/json");

        StaticJsonDocument<200> jsonDoc2;
        jsonDoc2["id_camara"] = 3;
        jsonDoc2["estado"] = false;

        String requestBody2;
        serializeJson(jsonDoc2, requestBody2);

        int httpResponseCode2 = http2.POST(requestBody2);
        if (httpResponseCode2 > 0) {
          Serial.print("Estado en BD actualizado: ");
          Serial.println(httpResponseCode2);
        } else {
          Serial.print("Error al actualizar actuador: ");
          Serial.println(http2.errorToString(httpResponseCode2).c_str());
        }

      }
      
    }

    lastSendTime = millis();
  }
}
