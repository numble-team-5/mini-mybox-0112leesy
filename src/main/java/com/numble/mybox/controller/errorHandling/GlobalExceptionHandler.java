package com.numble.mybox.controller.errorHandling;

import com.numble.mybox.data.dto.ErrorResponseDto;
import com.numble.mybox.exception.ObjectAlreadyExistsException;
import com.numble.mybox.exception.ObjectNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleObjectNotFoundException(ObjectNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponseDto(e.getMessage()));
    }

    @ExceptionHandler(ObjectAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleObjectAlreadyExistsException(ObjectAlreadyExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponseDto(e.getMessage()));
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponseDto> handleException(Exception e) {
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDto("Internal server error"));
//    }

}
