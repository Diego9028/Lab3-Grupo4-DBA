package delivery.demo.controllers;

import delivery.demo.services.NavegacionUsuariosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/navegacion")
public class NavegacionUsuariosController {

    private final NavegacionUsuariosService navegacionUsuariosService;

    @Autowired
    public NavegacionUsuariosController(NavegacionUsuariosService navegacionUsuariosService) {
        this.navegacionUsuariosService = navegacionUsuariosService;
    }

    @GetMapping("/sin-pedido")
    public List<Integer> obtenerClientesQueBuscaronPeroNoCompraron() {
        return navegacionUsuariosService.obtenerClientesQueBuscaronPeroNoCompraron();
    }
}
