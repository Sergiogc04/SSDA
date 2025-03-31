SHOW TABLES;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS CamaraDeteccion, EstadoActuador, Camara, Actuador, Grupo;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE Grupo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    canal_mqtt VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL
);

CREATE TABLE Camara (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    id_grupo INT NOT NULL,
    FOREIGN KEY (id_grupo) REFERENCES Grupo(id) ON DELETE CASCADE
);

CREATE TABLE Actuador (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    id_grupo INT NOT NULL,
    FOREIGN KEY (id_grupo) REFERENCES Grupo(id) ON DELETE CASCADE
);

CREATE TABLE CamaraDeteccion ( 
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_camara INT NOT NULL,
    num_personas INT NOT NULL,
    dia_semana ENUM('Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo') NOT NULL,
    contador_semanal INT NOT NULL DEFAULT 0,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_camara) REFERENCES Camara(id) ON DELETE CASCADE
);

CREATE TABLE EstadoActuador (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_actuador INT NOT NULL,
    estado BOOLEAN NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_actuador) REFERENCES Actuador(id) ON DELETE CASCADE
);





