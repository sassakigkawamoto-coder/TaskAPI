package br.edu.ifsp.web.service;

/** Falha de infraestrutura (banco, etc.). Substitui SQLException para os clientes. */
public class FalhaInternaException extends AplicacaoException {
    public FalhaInternaException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
