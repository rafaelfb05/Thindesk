package com.pfc.thindesk.service;

import com.mongodb.DuplicateKeyException;
import com.pfc.thindesk.dto.CadastroDTO;
import com.pfc.thindesk.dto.TecnicoDTO;
import com.pfc.thindesk.entity.Cliente;
import com.pfc.thindesk.entity.Usuario;
import com.pfc.thindesk.enums.TipoUsuario;
import com.pfc.thindesk.repository.ClienteRepository;
import com.pfc.thindesk.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ClienteService clienteService;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario cadastrarCliente(CadastroDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado");
        }
        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome().trim());
        cliente.setTelefone(dto.getTelefone().trim());
        cliente.setSetor(dto.getSetor().trim());
        cliente = clienteService.novoCliente(cliente);

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome().trim());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setTipo(TipoUsuario.CLIENTE);
        usuario.setAtivo(true);
        usuario.setClienteId(cliente.getId());

        try {
            return usuarioRepository.save(usuario);
        } catch (DuplicateKeyException e) {
            clienteRepository.deleteById(cliente.getId());
            throw new IllegalArgumentException("Este e-mail já está cadastrado");
        }
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public void alterarTipo(String id, TipoUsuario novoTipo, String emailLogado) {
        Usuario usuario = buscar(id);
        verificarPropriaConta(usuario, emailLogado);
        usuario.setTipo(novoTipo);
        usuarioRepository.save(usuario);
    }

    public void alterarAtivo(String id, boolean ativo, String emailLogado) {
        Usuario usuario = buscar(id);
        verificarPropriaConta(usuario, emailLogado);
        usuario.setAtivo(ativo);
        usuarioRepository.save(usuario);
    }

    private Usuario buscar(String id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
    }
    private void verificarPropriaConta(Usuario usuario, String emailLogado) {
        if (usuario.getEmail().equalsIgnoreCase(emailLogado)) {
            throw new IllegalArgumentException("Você não pode alterar a sua própria conta");
        }
    }

    public List<Usuario> listarPorTipo(TipoUsuario tipo) {
        return usuarioRepository.findByTipo(tipo);
    }

    public Usuario cadastrarTecnico(TecnicoDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome().trim());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        usuario.setTipo(TipoUsuario.TECNICO);
        usuario.setAtivo(true);

        try {
            return usuarioRepository.save(usuario);
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado");
        }
    }
}
