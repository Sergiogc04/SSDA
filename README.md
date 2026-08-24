# SSDA: Bus Stop Occupancy Tracking & Distribution System

**SSDA** (*Sistema de Seguimiento y Distribución de Autobuses*) is a comprehensive IoT solution designed to monitor and manage bus stop occupancy in real time. By leveraging **Edge Computing**, the system detects passenger flow locally on microcontrollers and synchronizes data across a distributed network using **MQTT** and custom APIs.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Technical Architecture](#technical-architecture)
- [Edge Detection Algorithm](#edge-detection-algorithm)
- [Key Features](#key-features)
- [Tech Stack](#tech-stack)
- [Repository Structure](#repository-structure)
- [Getting Started](#getting-started)
- [Authors](#authors)
- [License](#license)

---

## Project Overview

Traditional bus-stop monitoring relies on manual counts or expensive centralized vision systems. SSDA takes a different approach: cheap, distributed ESP32-CAM nodes run occupancy detection locally, at the edge, and only send lightweight status updates upstream. This keeps bandwidth and infrastructure costs low while still enabling real-time, city-wide visibility into which stops are full and which have room.

## Technical Architecture

The system is structured into three main layers:

- **Edge Nodes (ESP32-CAM):** Independent camera units placed at each bus stop that perform local image analysis and occupancy counting, without relying on a cloud vision service.
- **Communication Layer:** A dual-layer API architecture (High-level / Low-level) that manages data flow between nodes via **WiFi** and the **MQTT** protocol.
- **Command Center (Actuator):** A central node with an **LCD interface** and physical buttons that lets an operator monitor and switch between the feeds of different bus stops in real time.

## Edge Detection Algorithm

Instead of resource-heavy AI models that wouldn't fit on constrained hardware, this project implements a **frame-to-frame comparison** algorithm optimized for microcontrollers:

- **Resolution:** QVGA (320×240).
- **Block-based analysis:** each frame is divided into **16×16 pixel blocks** for efficient processing.
- **Motion threshold:** a **20% change threshold** per block triggers person-counting events.
- **Memory efficiency:** global frame buffers (`prev_frame`, `current_frame`) are reused to minimize heap fragmentation on the ESP32.

## Key Features

- **Real-time monitoring** of occupancy status (Full / Available) per bus stop.
- **Multi-tier API design:**
  - *Low-level API:* hardware abstraction, sensor readings, and camera control.
  - *High-level API:* business logic, database synchronization, and network protocols.
- **Interactive HMI:** a physical control interface to switch between bus stop feeds on the local LCD.
- **Historical logging:** integration with a database backend for long-term urban analytics and data persistence.

## Tech Stack

- **Languages:** C / C++ (Arduino / ESP-IDF framework).
- **Hardware:** ESP32-CAM camera units, ESP32/STM32 actuator hub, I2C LCD display.
- **Protocols:** MQTT (Mosquitto broker), HTTP/REST, WiFi.
- **Other:** Digital Image Processing (DIP), database logging.

## Repository Structure

```
SSDA/
├── BaseDeDatos/       # Database schema and logic for historical occupancy logging
├── Camara/            # ESP32-CAM firmware: image capture and occupancy detection
├── ConexionCamara/    # Networking layer connecting camera nodes to the system (WiFi/API)
├── WiFi_MQTT/         # WiFi provisioning and MQTT communication layer
└── .project           # Project configuration file
```

> Note: folder names are kept in Spanish, as in the original project. Feel free to rename them to English (e.g. `Database/`, `Camera/`, `CameraConnection/`, `WiFi_MQTT/`) if you want the repo to be fully English-facing.

## Getting Started

1. **Hardware setup:** flash the firmware in `Camara/` onto each ESP32-CAM unit, and the actuator firmware onto the central hub.
2. **Network configuration:** set your WiFi credentials and MQTT broker address in `WiFi_MQTT/`.
3. **Database:** set up the schema/backend described in `BaseDeDatos/` to receive and store occupancy logs.
4. **Run:** power on the camera nodes and the actuator hub; occupancy status should start streaming to the LCD and the database in real time.

*(Adjust these steps to match your actual build/flash tooling — e.g. Arduino IDE, PlatformIO, or ESP-IDF — and add exact commands once confirmed.)*

## Authors

- Sirio Randazzo Lecubarri
- Sergio Garrido Caballero
- Antonio Presencio de Olmedo
- Daniel Salamanca Garrido

*Computer Engineering students — University of Seville*

## License

No license has been specified for this project yet. Consider adding one (e.g. MIT, Apache 2.0) if you plan to share it publicly.
