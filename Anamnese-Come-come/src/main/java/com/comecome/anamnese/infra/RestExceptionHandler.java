package com.comecome.anamnese.infra;

import com.comecome.anamnese.exceptions.AnamneseNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AnamneseNotFoundException.class)
    private ResponseEntity<RestErrorMessage> anamneseNotFoundHandler(AnamneseNotFoundException anamneseNotFoundException){
        RestErrorMessage error = new RestErrorMessage(HttpStatus.NOT_FOUND,anamneseNotFoundException.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
