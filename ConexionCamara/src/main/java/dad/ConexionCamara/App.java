package dad.ConexionCamara;

import dad.ConexionCamara.Api.RestServer_1;
import io.vertx.core.Vertx;

public class App {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        DatabaseService dbService = new DatabaseService(vertx);

        // Desplegar RestServer_1
        vertx.deployVerticle(new RestServer_1(dbService), serverDeploy -> {
            if (serverDeploy.succeeded()) {
                System.out.println("✅ Servidor REST desplegado correctamente");
                System.out.println("📌 Endpoints disponibles:");
                System.out.println("  - GET    /api/camaras");
                System.out.println("  - GET    /api/detecciones");
                System.out.println("  - GET    /api/detecciones/{idCamara}");
                System.out.println("  - POST   /api/detecciones");
                System.out.println("  - GET    /api/estados-actuador/{idActuador}");
                System.out.println("  - POST   /api/estados-actuador");
                System.out.println("  - GET    /api/alertas/parada-llena/:idActuador");
                System.out.println("----Y muchos mas----");
                
                // Una vez que el servidor está listo, desplegamos el cliente
                
                
                vertx.deployVerticle(new RestClient(), clientDeploy -> {
                    if (clientDeploy.succeeded()) {
                        System.out.println("✅ Cliente REST desplegado correctamente");
                    } else {
                        System.err.println("❌ Error al desplegar cliente: " + clientDeploy.cause().getMessage());
                        System.exit(1);
                    }
                });
                
                
            } else {
                System.err.println("❌ Error al desplegar servidor: " + serverDeploy.cause().getMessage());
                System.exit(1);
            }
        });
    }
}