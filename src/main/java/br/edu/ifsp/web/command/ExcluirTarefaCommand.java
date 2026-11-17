package br.edu.ifsp.web.command;

import br.edu.ifsp.web.model.Usuario;
import br.edu.ifsp.web.service.TarefaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ExcluirTarefaCommand implements Command {
    private final TarefaService servico;
    public ExcluirTarefaCommand(TarefaService servico) { this.servico = servico; }

    @Override
    public void executar(HttpServletRequest req, HttpServletResponse resp, Usuario usuario, Long id) throws IOException {
        servico.excluirTarefa(usuario, id);
        resp.setStatus(204);
    }
}
