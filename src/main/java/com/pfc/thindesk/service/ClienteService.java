package com.pfc.thindesk.service;

import com.pfc.thindesk.entity.Cliente;
import com.pfc.thindesk.repository.ClienteRepository;
import com.pfc.thindesk.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    public Cliente novoCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    public Cliente buscarPorId(String id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente não encontrado com id: " + id));
    }

    public Cliente atualizar(String id, String nome, String telefone, String setor) {
        Cliente cliente = buscarPorId(id);
        cliente.setNome(nome.trim());
        cliente.setTelefone(telefone.trim());
        cliente.setSetor(setor.trim());
        return clienteRepository.save(cliente);
    }

    public void deletar(String id) {
        if (usuarioRepository.existsByClienteId(id)) {
            throw new IllegalStateException(
                    "Este cliente possui conta de acesso. Desative a conta em Configurações > Usuários.");
        }
        clienteRepository.deleteById(id);
    }
}
