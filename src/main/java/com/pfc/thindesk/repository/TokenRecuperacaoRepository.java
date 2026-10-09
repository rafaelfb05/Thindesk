package com.pfc.thindesk.repository;

import com.pfc.thindesk.entity.TokenRecuperacao;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TokenRecuperacaoRepository extends MongoRepository<TokenRecuperacao, String> {

    Optional<TokenRecuperacao> findByTokenHash(String tokenHash);

    void deleteByUsuarioId(String usuarioId);
}
