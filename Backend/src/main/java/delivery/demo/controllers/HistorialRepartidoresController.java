package delivery.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import delivery.demo.services.HistorialRepartidoresService;

@RestController
@RequestMapping("/historial-repartidores")
@CrossOrigin
public class HistorialRepartidoresController {

    @Autowired
    HistorialRepartidoresService historialRepartidoresService;

    @GetMapping("/tramos-frecuentes-ultimos-7-dias")
    public Object tramosFrecuentesUltimos7Dias() {
        return historialRepartidoresService.tramosFrecuentesUltimos7Dias();
    }
}