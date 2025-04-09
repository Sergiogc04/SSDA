#include "esp_camera.h"
#include <WiFi.h>
#include <math.h>

#define CAMERA_MODEL_AI_THINKER
#include "camera_pins.h"

const char *ssid = "Redmi Note 10 5G";
const char *password = "patata88";

#define WIDTH 320
#define HEIGHT 240
#define BLOCK_SIZE 16
#define W (WIDTH / BLOCK_SIZE)
#define H (HEIGHT / BLOCK_SIZE)
#define BLOCK_DIFF_THRESHOLD 0.2
#define IMAGE_DIFF_THRESHOLD 0.2

uint16_t prev_frame[H][W] = {0};
uint16_t current_frame[H][W] = {0};
int list[2] = {0, 0};
int counter = 0;

unsigned long lastMotionTime = 0;
const unsigned long PERSON_TIMEOUT = 15000; // 15 segundos sin movimiento => se resta

void startCameraServer();
void setupLedFlash(int pin);
bool capture_still();
int motion_detect();
void update_frame();
int count_motion_blocks();

void setup() {
  Serial.begin(115200);
  esp_log_level_set("*", ESP_LOG_ERROR);
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
  config.fb_location = CAMERA_FB_IN_PSRAM;
  config.jpeg_quality = 12;
  config.fb_count = 1;
  config.grab_mode = CAMERA_GRAB_WHEN_EMPTY;

  esp_err_t err = esp_camera_init(&config);
  if (err != ESP_OK) {
    Serial.printf("Camera init failed with error 0x%x", err);
    return;
  }

  WiFi.begin(ssid, password);
  WiFi.setSleep(false);

  Serial.print("WiFi connecting");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("WiFi connected");

  startCameraServer();

  Serial.print("Camera Ready! Use 'http://");
  Serial.print(WiFi.localIP());
  Serial.println("' to connect");
}

void loop() {
  if (!capture_still()) {
    Serial.println("❌ Error al capturar imagen para análisis");
    delay(3000);
    return;
  }

  int motion = motion_detect();
  if (motion > 0) {
    counter++;
    lastMotionTime = millis();
    Serial.println("🟢 Movimiento detectado (entrada)");
  } else {
    if (millis() - lastMotionTime > PERSON_TIMEOUT && counter > 0) {
      counter--;
      Serial.println("🔴 No hay movimiento reciente. Se reduce el contador.");
      lastMotionTime = millis();
    }
  }

  Serial.print("👥 Personas actuales: ");
  Serial.println(counter);

  update_frame();
  delay(3000);
}

bool capture_still() {
  camera_fb_t *frame_buffer = esp_camera_fb_get();
  if (!frame_buffer) return false;

  for (int y = 0; y < H; y++)
    for (int x = 0; x < W; x++)
      current_frame[y][x] = 0;

  for (uint32_t i = 0; i < WIDTH * HEIGHT; i++) {
    const uint16_t x = i % WIDTH;
    const uint16_t y = floor(i / WIDTH);
    const uint8_t block_x = floor(x / BLOCK_SIZE);
    const uint8_t block_y = floor(y / BLOCK_SIZE);
    const uint8_t pixel = frame_buffer->buf[i];
    current_frame[block_y][block_x] += pixel;
  }

  for (int y = 0; y < H; y++)
    for (int x = 0; x < W; x++)
      current_frame[y][x] /= BLOCK_SIZE * BLOCK_SIZE;

  esp_camera_fb_return(frame_buffer);
  return true;
}

int motion_detect() {
  uint16_t changes = 0;
  const uint16_t blocks = (WIDTH * HEIGHT) / (BLOCK_SIZE * BLOCK_SIZE);

  for (int y = 0; y < H; y++) {
    for (int x = 0; x < W; x++) {
      float current = current_frame[y][x];
      float prev = prev_frame[y][x];
      float delta = abs(current - prev) / (prev != 0 ? prev : 1);

      if (delta >= BLOCK_DIFF_THRESHOLD) {
        changes++;
      }
    }
  }

  if ((1.0 * changes / blocks) > IMAGE_DIFF_THRESHOLD) {
    return 1;
  } else {
    return 0;
  }
}

void update_frame() {
  for (int y = 0; y < H; y++) {
    for (int x = 0; x < W; x++) {
      prev_frame[y][x] = current_frame[y][x];
    }
  }
}
