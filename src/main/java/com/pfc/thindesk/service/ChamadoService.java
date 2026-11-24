package com.pfc.thindesk.service;

import com.pfc.thindesk.entity.Chamado;

import com.pfc.thindesk.entity.Usuario;
import com.pfc.thindesk.enums.StatusChamado;
import com.pfc.thindesk.exception.ChamadoResolvidoException;
import com.pfc.thindesk.exception.RegraNegocioException;
import com.pfc.thindesk.repository.ChamadoRepository;
import com.pfc.thindesk.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChamadoService {

    private static final String STATUS_CANCELADO = "CANCELADO";

    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;

    public ChamadoService(ChamadoRepository chamadoRepository, UsuarioRepository usuarioRepository) {
        this.chamadoRepository = chamadoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Chamado abrirChamado(String titulo, String descricao, String solicitante) {
        exigirTexto(titulo, "O título é obrigatório");
        exigirTexto(descricao, "A descrição é obrigatória");

        Chamado chamado = new Chamado();
        chamado.setTitulo(titulo.trim());
        chamado.setDescricao(descricao.trim());
        chamado.setUsuario(solicitante);
        chamado.setStatus(StatusChamado.ABERTO.name());
        chamado.setDataAbertura(LocalDateTime.now());
        return chamadoRepository.save(chamado);
    }

    public Chamado criarChamado(Chamado chamado) {
        impedirResolucaoManual(chamado.getStatus());
        if (chamado.getStatus() == null || chamado.getStatus().isBlank()) {
            chamado.setStatus(StatusChamado.ABERTO.name());
        }
        if (chamado.getDataAbertura() == null) {
            chamado.setDataAbertura(LocalDateTime.now());
        }
        return chamadoRepository.save(chamado);
    }

    public List<Chamado> listarChamados() {
        return chamadoRepository.findAll();
    }

    public List<Chamado> listarPorUsuario(String emailSolicitante) {
        return chamadoRepository.findByUsuarioOrderByDataAberturaDesc(emailSolicitante);
    }

    public List<Chamado> listarPorUsuarioId(String usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        return chamadoRepository.findByUsuarioOrderByDataAberturaDesc(usuario.getEmail());
    }

    public List<Chamado> listarPorUsuarioId(String usuarioId, String emailLogado, boolean equipe) {
        Usuario usuario = buscarUsuario(usuarioId);
        if (!equipe && !usuario.getEmail().equalsIgnoreCase(emailLogado)) {
            throw new AccessDeniedException("Você só pode consultar os seus próprios chamados");
        }
        return chamadoRepository.findByUsuarioOrderByDataAberturaDesc(usuario.getEmail());
    }

    public Chamado buscarPorId(String id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Chamado não encontrado com id: " + id));
    }

    public Chamado consultarChamado(String id, String emailLogado, boolean equipe) {
        Chamado chamado = buscarPorId(id);
        if (!equipe && !emailLogado.equalsIgnoreCase(chamado.getUsuario())) {
            throw new AccessDeniedException("Você só pode consultar os seus próprios chamados");
        }
        return chamado;
    }

    public Chamado atualizarChamado(String id, Chamado atualizado) {
        Chamado chamado = buscarPorId(id);
        garantirNaoResolvido(chamado);
        impedirResolucaoManual(atualizado.getStatus());

        chamado.setDescricao(atualizado.getDescricao());
        chamado.setStatus(atualizado.getStatus());
        chamado.setTipo(atualizado.getTipo());
        chamado.setTecnico(atualizado.getTecnico());
        chamado.setUsuario(atualizado.getUsuario());
        if (atualizado.getTitulo() != null && !atualizado.getTitulo().isBlank()) {
            chamado.setTitulo(atualizado.getTitulo().trim());
        }
        return chamadoRepository.save(chamado);
    }

    public Chamado cancelarChamado(String id) {
        Chamado chamado = buscarPorId(id);
        garantirNaoResolvido(chamado);
        chamado.setStatus(STATUS_CANCELADO);
        return chamadoRepository.save(chamado);
    }

    public Chamado resolverChamado(String id, String solucao, String tecnico) {
        exigirTexto(solucao, "A solução é obrigatória");

        Chamado chamado = buscarPorId(id);
        garantirNaoResolvido(chamado);
        if (STATUS_CANCELADO.equals(chamado.getStatus())) {
            throw new RegraNegocioException("Chamado cancelado não pode ser resolvido");
        }

        chamado.setSolucao(solucao.trim());
        chamado.setStatus(StatusChamado.RESOLVIDO.name());
        chamado.setDataResolucao(LocalDateTime.now());
        chamado.setTecnico(tecnico);
        return chamadoRepository.save(chamado);
    }

    private Usuario buscarUsuario(String usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuário não encontrado com id: " + usuarioId));
    }

    private void garantirNaoResolvido(Chamado chamado) {
        if (StatusChamado.RESOLVIDO.name().equals(chamado.getStatus())) {
            throw new ChamadoResolvidoException();
        }
    }

    private void impedirResolucaoManual(String status) {
        if (StatusChamado.RESOLVIDO.name().equals(status)) {
            throw new RegraNegocioException(
                    "Para marcar como RESOLVIDO use a operação de resolução, informando a solução");
        }
    }

    private void exigirTexto(String texto, String mensagem) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
    }
}