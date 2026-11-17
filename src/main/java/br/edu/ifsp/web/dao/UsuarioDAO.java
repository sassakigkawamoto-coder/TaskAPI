package br.edu.ifsp.web.dao;

import br.edu.ifsp.web.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UsuarioDAO {
    private final Connection conn;

    public UsuarioDAO(Connection conn) { this.conn = conn; }

    public Optional<Usuario> buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT id, nome, email, senha_hash FROM usuarios WHERE email = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Usuario(rs.getLong("id"), rs.getString("nome"),
                        rs.getString("email"), rs.getString("senha_hash")));
            }
        }
    }
}
