package com.project.url_shortener.exception;

public class AliasAlreadyTakenException extends RuntimeException {

    public AliasAlreadyTakenException(String alias) {
        super("Custom alias already taken: " + alias);
    }
}
