package br.edu.ifsp.web.service;

public class DadosInvalidosException extends AplicacaoException {
    public DadosInvalidosException(String mensagem) { super(mensagem); }
    public DadosInvalidosException(String mensagem, Throwable causa) { super(mensagem, causa); }
}
