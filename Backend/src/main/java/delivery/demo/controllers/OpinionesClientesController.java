package delivery.demo.controllers;

import delivery.demo.repositories.OpinionesClientesRepository;
import delivery.demo.services.OpinionesClientesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.bson.Document;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/opiniones-clientes")
@CrossOrigin

public class OpinionesClientesController {

    @Autowired
    OpinionesClientesService opinionesClientesService;


    @GetMapping("/hora-dia")
    public List<Document> obtenerOpinionesPorHoraDelDia() {
        return opinionesClientesService.opinionesPorHoraDelDia();
    }

    @GetMapping("/promedio-empresa")
    public List<Document> obtenerPromedioPuntuacionPorEmpresa() {
        return opinionesClientesService.promedioPuntuacionPorEmpresa();
    }

}
