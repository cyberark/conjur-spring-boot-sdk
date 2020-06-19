package com.db.cso.conjur.exception;

public class AuthenticationException extends Exception {
    public AuthenticationException(Exception e) {
        super(e);
    }
}
