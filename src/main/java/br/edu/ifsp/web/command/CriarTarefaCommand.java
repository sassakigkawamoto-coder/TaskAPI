package br.edu.ifsp.web.command;

import br.edu.ifsp.web.model.Tarefa;
import br.edu.ifsp.web.model.Usuario;
import br.edu.ifsp.web.service.TarefaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CriarTarefaCommand implements Command {
    private final TarefaService servico;
    public CriarTarefaCommand(TarefaService servico) { this.servico = servico; }

    @Override
    public void executar(HttpServletRequest req, HttpServletResponse resp, Usuario usuario, Long id) throws IOException {
        TarefaEntrada in = HttpJson.lerCorpo(req, TarefaEntrada.class);
        Tarefa criada = servico.criarTarefa(usuario, in.titulo, in.descricao);
        HttpJson.json(resp, 201, criada);
    }
}
