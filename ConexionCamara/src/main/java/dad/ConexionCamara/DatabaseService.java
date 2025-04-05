package dad.ConexionCamara;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.mysqlclient.SslMode;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

public class DatabaseService {
    private final MySQLPool mariadbPool;

    public DatabaseService(Vertx vertx) {
        // Configure for MariaDB compatibility
        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
            .setPort(3306)
            .setHost("localhost")
            .setDatabase("bd_proyectodad")
            .setUser("root")
            .setPassword("root")
            .setCharset("utf8mb4")
            .setCollation("utf8mb4_unicode_ci")
            .setSslMode(SslMode.DISABLED);

        // Disable version check warning
        System.setProperty("vertx.mysql.disableVersionCheck", "true");

        PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(5)
            .setIdleTimeout(5); // minutes

        this.mariadbPool = MySQLPool.pool(vertx, connectOptions, poolOptions);
    }

    // CameraDetections operations
    public Future<JsonArray> getDeteccionesByCamara(Integer idCamara) {
        Promise<JsonArray> promise = Promise.promise();
        
        String query = idCamara != null ?
            "SELECT * FROM CamaraDeteccion WHERE id_camara = ? ORDER BY timestamp DESC" :
            "SELECT * FROM CamaraDeteccion ORDER BY timestamp DESC";
        
        Tuple params = idCamara != null ? Tuple.of(idCamara) : Tuple.tuple();
        
        mariadbPool.preparedQuery(query)
            .execute(params)
            .onSuccess(rows -> {
                JsonArray result = new JsonArray();
                for (Row row : rows) {
                    result.add(new JsonObject()
                        .put("id", row.getInteger("id"))
                        .put("id_camara", row.getInteger("id_camara"))
                        .put("num_personas", row.getInteger("num_personas"))
                        .put("dia_semana", row.getString("dia_semana"))
                        .put("contador_semanal", row.getInteger("contador_semanal"))
                        .put("timestamp", row.getLocalDateTime("timestamp").toString()));
                }
                promise.complete(result);
            })
            .onFailure(promise::fail);
            
        return promise.future();
    }

    public Future<JsonObject> insertDeteccion(JsonObject deteccion) {
        Promise<JsonObject> promise = Promise.promise();
        
        mariadbPool.preparedQuery(
            "INSERT INTO CamaraDeteccion (id_camara, num_personas, dia_semana, contador_semanal) " +
            "VALUES (?, ?, ?, ?)")
            .execute(Tuple.of(
                deteccion.getInteger("id_camara"),
                deteccion.getInteger("num_personas"),
                deteccion.getString("dia_semana"),
                deteccion.getInteger("contador_semanal")
            ))
            .onSuccess(res -> {
                deteccion.put("id", res.property(io.vertx.mysqlclient.MySQLClient.LAST_INSERTED_ID));
                promise.complete(deteccion);
            })
            .onFailure(promise::fail);
            
        return promise.future();
    }

    // ActuatorState operations
    public Future<JsonObject> getLastEstadoActuador(Integer idActuador) {
        Promise<JsonObject> promise = Promise.promise();
        
        mariadbPool.preparedQuery(
            "SELECT * FROM EstadoActuador WHERE id_actuador = ? ORDER BY timestamp DESC LIMIT 1")
            .execute(Tuple.of(idActuador))
            .onSuccess(rows -> {
                if (rows.size() > 0) {
                    Row row = rows.iterator().next();
                    promise.complete(new JsonObject()
                        .put("id", row.getInteger("id"))
                        .put("id_actuador", row.getInteger("id_actuador"))
                        .put("estado", row.getBoolean("estado"))
                        .put("timestamp", row.getLocalDateTime("timestamp").toString()));
                } else {
                    promise.fail("No se encontraron estados para el actuador " + idActuador);
                }
            })
            .onFailure(promise::fail);
            
        return promise.future();
    }

    public Future<JsonObject> insertEstadoActuador(JsonObject estado) {
        Promise<JsonObject> promise = Promise.promise();
        
        mariadbPool.preparedQuery(
            "INSERT INTO EstadoActuador (id_actuador, estado) VALUES (?, ?)")
            .execute(Tuple.of(
                estado.getInteger("id_actuador"),
                estado.getBoolean("estado")
            ))
            .onSuccess(res -> {
                estado.put("id", res.property(io.vertx.mysqlclient.MySQLClient.LAST_INSERTED_ID));
                promise.complete(estado);
            })
            .onFailure(promise::fail);
            
        return promise.future();
    }

    // Reference data operations
    public Future<JsonArray> getCamaras() {
        Promise<JsonArray> promise = Promise.promise();
        
        mariadbPool.query("SELECT * FROM Camara")
            .execute()
            .onSuccess(rows -> {
                JsonArray result = new JsonArray();
                for (Row row : rows) {
                    result.add(new JsonObject()
                        .put("id", row.getInteger("id"))
                        .put("nombre", row.getString("nombre"))
                        .put("id_grupo", row.getInteger("id_grupo")));
                }
                promise.complete(result);
            })
            .onFailure(promise::fail);
            
        return promise.future();
    }

    public Future<JsonArray> getActuadores() {
        Promise<JsonArray> promise = Promise.promise();
        
        mariadbPool.query("SELECT * FROM Actuador")
            .execute()
            .onSuccess(rows -> {
                JsonArray result = new JsonArray();
                for (Row row : rows) {
                    result.add(new JsonObject()
                        .put("id", row.getInteger("id"))
                        .put("nombre", row.getString("nombre"))
                        .put("id_grupo", row.getInteger("id_grupo")));
                }
                promise.complete(result);
            })
            .onFailure(promise::fail);
            
        return promise.future();
    }

    public Future<Void> close() {
        return mariadbPool.close();
    }
}