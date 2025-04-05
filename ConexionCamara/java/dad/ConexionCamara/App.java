package dad.ConexionCamara;

import dad.ConexionCamara.Api.RestServer_1;
import io.vertx.core.Vertx;

public class App {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DatabaseService dbService = new DatabaseService(vertx);
        
        vertx.deployVerticle(new RestServer_1(dbService), res -> {
            if (res.succeeded()) {
                System.out.println("✅ Servidor REST desplegado correctamente");
                System.out.println("📌 Endpoints disponibles:");
                System.out.println("  - GET    /api/camaras");
                System.out.println("  - GET    /api/detecciones");
                System.out.println("  - GET    /api/detecciones/{idCamara}");
                System.out.println("  - POST   /api/detecciones");
                System.out.println("  - GET    /api/estados-actuador/{idActuador}");
                System.out.println("  - POST   /api/estados-actuador");
            } else {
                System.err.println("❌ Error al desplegar servidor: " + res.cause().getMessage());
                System.exit(1);
            }
        });
    }
}