package br.edu.ifsp.web.listener;

import br.edu.ifsp.web.service.AutenticacaoServiceImpl;
import br.edu.ifsp.web.service.TarefaServiceImpl;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Único ponto da API que conhece as implementações. Se já tem um listener de aplicação
 * da Tarefa 02, copie o corpo de contextInitialized para ele em vez de criar este.
 */
@WebListener
public class AplicacaoListener implements ServletContextListener {
    public static final String TAREFA_SERVICE = "tarefaService";
    public static final String AUTENTICACAO_SERVICE = "autenticacaoService";

    @Override
    public void contextInitialized(ServletContextEvent e) {
        e.getServletContext().setAttribute(TAREFA_SERVICE, new TarefaServiceImpl());
        e.getServletContext().setAttribute(AUTENTICACAO_SERVICE, new AutenticacaoServiceImpl());
    }
}
