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
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import java.util.Arrays;
import java.util.List;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Repository
public class HistorialRepartidoresRepository {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public HistorialRepartidoresRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<Document> tramosFrecuentesUltimos7Dias() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.unwind("rutas"),
                Aggregation.match(Criteria.where("rutas.timestamp").gte(
                        new Document("$dateSubtract", new Document("startDate", "$$NOW")
                                .append("unit", "day")
                                .append("amount", 7))
                )),
                Aggregation.group("_id")
                        .first("repartidor_id").as("repartidor_id")
                        .push("rutas").as("rutas"),
                // 👇 esta es la etapa clave modificada
                (context -> Document.parse("""
        {
          $project: {
            repartidor_id: 1,
            tramos: {
              $map: {
                input: { $range: [0, { $subtract: [ { $size: "$rutas" }, 1 ] }] },
                as: "i",
                in: {
                  origen: {
                    lat: { $arrayElemAt: ["$rutas.lat", "$$i"] },
                    lng: { $arrayElemAt: ["$rutas.lng", "$$i"] }
                  },
                  destino: {
                    lat: { $arrayElemAt: ["$rutas.lat", { $add: ["$$i", 1] }] },
                    lng: { $arrayElemAt: ["$rutas.lng", { $add: ["$$i", 1] }] }
                  }
                }
              }
            }
          }
        }
        """)),
                Aggregation.unwind("tramos"),
                Aggregation.group("tramos.origen", "tramos.destino")
                        .count().as("frecuencia"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "frecuencia")),
                Aggregation.limit(10)
        );

        return mongoTemplate.aggregate(aggregation, "historial_repartidores", Document.class).getMappedResults();
    }
}