package dad.ConexionCamara;

//CamaraDeteccion.java


import java.sql.Timestamp;

public class CamaraDeteccion {
 private Integer id;
 private Integer idCamara;
 private Integer numPersonas;
 private String diaSemana;
 private Integer contadorSemanal;
 private Timestamp timestamp;

 public CamaraDeteccion() {
 }

 public CamaraDeteccion(Integer id, Integer idCamara, Integer numPersonas, String diaSemana, Integer contadorSemanal, Timestamp timestamp) {
     this.id = id;
     this.idCamara = idCamara;
     this.numPersonas = numPersonas;
     this.diaSemana = diaSemana;
     this.contadorSemanal = contadorSemanal;
     this.timestamp = timestamp;
 }

 // Getters and setters
 public Integer getId() {
     return id;
 }

 public void setId(Integer id) {
     this.id = id;
 }

 public Integer getIdCamara() {
     return idCamara;
 }

 public void setIdCamara(Integer idCamara) {
     this.idCamara = idCamara;
 }

 public Integer getNumPersonas() {
     return numPersonas;
 }

 public void setNumPersonas(Integer numPersonas) {
     this.numPersonas = numPersonas;
 }

 public String getDiaSemana() {
     return diaSemana;
 }

 public void setDiaSemana(String diaSemana) {
     this.diaSemana = diaSemana;
 }

 public Integer getContadorSemanal() {
     return contadorSemanal;
 }

 public void setContadorSemanal(Integer contadorSemanal) {
     this.contadorSemanal = contadorSemanal;
 }

 public Timestamp getTimestamp() {
     return timestamp;
 }

 public void setTimestamp(Timestamp timestamp) {
     this.timestamp = timestamp;
 }

 @Override
 public String toString() {
     return "CamaraDeteccion [id=" + id + ", idCamara=" + idCamara + ", numPersonas=" + numPersonas + 
            ", diaSemana=" + diaSemana + ", contadorSemanal=" + contadorSemanal + 
            ", timestamp=" + timestamp + "]";
 }
}