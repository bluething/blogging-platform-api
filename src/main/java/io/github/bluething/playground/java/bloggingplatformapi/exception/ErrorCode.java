package io.github.bluething.playground.java.bloggingplatformapi.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    BAD_REQUEST(400, "Bad Request"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_ERROR(500, "Internal Server Error");

    private final int status;
    private final String reason;

    ErrorCode(int status, String reason) {
        this.status = status;
        this.reason = reason;
    }
}
