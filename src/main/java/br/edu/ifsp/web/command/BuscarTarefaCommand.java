package br.edu.ifsp.web.command;

import br.edu.ifsp.web.model.Usuario;
import br.edu.ifsp.web.service.TarefaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class BuscarTarefaCommand implements Command {
    private final TarefaService servico;
    public BuscarTarefaCommand(TarefaService servico) { this.servico = servico; }

    @Override
    public void executar(HttpServletRequest req, HttpServletResponse resp, Usuario usuario, Long id) throws IOException {
        HttpJson.json(resp, 200, servico.buscarTarefa(usuario, id));
    }
}
