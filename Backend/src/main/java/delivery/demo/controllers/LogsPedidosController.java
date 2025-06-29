package delivery.demo.controllers;

import delivery.demo.services.LogsPedidosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logs")
public class LogsPedidosController {

    private final LogsPedidosService logsPedidosService;

    @Autowired
    public LogsPedidosController(LogsPedidosService logsPedidosService) {
        this.logsPedidosService = logsPedidosService;
    }

    @GetMapping("/cambios-rapidos")
    public long contarPedidosRapidos() {
        return logsPedidosService.obtenerCantidadDePedidosConCambiosRapidos();
    }
}
