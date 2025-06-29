package delivery.demo.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import delivery.demo.repositories.HistorialRepartidoresRepository;
import org.bson.Document;
import java.util.List;


@Service
public class HistorialRepartidoresService {

    private final HistorialRepartidoresRepository historialRepartidoresRepository;

    @Autowired
    public HistorialRepartidoresService(HistorialRepartidoresRepository historialRepartidoresRepository) {
        this.historialRepartidoresRepository = historialRepartidoresRepository;
    }

    public List<Document> tramosFrecuentesUltimos7Dias() {
        return historialRepartidoresRepository.tramosFrecuentesUltimos7Dias();
    }

}
