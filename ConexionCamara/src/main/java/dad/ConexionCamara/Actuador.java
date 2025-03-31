package dad.ConexionCamara;


public class Actuador {
 private Integer id;
 private String nombre;
 private Integer idGrupo;

 public Actuador() {
 }

 public Actuador(Integer id, String nombre, Integer idGrupo) {
     this.id = id;
     this.nombre = nombre;
     this.idGrupo = idGrupo;
 }

 // Getters and setters
 public Integer getId() {
     return id;
 }

 public void setId(Integer id) {
     this.id = id;
 }

 public String getNombre() {
     return nombre;
 }

 public void setNombre(String nombre) {
     this.nombre = nombre;
 }

 public Integer getIdGrupo() {
     return idGrupo;
 }

 public void setIdGrupo(Integer idGrupo) {
     this.idGrupo = idGrupo;
 }

 @Override
 public String toString() {
     return "Actuador [id=" + id + ", nombre=" + nombre + ", idGrupo=" + idGrupo + "]";
 }
}