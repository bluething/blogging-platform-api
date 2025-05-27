package io.github.bluething.playground.java.bloggingplatformapi.exception;

public class BadRequestException extends ApplicationException {
    protected BadRequestException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }
}
