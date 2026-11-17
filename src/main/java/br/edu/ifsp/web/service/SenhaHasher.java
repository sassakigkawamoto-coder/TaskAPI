package br.edu.ifsp.web.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/** PBKDF2 do próprio JDK. Formato: pbkdf2$iteracoes$salt$hash (Base64). */
public final class SenhaHasher {
    private static final int ITERACOES = 120_000;
    private static final int BITS = 256;

    private SenhaHasher() { }

    public static String gerar(String senha) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = derivar(senha, salt, ITERACOES);
        return "pbkdf2$" + ITERACOES + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean conferir(String senha, String armazenado) {
        if (armazenado == null) return false;
        String[] p = armazenado.split("\\$");
        if (p.length != 4 || !p[0].equals("pbkdf2")) return false;
        byte[] esperado = Base64.getDecoder().decode(p[3]);
        byte[] obtido = derivar(senha, Base64.getDecoder().decode(p[2]), Integer.parseInt(p[1]));
        return MessageDigest.isEqual(esperado, obtido);
    }

    private static byte[] derivar(String senha, byte[] salt, int iteracoes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), salt, iteracoes, BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (java.security.NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 indisponível", e);
        }
    }
}
