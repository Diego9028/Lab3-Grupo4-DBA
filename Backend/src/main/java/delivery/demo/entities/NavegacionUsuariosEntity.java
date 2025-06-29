package delivery.demo.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document("navegacion_usuarios")
public class NavegacionUsuariosEntity {
    @Id
    private String id;
    private int cliente_id;
    private List<EventoNavegacion> eventos;
    public static class EventoNavegacion {
        private String tipo;
        private String valor;
        private Instant timestamp;
    }
}
