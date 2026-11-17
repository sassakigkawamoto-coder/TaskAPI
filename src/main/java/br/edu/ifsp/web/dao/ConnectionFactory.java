package br.edu.ifsp.web.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Usa DriverManager para funcionar dentro e fora do Tomcat. Ajuste ao seu da Tarefa 02. */
public class ConnectionFactory {
    private static final String URL = System.getProperty("db.url",
            "jdbc:mysql://localhost:3306/taskdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    private static final String USUARIO = System.getenv().getOrDefault("DB_USER", "root");
    private static final String SENHA = System.getenv().getOrDefault("DB_SENHA", "");

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }
}
