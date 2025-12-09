package com.backend.favorite.infra;

import com.backend.favorite.exceptions.CategoryAlreadyAddedException;
import com.backend.favorite.exceptions.CategoryNotFoundException;
import com.backend.favorite.exceptions.FavoriteAlreadyAddedException;
import com.backend.favorite.exceptions.FavoriteNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CategoryNotFoundException.class)
    private ResponseEntity<RestErrorMessage> CategoryNotFoundHandler(CategoryNotFoundException exception){
        RestErrorMessage error = new RestErrorMessage(HttpStatus.NOT_FOUND,exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(FavoriteAlreadyAddedException.class)
    private ResponseEntity<RestErrorMessage> FavoriteAlreadyAddedHandler(FavoriteAlreadyAddedException exception){
        RestErrorMessage error = new RestErrorMessage(HttpStatus.CONFLICT,exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(FavoriteNotFoundException.class)
    private ResponseEntity<RestErrorMessage> FavoriteNotFoundHandler(FavoriteNotFoundException exception){
        RestErrorMessage error = new RestErrorMessage(HttpStatus.NOT_FOUND,exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(CategoryAlreadyAddedException.class)
    private ResponseEntity<RestErrorMessage> CategoryAlreadyAddedHandler(CategoryAlreadyAddedException exception){
        RestErrorMessage error = new RestErrorMessage(HttpStatus.CONFLICT, exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }


}
