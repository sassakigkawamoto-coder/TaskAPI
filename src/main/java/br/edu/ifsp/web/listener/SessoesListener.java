package br.edu.ifsp.web.listener;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

import java.util.concurrent.atomic.AtomicInteger;

@WebListener
public class SessoesListener implements HttpSessionListener {
    private static final AtomicInteger ATIVAS = new AtomicInteger();

    public static int sessoesAtivas() { return ATIVAS.get(); }

    @Override
    public void sessionCreated(HttpSessionEvent e) {
        int n = ATIVAS.incrementAndGet();
        System.out.println("[SESSAO] criada   id=" + e.getSession().getId() + " | ativas=" + n);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent e) {
        int n = ATIVAS.decrementAndGet();
        System.out.println("[SESSAO] destruída id=" + e.getSession().getId() + " | ativas=" + n);
    }
}
