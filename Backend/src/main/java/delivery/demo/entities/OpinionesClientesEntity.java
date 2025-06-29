package delivery.demo.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("opiniones_clientes")
public class OpinionesClientesEntity {
    @Id
    private String id;
    private int cliente_id;
    private int empresa_id;
    private String comentario;
    private int puntuacion;
    private Instant fecha;
}
