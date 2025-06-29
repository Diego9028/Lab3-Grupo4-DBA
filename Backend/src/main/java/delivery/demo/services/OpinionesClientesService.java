package delivery.demo.services;

import delivery.demo.repositories.OpinionesClientesRepository;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpinionesClientesService {

    private OpinionesClientesRepository opinionesClientesRepository;

    public List<Document> opinionesPorHoraDelDia() {
        return opinionesClientesRepository.opinionesPorHoraDelDia();
    }
}
