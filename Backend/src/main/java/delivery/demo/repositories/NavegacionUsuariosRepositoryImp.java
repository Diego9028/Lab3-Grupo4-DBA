package delivery.demo.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Repository;
import org.bson.Document;

import java.util.List;

@Repository
public class NavegacionUsuariosRepositoryImp {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public NavegacionUsuariosRepositoryImp(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<Integer> obtenerClientesQueBuscaronPeroNoCompraron() {

        AggregationOperation matchBusqueda = context -> new Document("$match",
                new Document("eventos", new Document("$elemMatch", new Document("tipo", "busqueda")))
        );

        AggregationOperation addTienePedidoConfirmado = context -> new Document("$addFields",
                new Document("tiene_pedido_confirmado",
                        new Document("$anyElementTrue", List.of(
                                new Document("$map", new Document()
                                        .append("input", "$eventos")
                                        .append("as", "e")
                                        .append("in", new Document("$and", List.of(
                                                new Document("$eq", List.of("$$e.tipo", "pedido")),
                                                new Document("$eq", List.of("$$e.valor", "confirmar"))
                                        )))
                                )
                        ))
                )
        );

        AggregationOperation matchSinPedidoConfirmado = context -> new Document("$match",
                new Document("tiene_pedido_confirmado", false)
        );

        AggregationOperation projectClienteId = context -> new Document("$project",
                new Document("cliente_id", 1).append("_id", 0)
        );

        Aggregation aggregation = Aggregation.newAggregation(
                matchBusqueda,
                addTienePedidoConfirmado,
                matchSinPedidoConfirmado,
                projectClienteId
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "navegacion_usuarios", Document.class);

        return results.getMappedResults().stream()
                .map(doc -> doc.getInteger("cliente_id"))
                .toList();
    }

}
