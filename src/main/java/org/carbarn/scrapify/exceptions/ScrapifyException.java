package org.carbarn.scrapify.exceptions;

public class ScrapifyException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ScrapifyException(String message) {
        super(message);
    }
}