#include "esp_camera.h"
#include <WiFi.h>
#include <ArduinoJson.h>
#include <HTTPClient.h>
#include <time.h>

// Configuración WiFi
const char* ssid = "Red";
const char* wifiPassword = "patata88";

// Configuración de detección
#define WIDTH 320
#define HEIGHT 240
#define BLOCK_SIZE 8
#define W (WIDTH / BLOCK_SIZE)
#define H (HEIGHT / BLOCK_SIZE)
#define BLOCK_DIFF_THRESHOLD 0.3
#define IMAGE_DIFF_THRESHOLD 0.1
#define MIN_MOTION_BLOCKS 10
#define MIN_PIXEL_DIFF 60
#define ADD_INTERVAL 3000      // Sumar cada 3 segundos si hay movimiento
#define SUBTRACT_INTERVAL 10000 // Restar cada 10 segundos si no hay movimiento
#define SEND_INTERVAL 5000     // Enviar datos cada 5 segundos

// Variables globales
uint16_t prev_frame[H][W] = {0};
uint16_t current_frame[H][W] = {0};
int personasCount = 0;
unsigned long lastAddTime = 0;
unsigned long lastSubtractTime = 0;
unsigned long lastSendTime = 0;
unsigned long lastMotionTime = 0;  // Tiempo del último movimiento detectado

// Configuración de la cámara
#define CAMERA_MODEL_AI_THINKER
#include "camera_pins.h"

void setup() {
  Serial.begin(115200);
  Serial.setDebugOutput(true);
  Serial.println("\nIniciando sistema de conteo asimétrico de personas...");

  // Configuración de la cámara (igual que antes)
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
    ESP.restart();
  }

  setup_wifi();
  configTime(0, 0, "pool.ntp.org");
  
  Serial.println("Sistema iniciado correctamente");
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
  Serial.print("IP local: ");
  Serial.println(WiFi.localIP());
}

bool capture_still() {
  camera_fb_t *fb = esp_camera_fb_get();
  if (!fb || fb->format != PIXFORMAT_GRAYSCALE) {
    Serial.println("Error en captura de imagen");
    if (fb) {
      esp_camera_fb_return(fb);
    }
    return false;
  }

  for (int y = 0; y < H; y++) {
    for (int x = 0; x < W; x++) {
      current_frame[y][x] = 0;
      
      for (int by = 0; by < BLOCK_SIZE; by++) {
        for (int bx = 0; bx < BLOCK_SIZE; bx++) {
          int px = x * BLOCK_SIZE + bx;
          int py = y * BLOCK_SIZE + by;
          if (px < WIDTH && py < HEIGHT) {
            current_frame[y][x] += fb->buf[py * WIDTH + px];
          }
        }
      }
      current_frame[y][x] /= (BLOCK_SIZE * BLOCK_SIZE);
    }
  }
  esp_camera_fb_return(fb);
  return true;
}

bool detect_motion() {
  int changed_blocks = 0;
  
  for (int y = 1; y < H-1; y++) {
    for (int x = 1; x < W-1; x++) {
      int16_t diff = abs((int16_t)current_frame[y][x] - (int16_t)prev_frame[y][x]);
      if (diff > MIN_PIXEL_DIFF) {
        changed_blocks++;
      }
    }
  }

  memcpy(prev_frame, current_frame, sizeof(current_frame));
  
  float changed_ratio = (float)changed_blocks / (float)(W * H);
  bool motion = (changed_ratio > IMAGE_DIFF_THRESHOLD) && (changed_blocks > MIN_MOTION_BLOCKS);
  
  return motion;
}

void update_person_count() {
  unsigned long currentTime = millis();
  
  if (capture_still()) {
    bool currentMotion = detect_motion();
    
    // Actualizar tiempo del último movimiento
    if (currentMotion) {
      lastMotionTime = currentTime;
    }
    
    // Sumar personas cada 3 segundos si hay movimiento
    if (currentMotion && (currentTime - lastAddTime >= ADD_INTERVAL)) {
      personasCount++;
      lastAddTime = currentTime;
      Serial.print("[+] Movimiento detectado. Personas: ");
      Serial.println(personasCount);
    }
    
    // Restar personas cada 10 segundos si no hay movimiento desde hace más de 10 segundos
    if (!currentMotion && (currentTime - lastMotionTime >= SUBTRACT_INTERVAL) && 
        (currentTime - lastSubtractTime >= SUBTRACT_INTERVAL)) {
      if (personasCount > 0) {
        personasCount--;
        lastSubtractTime = currentTime;
        Serial.print("[-] Sin movimiento por 10s. Personas: ");
        Serial.println(personasCount);
      }
    }
  }
}

void send_data_to_server() {
  Serial.print("Enviando datos - Personas detectadas: ");
  Serial.println(personasCount);
  
  HTTPClient http;
  http.begin("http://192.168.153.74:8080/api/detecciones");
  http.addHeader("Content-Type", "application/json");

  StaticJsonDocument<200> jsonDoc;
  jsonDoc["id_camara"] = 2;
  jsonDoc["num_personas"] = personasCount;

  String requestBody;
  serializeJson(jsonDoc, requestBody);

  int httpResponseCode = http.POST(requestBody);
  
  if (httpResponseCode > 0) {
    Serial.println("Datos enviados correctamente");
  } else {
    Serial.print("Error en el envío: ");
    Serial.println(http.errorToString(httpResponseCode).c_str());
  }
  http.end();

  // Control de actuador
  bool actuadorEstado = (personasCount > 10);
  HTTPClient http2;
  http2.begin("http://192.168.153.74:8080/api/estados-actuador");
  http2.addHeader("Content-Type", "application/json");

  StaticJsonDocument<200> jsonDoc2;
  jsonDoc2["id_camara"] = 3;
  jsonDoc2["estado"] = actuadorEstado;

  String requestBody2;
  serializeJson(jsonDoc2, requestBody2);

  int httpResponseCode2 = http2.POST(requestBody2);
  if (httpResponseCode2 > 0) {
    Serial.print("Actuador ");
    Serial.println(actuadorEstado ? "ACTIVADO" : "DESACTIVADO");
  } else {
    Serial.print("Error al actualizar actuador: ");
    Serial.println(http2.errorToString(httpResponseCode2).c_str());
  }
  http2.end();
}

void loop() {
  update_person_count();

  if (millis() - lastSendTime > SEND_INTERVAL) {
    send_data_to_server();
    lastSendTime = millis();
  }
  
  delay(100);
}

    lastSendTime = millis();
  }
}#include "esp_camera.h"
#include <WiFi.h>
#include <ArduinoJson.h>
#include <HTTPClient.h>
#include <time.h>

// Configuración WiFi
const char* ssid = "Red";
const char* wifiPassword = "patata88";

// Configuración de detección
#define WIDTH 320
#define HEIGHT 240
#define BLOCK_SIZE 8
#define W (WIDTH / BLOCK_SIZE)
#define H (HEIGHT / BLOCK_SIZE)
#define BLOCK_DIFF_THRESHOLD 0.3
#define IMAGE_DIFF_THRESHOLD 0.1
#define MIN_MOTION_BLOCKS 10
#define MIN_PIXEL_DIFF 60
#define ADD_INTERVAL 3000      // Sumar cada 3 segundos si hay movimiento
#define SUBTRACT_INTERVAL 10000 // Restar cada 10 segundos si no hay movimiento
#define SEND_INTERVAL 5000     // Enviar datos cada 5 segundos

// Variables globales
uint16_t prev_frame[H][W] = {0};
uint16_t current_frame[H][W] = {0};
int personasCount = 0;
unsigned long lastAddTime = 0;
unsigned long lastSubtractTime = 0;
unsigned long lastSendTime = 0;
unsigned long lastMotionTime = 0;  // Tiempo del último movimiento detectado

// Configuración de la cámara
#define CAMERA_MODEL_AI_THINKER
#include "camera_pins.h"

void setup() {
  Serial.begin(115200);
  Serial.setDebugOutput(true);
  Serial.println("\nIniciando sistema de conteo asimétrico de personas...");

  // Configuración de la cámara (igual que antes)
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
    ESP.restart();
  }

  setup_wifi();
  configTime(0, 0, "pool.ntp.org");
  
  Serial.println("Sistema iniciado correctamente");
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
  Serial.print("IP local: ");
  Serial.println(WiFi.localIP());
}

bool capture_still() {
  camera_fb_t *fb = esp_camera_fb_get();
  if (!fb || fb->format != PIXFORMAT_GRAYSCALE) {
    Serial.println("Error en captura de imagen");
    if (fb) {
      esp_camera_fb_return(fb);
    }
    return false;
  }

  for (int y = 0; y < H; y++) {
    for (int x = 0; x < W; x++) {
      current_frame[y][x] = 0;
      
      for (int by = 0; by < BLOCK_SIZE; by++) {
        for (int bx = 0; bx < BLOCK_SIZE; bx++) {
          int px = x * BLOCK_SIZE + bx;
          int py = y * BLOCK_SIZE + by;
          if (px < WIDTH && py < HEIGHT) {
            current_frame[y][x] += fb->buf[py * WIDTH + px];
          }
        }
      }
      current_frame[y][x] /= (BLOCK_SIZE * BLOCK_SIZE);
    }
  }
  esp_camera_fb_return(fb);
  return true;
}

bool detect_motion() {
  int changed_blocks = 0;
  
  for (int y = 1; y < H-1; y++) {
    for (int x = 1; x < W-1; x++) {
      int16_t diff = abs((int16_t)current_frame[y][x] - (int16_t)prev_frame[y][x]);
      if (diff > MIN_PIXEL_DIFF) {
        changed_blocks++;
      }
    }
  }

  memcpy(prev_frame, current_frame, sizeof(current_frame));
  
  float changed_ratio = (float)changed_blocks / (float)(W * H);
  bool motion = (changed_ratio > IMAGE_DIFF_THRESHOLD) && (changed_blocks > MIN_MOTION_BLOCKS);
  
  return motion;
}

void update_person_count() {
  unsigned long currentTime = millis();
  
  if (capture_still()) {
    bool currentMotion = detect_motion();
    
    // Actualizar tiempo del último movimiento
    if (currentMotion) {
      lastMotionTime = currentTime;
    }
    
    // Sumar personas cada 3 segundos si hay movimiento
    if (currentMotion && (currentTime - lastAddTime >= ADD_INTERVAL)) {
      personasCount++;
      lastAddTime = currentTime;
      Serial.print("[+] Movimiento detectado. Personas: ");
      Serial.println(personasCount);
    }
    
    // Restar personas cada 10 segundos si no hay movimiento desde hace más de 10 segundos
    if (!currentMotion && (currentTime - lastMotionTime >= SUBTRACT_INTERVAL) && 
        (currentTime - lastSubtractTime >= SUBTRACT_INTERVAL)) {
      if (personasCount > 0) {
        personasCount--;
        lastSubtractTime = currentTime;
        Serial.print("[-] Sin movimiento por 10s. Personas: ");
        Serial.println(personasCount);
      }
    }
  }
}

void send_data_to_server() {
  Serial.print("Enviando datos - Personas detectadas: ");
  Serial.println(personasCount);
  
  HTTPClient http;
  http.begin("http://192.168.153.74:8080/api/detecciones");
  http.addHeader("Content-Type", "application/json");

  StaticJsonDocument<200> jsonDoc;
  jsonDoc["id_camara"] = 3;
  jsonDoc["num_personas"] = personasCount;

  String requestBody;
  serializeJson(jsonDoc, requestBody);

  int httpResponseCode = http.POST(requestBody);
  
  if (httpResponseCode > 0) {
    Serial.println("Datos enviados correctamente");
  } else {
    Serial.print("Error en el envío: ");
    Serial.println(http.errorToString(httpResponseCode).c_str());
  }
  http.end();

  // Control de actuador
  bool actuadorEstado = (personasCount > 10);
  HTTPClient http2;
  http2.begin("http://192.168.153.74:8080/api/estados-actuador");
  http2.addHeader("Content-Type", "application/json");

  StaticJsonDocument<200> jsonDoc2;
  jsonDoc2["id_camara"] = 3;
  jsonDoc2["estado"] = actuadorEstado;

  String requestBody2;
  serializeJson(jsonDoc2, requestBody2);

  int httpResponseCode2 = http2.POST(requestBody2);
  if (httpResponseCode2 > 0) {
    Serial.print("Actuador ");
    Serial.println(actuadorEstado ? "ACTIVADO" : "DESACTIVADO");
  } else {
    Serial.print("Error al actualizar actuador: ");
    Serial.println(http2.errorToString(httpResponseCode2).c_str());
  }
  http2.end();
}

void loop() {
  update_person_count();

  if (millis() - lastSendTime > SEND_INTERVAL) {
    send_data_to_server();
    lastSendTime = millis();
  }
  
  delay(100);
}
