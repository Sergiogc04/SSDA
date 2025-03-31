package dad.ConexionCamara;


public class Grupo {
 private Integer id;
 private String canalMqtt;
 private String nombre;

 public Grupo() {
 }

 public Grupo(Integer id, String canalMqtt, String nombre) {
     this.id = id;
     this.canalMqtt = canalMqtt;
     this.nombre = nombre;
 }

 // Getters and setters
 public Integer getId() {
     return id;
 }

 public void setId(Integer id) {
     this.id = id;
 }

 public String getCanalMqtt() {
     return canalMqtt;
 }

 public void setCanalMqtt(String canalMqtt) {
     this.canalMqtt = canalMqtt;
 }

 public String getNombre() {
     return nombre;
 }

 public void setNombre(String nombre) {
     this.nombre = nombre;
 }

 @Override
 public String toString() {
     return "Grupo [id=" + id + ", canalMqtt=" + canalMqtt + ", nombre=" + nombre + "]";
 }
}
