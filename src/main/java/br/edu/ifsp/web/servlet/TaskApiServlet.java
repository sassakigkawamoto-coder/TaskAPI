package br.edu.ifsp.web.servlet;

import br.edu.ifsp.web.command.*;
import br.edu.ifsp.web.listener.AplicacaoListener;
import br.edu.ifsp.web.model.Usuario;
import br.edu.ifsp.web.service.TarefaService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Front Controller de /tarefas: resolve a rota, chama o Command e traduz exceções em HTTP. */
@WebServlet(urlPatterns = "/tarefas/*")
public class TaskApiServlet extends HttpServlet {
    private final Map<String, Command> rotas = new HashMap<>();

    @Override
    public void init() throws ServletException {
        TarefaService servico = (TarefaService) getServletContext().getAttribute(AplicacaoListener.TAREFA_SERVICE);
        rotas.put("GET:colecao", new ListarTarefasCommand(servico));
        rotas.put("POST:colecao", new CriarTarefaCommand(servico));
        rotas.put("GET:item", new BuscarTarefaCommand(servico));
        rotas.put("PUT:item", new AtualizarTarefaCommand(servico));
        rotas.put("PATCH:item", new ConcluirTarefaCommand(servico));
        rotas.put("DELETE:item", new ExcluirTarefaCommand(servico));
    }

    // service() em vez de doGet/doPost: HttpServlet não tem doPatch
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long id = null;
        String info = req.getPathInfo();
        if (info != null && !info.equals("/")) {
            try {
                id = Long.parseLong(info.substring(1));
            } catch (NumberFormatException e) {
                HttpJson.erro(resp, 404, "Recurso não encontrado");
                return;
            }
        }
        Command comando = rotas.get(req.getMethod() + ":" + (id == null ? "colecao" : "item"));
        if (comando == null) {
            HttpJson.erro(resp, 404, "Rota não encontrada");
            return;
        }
        Usuario usuario = SessaoUsuario.de(req);
        if (usuario == null) { // o filtro já barra antes; segunda linha de defesa
            HttpJson.erro(resp, 401, "Não autenticado");
            return;
        }
        try {
            comando.executar(req, resp, usuario, id);
        } catch (Exception e) {
            HttpJson.erro(resp, e);
        }
    }
}
