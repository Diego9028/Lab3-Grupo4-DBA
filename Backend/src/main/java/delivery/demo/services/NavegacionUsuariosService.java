package delivery.demo.services;

import delivery.demo.repositories.NavegacionUsuariosRepositoryImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NavegacionUsuariosService {

    private final NavegacionUsuariosRepositoryImp navegacionUsuariosRepository;

    @Autowired
    public NavegacionUsuariosService(NavegacionUsuariosRepositoryImp navegacionUsuariosRepository) {
        this.navegacionUsuariosRepository = navegacionUsuariosRepository;
    }

    public List<Integer> obtenerClientesQueBuscaronPeroNoCompraron() {
        return navegacionUsuariosRepository.obtenerClientesQueBuscaronPeroNoCompraron();
    }
}