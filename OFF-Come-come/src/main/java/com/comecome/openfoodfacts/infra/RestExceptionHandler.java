package com.comecome.openfoodfacts.infra;

import com.comecome.openfoodfacts.exceptions.FoodNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(FoodNotFoundException.class)
    private ResponseEntity<RestErrorMessage> foodNotFoundException (FoodNotFoundException exception){
        RestErrorMessage error = new RestErrorMessage(HttpStatus.NOT_FOUND,exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
