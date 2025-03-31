-- Insertar grupos
INSERT INTO Grupo (canal_mqtt, nombre) VALUES
('grupo_1/canal', 'Grupo Centro'),
('grupo_2/canal', 'Grupo Norte'),
('grupo_3/canal', 'Grupo Sur');

-- Insertar cámaras asociadas a los grupos
INSERT INTO Camara (nombre, id_grupo) VALUES
('Camara 1 - Centro', 1),
('Camara 2 - Centro', 1),
('Camara 3 - Norte', 2),
('Camara 4 - Sur', 3);

-- Insertar actuadores asociados a los grupos
INSERT INTO Actuador (nombre, id_grupo) VALUES
('Semáforo Peatonal Centro', 1),
('Semáforo Peatonal Norte', 2),
('Semáforo Peatonal Sur', 3);

-- Insertar detecciones de cámaras
INSERT INTO CamaraDeteccion (id_camara, num_personas, dia_semana, contador_semanal) VALUES
(1, 5, 'Lunes', 10),
(1, 8, 'Martes', 20),
(2, 12, 'Miércoles', 15),
(3, 20, 'Jueves', 25),
(4, 30, 'Viernes', 30);

-- Insertar estados de actuadores
INSERT INTO EstadoActuador (id_actuador, estado) VALUES
(1, TRUE),
(2, FALSE),
(3, TRUE);
