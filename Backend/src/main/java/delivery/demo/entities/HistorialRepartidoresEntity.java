package delivery.demo.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("historial_repartidores")
public class HistorialRepartidoresEntity {
    @Id
    private String id;
    private int repartidor_id;
    private List<Ruta> rutas;
    public static class Ruta {
        private double lat;
        private double lng;
        private String timestamp;
    }
}
