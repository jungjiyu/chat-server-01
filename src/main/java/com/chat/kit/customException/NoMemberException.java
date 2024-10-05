package com.chat.kit.customException;

public class NoMemberException extends RuntimeException{
    public NoMemberException() {
        super();
    }

    public NoMemberException(String message) {
        super(message);
    }
}
