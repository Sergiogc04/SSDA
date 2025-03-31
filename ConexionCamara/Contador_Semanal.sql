DELIMITER //

CREATE TRIGGER reiniciar_contador_martes
BEFORE INSERT ON CamaraDeteccion
FOR EACH ROW
BEGIN
    DECLARE dia_actual VARCHAR(10);
    DECLARE ultimo_contador INT DEFAULT 0;

    -- Obtener el día de la semana del nuevo registro
    SET dia_actual = DAYNAME(NEW.timestamp);
    
    -- Obtener el último contador de la misma cámara (excepto si es martes)
    IF dia_actual <> 'Tuesday' THEN
        SELECT IFNULL(contador_semanal, 0) 
        INTO ultimo_contador
        FROM CamaraDeteccion 
        WHERE id_camara = NEW.id_camara
        ORDER BY timestamp DESC 
        LIMIT 1;
    END IF;

    -- Asignar valores a NEW
    SET NEW.contador_semanal = ultimo_contador + NEW.num_personas;

    CASE dia_actual
        WHEN 'Monday' THEN SET NEW.dia_semana = 'Lunes';
        WHEN 'Tuesday' THEN SET NEW.dia_semana = 'Martes';
        WHEN 'Wednesday' THEN SET NEW.dia_semana = 'Miércoles';
        WHEN 'Thursday' THEN SET NEW.dia_semana = 'Jueves';
        WHEN 'Friday' THEN SET NEW.dia_semana = 'Viernes';
        WHEN 'Saturday' THEN SET NEW.dia_semana = 'Sábado';
        WHEN 'Sunday' THEN SET NEW.dia_semana = 'Domingo';
    END CASE;
END;
//

DELIMITER ;