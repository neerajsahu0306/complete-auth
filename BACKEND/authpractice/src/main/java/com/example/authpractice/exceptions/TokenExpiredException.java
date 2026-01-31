package com.example.authpractice.exceptions;

public class TokenExpiredException extends RuntimeException{

    public TokenExpiredException(String message){
        super(message);
    }
}
