package de.hpi.swa.trufflelsp.exceptions;

public class UnknownLanguageException extends RuntimeException {

    private static final long serialVersionUID = -4681567232393674256L;

    public UnknownLanguageException(String message) {
        super(message);
    }
}
