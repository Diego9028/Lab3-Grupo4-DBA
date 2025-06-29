package delivery.demo.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document("logs_pedidos")
public class LogsPedidosEntity {
    @Id
    private String id;
    private int pedido_id;
    private List<Evento> eventos;
    public static class Evento {
        private String estado;
        private Instant timestamp;
    }
}
