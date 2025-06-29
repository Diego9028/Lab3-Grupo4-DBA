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
public class LogsPedidosRepositoryImp {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public LogsPedidosRepositoryImp(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public long contarPedidosConCambiosRapidos() {
        // $addFields con $function para calcular "cumple_rapido"
        Document functionStage = new Document("$addFields",
                new Document("cumple_rapido",
                        new Document("$function", new Document()
                                .append("body", "function(eventos) {" +
                                        "if (!eventos || eventos.length < 4) return false;" +
                                        "eventos = eventos.map(e => new Date(e.timestamp));" +
                                        "for (let i = 0; i <= eventos.length - 4; i++) {" +
                                        "  let inicio = eventos[i];" +
                                        "  let fin = eventos[i + 3];" +
                                        "  let diffMin = (fin - inicio) / (1000 * 60);" +
                                        "  if (diffMin <= 10) return true;" +
                                        "}" +
                                        "return false;" +
                                        "}")
                                .append("args", List.of("$eventos"))
                                .append("lang", "js")
                        )
                )
        );

        // $match para filtrar los que cumplen
        AggregationOperation matchStage = context -> new Document("$match", new Document("cumple_rapido", true));

        // $count para contar los pedidos válidos
        AggregationOperation countStage = context -> new Document("$count", "pedidos_con_cambios_rapidos");

        // Ejecutar la agregación
        Aggregation aggregation = Aggregation.newAggregation(
                context -> functionStage,
                matchStage,
                countStage
        );

        AggregationResults<Document> result = mongoTemplate.aggregate(aggregation, "logs_pedidos", Document.class);
        Document doc = result.getUniqueMappedResult();

        return doc != null ? doc.getInteger("pedidos_con_cambios_rapidos").longValue() : 0;
    }
}
