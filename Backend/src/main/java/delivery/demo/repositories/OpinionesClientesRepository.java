package delivery.demo.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;


import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OpinionesClientesRepository {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public OpinionesClientesRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    public List<Document> opinionesPorHoraDelDia() {
        AggregationOperation addFields = context -> new Document("$addFields",
                new Document("hora_dia",
                        new Document("$hour", new Document("date", new Document("$toDate", "$fecha"))
                                .append("timezone", "America/Santiago")
                        )
                )
        );

        Aggregation aggregation = Aggregation.newAggregation(
                addFields,
                Aggregation.group("hora_dia")
                        .count().as("total_opiniones")
                        .avg("puntuacion").as("promedio_puntuacion")
                        .push("comentario").as("comentarios"),
                Aggregation.sort(Sort.by("_id"))
        );

        return mongoTemplate.aggregate(aggregation, "opiniones_clientes", Document.class).getMappedResults();
    }

    public List<Document> promedioPuntuacionPorEmpresa() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("empresa_id")
                        .avg("puntuacion").as("promedio_puntuacion")
                        .count().as("total_opiniones"),
                Aggregation.sort(Sort.by("_id"))
        );

        return mongoTemplate.aggregate(aggregation, "opiniones_clientes", Document.class).getMappedResults();
    }
}
