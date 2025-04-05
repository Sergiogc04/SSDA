package dad.ConexionCamara;


import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import dad.ConexionCamara.Entities.Actuador;
import dad.ConexionCamara.Entities.Camara;
import dad.ConexionCamara.Entities.CamaraDeteccion;
import dad.ConexionCamara.Entities.EstadoActuador;
import dad.ConexionCamara.Entities.Grupo;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class MainVerticle {

    private MySQLPool mySqlClient;

    public MainVerticle() {
        // Configuración de la conexión a MySQL
        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
                .setPort(3306)
                .setHost("localhost")
                .setDatabase("bd_proyectodad")  // Cambia esto por el nombre de tu BD
                .setUser("root")
                .setPassword("root");

        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

        mySqlClient = MySQLPool.pool(vertx, connectOptions, poolOptions);

        // Ejemplos de operaciones con las diferentes tablas
        getAllActuadores();
        getCamaraById(1);
        insertGrupo(new Grupo(null, "canal/nuevo", "Nuevo Grupo"));
        getLastEstadoActuador(1);
        getDeteccionesByCamara(1);
    }

    // Operaciones para la tabla Actuador
    private void getAllActuadores() {
        mySqlClient.query("SELECT * FROM Actuador")
            .execute()
            .onSuccess(resultSet -> {
                JsonArray result = new JsonArray();
                for (Row row : resultSet) {
                    result.add(JsonObject.mapFrom(new Actuador(
                        row.getInteger("id"),
                        row.getString("nombre"),
                        row.getInteger("idGrupo")
                    )));
                }
                System.out.println("Actuadores: " + result);
            })
            .onFailure(err -> {
                System.out.println("Error al obtener actuadores: " + err.getMessage());
            });
    }

    // Operaciones para la tabla Camara
    private void getCamaraById(Integer id) {
        mySqlClient.preparedQuery("SELECT * FROM Camara WHERE id = ?")
                .execute(Tuple.of(id), res -> {
                    if (res.succeeded()) {
                        RowSet<Row> rows = res.result();
                        if (rows.size() > 0) {
                            Row row = rows.iterator().next();
                            Camara camara = new Camara(
                                    row.getInteger("id"),
                                    row.getString("nombre"),
                                    row.getInteger("idGrupo")
                            );
                            System.out.println("Cámara encontrada: " + camara);
                        } else {
                            System.out.println("No se encontró la cámara con ID: " + id);
                        }
                    } else {
                        System.out.println("Error al buscar cámara: " + res.cause().getMessage());
                    }
                });
    }

    // Operaciones para la tabla Grupo
    private void insertGrupo(Grupo grupo) {
        mySqlClient.preparedQuery("INSERT INTO Grupo (canalMqtt, nombre) VALUES (?, ?)")
                .execute(Tuple.of(grupo.getCanalMqtt(), grupo.getNombre()), res -> {
                    if (res.succeeded()) {
                        System.out.println("Grupo insertado correctamente");
                    } else {
                        System.out.println("Error al insertar grupo: " + res.cause().getMessage());
                    }
                });
    }

    // Operaciones para la tabla EstadoActuador
    private void getLastEstadoActuador(Integer idActuador) {
        mySqlClient.preparedQuery("SELECT * FROM EstadoActuador WHERE idActuador = ? ORDER BY timestamp DESC LIMIT 1")
                .execute(Tuple.of(idActuador), res -> {
                    if (res.succeeded()) {
                        RowSet<Row> rows = res.result();
                        if (rows.size() > 0) {
                            Row row = rows.iterator().next();
                            EstadoActuador estado = new EstadoActuador(
                                    row.getInteger("id"),
                                    row.getInteger("idActuador"),
                                    row.getBoolean("estado"),
                                    Timestamp.valueOf(row.getLocalDateTime("timestamp"))
                            );
                            System.out.println("Último estado del actuador: " + estado);
                        } else {
                            System.out.println("No se encontraron estados para el actuador con ID: " + idActuador);
                        }
                    } else {
                        System.out.println("Error al buscar estado del actuador: " + res.cause().getMessage());
                    }
                });
    }

    // Operaciones para la tabla CamaraDeteccion
    private void getDeteccionesByCamara(Integer idCamara) {
        mySqlClient.preparedQuery("SELECT * FROM CamaraDeteccion WHERE idCamara = ? ORDER BY timestamp DESC")
                .execute(Tuple.of(idCamara), res -> {
                    if (res.succeeded()) {
                        RowSet<Row> rows = res.result();
                        JsonArray detecciones = new JsonArray();
                        for (Row row : rows) {
                            detecciones.add(JsonObject.mapFrom(new CamaraDeteccion(
                                    row.getInteger("id"),
                                    row.getInteger("idCamara"),
                                    row.getInteger("numPersonas"),
                                    row.getString("diaSemana"),
                                    row.getInteger("contadorSemanal"),
                                    Timestamp.valueOf(row.getLocalDateTime("timestamp"))
                            )));
                        }
                        System.out.println("Detecciones para cámara " + idCamara + ": " + detecciones);
                    } else {
                        System.out.println("Error al buscar detecciones: " + res.cause().getMessage());
                    }
                });
    }

    // Método auxiliar para conversión de fechas
    private Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    
    	
    	public void stop(Promise<Void> stopPromise) throws Exception {
    	    if (mySqlClient != null) {
    	        mySqlClient.close()
    	            .onComplete(ar -> {
    	                if (ar.succeeded()) {
    	                    stopPromise.complete();
    	                } else {
    	                    stopPromise.fail(ar.cause());
    	                }
    	            });
    	    } else {
    	        stopPromise.complete();
    	    }
    	}
    }

