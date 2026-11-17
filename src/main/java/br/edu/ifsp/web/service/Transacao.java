package br.edu.ifsp.web.service;

import br.edu.ifsp.web.dao.ConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Decisão de projeto: cada operação de serviço abre a sua conexão e a sua transação.
 * Assim a mesma operação funciona na API e no importador, sem depender de filtro.
 * Commit se tudo der certo; rollback em qualquer falha.
 */
final class Transacao {
    @FunctionalInterface
    interface Trabalho<T> {
        T executar(Connection conn) throws SQLException;
    }

    private Transacao() { }

    static <T> T executar(Trabalho<T> trabalho) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                T resultado = trabalho.executar(conn);
                conn.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                desfazer(conn);
                throw e;
            }
        } catch (SQLException e) {
            throw new FalhaInternaException("Falha ao acessar o banco de dados", e);
        }
    }

    private static void desfazer(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignorada) {
            // a conexão será fechada de qualquer forma
        }
    }
}
