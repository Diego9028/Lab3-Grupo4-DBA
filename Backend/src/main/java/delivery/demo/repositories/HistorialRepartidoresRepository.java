package delivery.demo.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.domain.Sort;
import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import java.util.Arrays;
import java.util.List;

@Repository
public class HistorialRepartidoresRepository {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public HistorialRepartidoresRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<Document> tramosFrecuentesUltimos7Dias() {
        AggregationOperation unwindRutas = context -> new Document("$unwind", "$rutas");

        AggregationOperation matchUltimos7Dias = context -> new Document("$match", new Document("$expr",
                new Document("$gte", Arrays.asList(
                        new Document("$dateFromString", new Document("dateString", "$rutas.timestamp")),
                        new Document("$dateSubtract", new Document("startDate", "$$NOW")
                                .append("unit", "day")
                                .append("amount", 7))
                ))
        ));

        AggregationOperation groupPorRepartidor = context -> new Document("$group", new Document("_id", "$_id")
                .append("repartidor_id", new Document("$first", "$repartidor_id"))
                .append("rutas", new Document("$push", "$rutas"))
        );

        AggregationOperation construirTramos = context -> new Document("$project", new Document("repartidor_id", 1)
                .append("tramos", new Document("$map", new Document()
                        .append("input", new Document("$range", Arrays.asList(
                                0,
                                new Document("$subtract", Arrays.asList(new Document("$size", "$rutas"), 1))
                        )))
                        .append("as", "i")
                        .append("in", new Document()
                                .append("origen", new Document()
                                        .append("lat", new Document("$arrayElemAt", Arrays.asList("$rutas.lat", "$$i")))
                                        .append("lng", new Document("$arrayElemAt", Arrays.asList("$rutas.lng", "$$i")))
                                )
                                .append("destino", new Document()
                                        .append("lat", new Document("$arrayElemAt", Arrays.asList("$rutas.lat", new Document("$add", Arrays.asList("$$i", 1)))))
                                        .append("lng", new Document("$arrayElemAt", Arrays.asList("$rutas.lng", new Document("$add", Arrays.asList("$$i", 1)))))
                                )
                        )
                ))
        );

        AggregationOperation unwindTramos = context -> new Document("$unwind", "$tramos");

        AggregationOperation groupFrecuencia = context -> new Document("$group", new Document("_id", new Document()
                .append("origen", "$tramos.origen")
                .append("destino", "$tramos.destino"))
                .append("frecuencia", new Document("$sum", 1))
        );

        AggregationOperation sortYLimit = context -> new Document("$sort", new Document("frecuencia", -1));
        AggregationOperation limitTop10 = context -> new Document("$limit", 10);

        Aggregation aggregation = Aggregation.newAggregation(
                unwindRutas,
                matchUltimos7Dias,
                groupPorRepartidor,
                construirTramos,
                unwindTramos,
                groupFrecuencia,
                sortYLimit,
                limitTop10
        );

        return mongoTemplate.aggregate(aggregation, "historial_repartidores", Document.class).getMappedResults();
    }
}