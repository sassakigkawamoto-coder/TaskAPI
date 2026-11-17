package br.edu.ifsp.web.servlet;

import br.edu.ifsp.web.command.HttpJson;
import br.edu.ifsp.web.command.SessaoUsuario;
import br.edu.ifsp.web.listener.AplicacaoListener;
import br.edu.ifsp.web.listener.SessoesListener;
import br.edu.ifsp.web.model.Usuario;
import br.edu.ifsp.web.service.AutenticacaoService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controlador de /auth. Escolha aqui: um controlador simples com 3 ações fixas
 * (sem registro de Commands). Justifique a sua escolha no relatório.
 */
@WebServlet(urlPatterns = "/auth/*")
public class AutenticacaoServlet extends HttpServlet {

    public static class Credenciais {
        public String email;
        public String senha;
    }

    private AutenticacaoService autenticacao() {
        return (AutenticacaoService) getServletContext().getAttribute(AplicacaoListener.AUTENTICACAO_SERVICE);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String acao = req.getPathInfo() == null ? "" : req.getPathInfo();
        try {
            switch (acao) {
                case "/login" -> login(req, resp);
                case "/logout" -> logout(req, resp);
                default -> HttpJson.erro(resp, 404, "Rota não encontrada");
            }
        } catch (Exception e) {
            HttpJson.erro(resp, e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!"/me".equals(req.getPathInfo())) {
            HttpJson.erro(resp, 404, "Rota não encontrada");
            return;
        }
        Usuario usuario = SessaoUsuario.de(req);
        if (usuario == null) {
            HttpJson.erro(resp, 401, "Não autenticado");
            return;
        }
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("usuario", usuario);
        corpo.put("sessoesAtivas", SessoesListener.sessoesAtivas());
        HttpJson.json(resp, 200, corpo);
    }

    private void login(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Credenciais in = HttpJson.lerCorpo(req, Credenciais.class);
        HttpSession sessao = req.getSession(true);        // sessão pré-login
        Usuario usuario = autenticacao().autenticar(in.email, in.senha); // lança se inválido
        req.changeSessionId();                            // novo identificador após autenticar
        sessao.setAttribute(SessaoUsuario.ATRIBUTO, usuario); // Usuario sem credencial
        HttpJson.json(resp, 200, usuario);
    }

    private void logout(HttpServletRequest req, HttpServletResponse resp) {
        HttpSession sessao = req.getSession(false);
        if (sessao != null) sessao.invalidate();
        resp.setStatus(204);
    }
}
