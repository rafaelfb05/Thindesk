package com.pfc.thindesk.repository;

import com.pfc.thindesk.entity.Usuario;
import com.pfc.thindesk.enums.TipoUsuario;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByClienteId(String clienteId);

    List<Usuario> findByTipo(TipoUsuario tipo);
}
