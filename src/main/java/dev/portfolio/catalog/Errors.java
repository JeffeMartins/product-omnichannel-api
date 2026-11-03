package dev.portfolio.catalog;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class Errors {
  @ExceptionHandler(ConstraintViolationException.class)
  ResponseEntity<ProblemDetail> invalid(ConstraintViolationException ex) {
    return ResponseEntity.badRequest()
        .body(
            ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request parameters"));
  }

  @ExceptionHandler(DataAccessException.class)
  ResponseEntity<ProblemDetail> unavailable(DataAccessException ex) {
    return ResponseEntity.status(503)
        .body(
            ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE, "Catalog temporarily unavailable"));
  }
}
