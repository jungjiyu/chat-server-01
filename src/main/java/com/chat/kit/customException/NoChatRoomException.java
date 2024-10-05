package com.chat.kit.customException;

public class NoChatRoomException extends RuntimeException{
    public NoChatRoomException() {
        super();
    }

    public NoChatRoomException(String message) {
        super(message);
    }
}
