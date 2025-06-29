package delivery.demo.services;

import delivery.demo.repositories.LogsPedidosRepositoryImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogsPedidosService {

    private final LogsPedidosRepositoryImp logsPedidosRepository;

    @Autowired
    public LogsPedidosService(LogsPedidosRepositoryImp logsPedidosRepository) {
        this.logsPedidosRepository = logsPedidosRepository;
    }

    public long obtenerCantidadDePedidosConCambiosRapidos() {
        return logsPedidosRepository.contarPedidosConCambiosRapidos();
    }
}
