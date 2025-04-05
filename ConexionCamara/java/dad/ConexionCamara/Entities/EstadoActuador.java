package dad.ConexionCamara.Entities;


import java.sql.Timestamp;

public class EstadoActuador {
 private Integer id;
 private Integer idActuador;
 private Boolean estado;
 private Timestamp timestamp;

 public EstadoActuador() {
 }

 public EstadoActuador(Integer id, Integer idActuador, Boolean estado, Timestamp timestamp) {
     this.id = id;
     this.idActuador = idActuador;
     this.estado = estado;
     this.timestamp = timestamp;
 }

 // Getters and setters
 public Integer getId() {
     return id;
 }

 public void setId(Integer id) {
     this.id = id;
 }

 public Integer getIdActuador() {
     return idActuador;
 }

 public void setIdActuador(Integer idActuador) {
     this.idActuador = idActuador;
 }

 public Boolean getEstado() {
     return estado;
 }

 public void setEstado(Boolean estado) {
     this.estado = estado;
 }

 public Timestamp getTimestamp() {
     return timestamp;
 }

 public void setTimestamp(Timestamp timestamp) {
     this.timestamp = timestamp;
 }

 @Override
 public String toString() {
     return "EstadoActuador [id=" + id + ", idActuador=" + idActuador + 
            ", estado=" + estado + ", timestamp=" + timestamp + "]";
 }
}