# SSDA: Bus Tracking & Distribution System

## Project Overview
**SSDA** (*Bus Tracking and Distribution System*) is a comprehensive IoT solution designed to monitor and manage bus stop occupancy in real-time.

By leveraging **Edge Computing**, the system detects passenger flow locally on microcontrollers and synchronizes data across a distributed network using **MQTT** and custom APIs.

---

## Technical Architecture
The system is structured into three main layers:

* **Edge Nodes (ESP32-CAM):** Three independent camera units that perform local image analysis and occupancy counting.
* **Communication Layer:** A dual-layer API architecture (High/Low level) managing data flow via **WiFi** and **MQTT** protocol.
* **Command Center (Actuator):** A central node featuring an **LCD interface** and physical buttons for real-time monitoring and node selection.

---

## Edge Detection Algorithm
Instead of resource-heavy AI models, this project implements a **Frame-to-Frame Comparison** algorithm highly optimized for constrained hardware:

* **Resolution:** QVGA (320x240).
* **Block-Based Analysis:** The image is divided into **16x16 pixel blocks** for efficient processing.
* **Motion Threshold:** A **20% change threshold** is used to trigger person-counting events.
* **Memory Efficiency:** Utilizes global frame buffers (`prev_frame`, `current_frame`) to minimize heap fragmentation.

---

## Key Features
* **Real-time Monitoring:** Live transmission of occupancy status (Full/Available).
* **Multi-Tier API Design:**
    * *Low-Level API:* Hardware abstraction, sensor readings, and camera control.
    * *High-Level API:* Logic management, database synchronization, and network protocols.
* **Interactive HMI:** Physical control interface to toggle between different bus stop feeds on the local LCD.
* **Historical Logging:** Integration with databases for long-term urban analytics and data persistence.

---

## Tech Stack
* **Languages:** C / C++ (Arduino/ESP-IDF framework).
* **Hardware:** ESP32-CAM (3 units), STM32/ESP32 Actuator hub, I2C LCD Display.
* **Protocols:** MQTT (Mosquitto), HTTP/REST, WiFi.
* **Tools:** Digital Image Processing (DIP), Database logging.

---

## Project Structure
* `/Camera_Node`: Firmware for the ESP32-CAM units.
* `/Actuator_Center`: Code for the central command hub and LCD management.
* `/API_Services`: Logic for communication between nodes and database.

---

## Authors
**Sirio Randazzo Lecubarri**

**Sergio Garrido Caballero**

**Antonio Presencio de Olmedo**

**Daniel Salamanca Garrido**

*Computer Engineering Student*
**University of Seville**
