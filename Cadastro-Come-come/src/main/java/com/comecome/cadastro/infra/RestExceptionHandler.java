package com.comecome.cadastro.infra;

import com.comecome.cadastro.exceptions.EmailAlreadyExistsException;
import com.comecome.cadastro.exceptions.NotAuthenticatedException;
import com.comecome.cadastro.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    private ResponseEntity<RestErrorMessage> UserNotFoundHandler(UserNotFoundException exception){
        RestErrorMessage error = new RestErrorMessage(HttpStatus.NOT_FOUND,exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(NotAuthenticatedException.class)
    private ResponseEntity<RestErrorMessage> NotAuthenticatedHandler(NotAuthenticatedException exception){
        RestErrorMessage error = new RestErrorMessage(HttpStatus.UNAUTHORIZED,exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    private ResponseEntity<RestErrorMessage> EmailAlreadyExistsHandler(EmailAlreadyExistsException exception){
        RestErrorMessage error = new RestErrorMessage(HttpStatus.CONFLICT,exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

}
