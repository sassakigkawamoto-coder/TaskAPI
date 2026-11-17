package br.edu.ifsp.web.filter;

import br.edu.ifsp.web.command.HttpJson;
import br.edu.ifsp.web.command.SessaoUsuario;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Declarado no web.xml (ordem importa). Mapeado em /tarefas/*. Não cria sessão. */
public class FiltroAutenticacao implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        if (SessaoUsuario.de((HttpServletRequest) req) == null) {
            HttpJson.erro((HttpServletResponse) resp, 401, "Não autenticado");
            return; // não chega ao controlador
        }
        chain.doFilter(req, resp);
    }
}
