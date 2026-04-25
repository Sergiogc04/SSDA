SSDA: Smart Bus Occupancy & Distribution System
Project Overview
SSDA is an end-to-end IoT solution designed to monitor and manage bus stop occupancy in real-time. By leveraging Edge Computing, the system detects passenger flow locally on microcontrollers and synchronizes data across a distributed network using MQTT and Custom APIs.

The project integrates hardware control, digital image processing, and a centralized command center for urban transport optimization.

Technical Architecture
The system is structured into three main layers:

Edge Nodes (ESP32-CAM): Three independent camera units that perform local image analysis.

Communication Layer: A dual-layer API (High/Low level) architecture managing data flow via WiFi and MQTT protocol.

Command Center (Actuator): A central hub with an LCD interface and physical buttons for real-time monitoring and node selection.

Edge Detection Algorithm
Unlike heavy AI models, this project implements a highly optimized Frame-to-Frame Comparison Algorithm suitable for resource-constrained hardware:

Resolution: QVGA (320x240).

Block-Based Analysis: The image is divided into 16x16 pixel blocks.

Motion Threshold: A 20% change threshold is used to trigger person counting events.

Memory Efficiency: Uses global frame buffers (prev_frame, current_frame) to minimize heap allocation.

Key Features
Real-time Monitoring: Live data transmission of occupancy status (Full/Available).

Multi-Tier API Design: * Low-Level API: Hardware abstraction, sensor readings, and camera control.

High-Level API: Logic management, database synchronization, and network protocols.

Interactive HMI: Physical control interface to toggle between different bus stop feeds on a local LCD.

Historical Logging: Database integration for long-term urban analytics.

Tech Stack
Languages: C / C++ (Arduino/ESP-IDF framework).

Hardware: ESP32-CAM, STM32/ESP32 Actuator, LCD I2C Display.

Protocols: MQTT (Mosquitto), HTTP/REST, WiFi.

Tools: Digital Image Processing (DIP), MySQL/NoSQL for data logging.

📂 Project Structure
/Camera_Node: Firmware for the ESP32-CAM units.

/Actuator_Center: Code for the central command hub and LCD management.

/API_Services: Logic for communication between nodes and database.
