package br.edu.ifsp.web.command;

import br.edu.ifsp.web.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** A sessão guarda só o Usuario sem credencial. Lê sem nunca criar sessão. */
public final class SessaoUsuario {
    public static final String ATRIBUTO = "usuario";

    private SessaoUsuario() { }

    public static Usuario de(HttpServletRequest req) {
        HttpSession sessao = req.getSession(false);
        return sessao == null ? null : (Usuario) sessao.getAttribute(ATRIBUTO);
    }
}
