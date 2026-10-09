package com.pfc.thindesk.controller;

import com.pfc.thindesk.dto.ClienteDTO;
import com.pfc.thindesk.entity.Cliente;
import com.pfc.thindesk.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ClienteViewController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping("/novo")
    public String formNovo(Model model) {
        model.addAttribute("cliente", new ClienteDTO());
        return "cliente-form";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("cliente") ClienteDTO dto,
                         BindingResult result,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "cliente-form";
        }
        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome().trim());
        cliente.setTelefone(dto.getTelefone().trim());
        cliente.setSetor(dto.getSetor().trim());
        clienteService.novoCliente(cliente);

        redirect.addFlashAttribute("sucesso", "Cliente cadastrado");
        return "redirect:/clientes";
    }

    @GetMapping("/editar/{id}")
    public String formEditar(@PathVariable String id, Model model) {
        Cliente cliente = clienteService.buscarPorId(id);

        ClienteDTO dto = new ClienteDTO();
        dto.setNome(cliente.getNome());
        dto.setTelefone(cliente.getTelefone());
        dto.setSetor(cliente.getSetor());

        model.addAttribute("cliente", dto);
        model.addAttribute("clienteId", id);
        return "cliente-form";
    }

    @PostMapping("/editar/{id}")
    public String atualizar(@PathVariable String id,
                            @Valid @ModelAttribute("cliente") ClienteDTO dto,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("clienteId", id);
            return "cliente-form";
        }
        clienteService.atualizar(id, dto.getNome(), dto.getTelefone(), dto.getSetor());
        redirect.addFlashAttribute("sucesso", "Cliente atualizado");
        return "redirect:/clientes";
    }

    @PostMapping("/deletar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deletar(@PathVariable String id, RedirectAttributes redirect) {
        try {
            clienteService.deletar(id);
            redirect.addFlashAttribute("sucesso", "Cliente excluído");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/clientes";
    }
}
