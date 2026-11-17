package br.edu.ifsp.web.command;

import br.edu.ifsp.web.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface Command {
    /** id é null nas rotas de coleção (GET /tarefas, POST /tarefas). */
    void executar(HttpServletRequest req, HttpServletResponse resp, Usuario usuario, Long id) throws IOException;
}
