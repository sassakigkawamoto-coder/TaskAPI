package br.edu.ifsp.web.service;

/** Base das falhas que a Service Layer devolve aos clientes. */
public abstract class AplicacaoException extends RuntimeException {
    protected AplicacaoException(String mensagem) { super(mensagem); }
    protected AplicacaoException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
