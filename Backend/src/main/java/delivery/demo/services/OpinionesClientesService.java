package delivery.demo.services;

import delivery.demo.repositories.OpinionesClientesRepository;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OpinionesClientesService {

    private final OpinionesClientesRepository opinionesClientesRepository;

    @Autowired
    public OpinionesClientesService(OpinionesClientesRepository opinionesClientesRepository) {
        this.opinionesClientesRepository = opinionesClientesRepository;
    }

    public List<Document> opinionesPorHoraDelDia() {
        return opinionesClientesRepository.opinionesPorHoraDelDia();
    }

    public List<Document> promedioPuntuacionPorEmpresa() {
        return opinionesClientesRepository.promedioPuntuacionPorEmpresa();
    }
    public List<Document> opinionesConProblemasDetectados() {
        return opinionesClientesRepository.opinionesConProblemasDetectados();
    }
}
