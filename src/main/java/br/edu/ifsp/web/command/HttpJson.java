package br.edu.ifsp.web.command;

import br.edu.ifsp.web.service.CredenciaisInvalidasException;
import br.edu.ifsp.web.service.DadosInvalidosException;
import br.edu.ifsp.web.service.RecursoNaoEncontradoException;
import br.edu.ifsp.web.service.RegraDeDominioException;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/** Tradução HTTP/JSON compartilhada por Commands e controladores. */
public final class HttpJson {
    private static final Gson GSON = new Gson();

    private HttpJson() { }

    public static void json(HttpServletResponse resp, int status, Object corpo) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(GSON.toJson(corpo));
    }

    public static void erro(HttpServletResponse resp, int status, String mensagem) throws IOException {
        json(resp, status, Map.of("erro", mensagem));
    }

    /** Exceção da aplicação -> status HTTP. */
    public static void erro(HttpServletResponse resp, Exception e) throws IOException {
        if (e instanceof DadosInvalidosException) {
            erro(resp, 400, e.getMessage());
        } else if (e instanceof CredenciaisInvalidasException) {
            erro(resp, 401, e.getMessage());
        } else if (e instanceof RecursoNaoEncontradoException) {
            erro(resp, 404, e.getMessage());
        } else if (e instanceof RegraDeDominioException) {
            erro(resp, 409, e.getMessage());
        } else {
            e.printStackTrace();
            erro(resp, 500, "Erro interno do servidor");
        }
    }

    public static <T> T lerCorpo(HttpServletRequest req, Class<T> tipo) throws IOException {
        try {
            T corpo = GSON.fromJson(req.getReader(), tipo);
            if (corpo == null) throw new DadosInvalidosException("Corpo da requisição ausente");
            return corpo;
        } catch (JsonParseException e) {
            throw new DadosInvalidosException("JSON inválido", e);
        }
    }
}
