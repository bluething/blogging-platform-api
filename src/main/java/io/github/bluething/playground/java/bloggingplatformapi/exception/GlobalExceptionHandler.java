package io.github.bluething.playground.java.bloggingplatformapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
final class GlobalExceptionHandler {
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiError> handle(ApplicationException ex, HttpServletRequest request) {
        ApiError error = ApiError.builder()
                .status(ex.getErrorCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(ex.getErrorCode().getStatus()).body(error);
    }
    @ExceptionHandler({MethodArgumentNotValidException.class,
    BindException.class})
    public ResponseEntity<ApiError> handle(Exception ex, HttpServletRequest request) {
        List<String> details = ex instanceof MethodArgumentNotValidException mex ?
                mex.getBindingResult().getFieldErrors().stream()
                        .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                        .toList()
                : ((BindException) ex).getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        ApiError error = ApiError.builder()
                .status(ErrorCode.BAD_REQUEST)
                .message("Validation failed")
                .errors(details)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex, HttpServletRequest request) {
        log.error(ex.getMessage(), ex);

        ApiError error = ApiError.builder()
                .status(ErrorCode.INTERNAL_ERROR)
                .message("An unexpected error occurred")
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(500).body(error);
    }

}
