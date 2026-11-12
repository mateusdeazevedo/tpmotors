package com.tpmotors.controller;

import com.tpmotors.exception.ErrorResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<ErrorResponse> handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object originalUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        String path = originalUri != null ? originalUri.toString() : request.getRequestURI();

        int statusCode = HttpStatus.NOT_FOUND.value();
        String errorName = "Not Found";
        String message = "A rota solicitada não existe no servidor TPMotors.";

        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
            try {
                HttpStatus httpStatus = HttpStatus.valueOf(statusCode);
                errorName = httpStatus.getReasonPhrase();
            } catch (Exception ignored) {
            }

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                message = "Recurso ou rota não encontrada: " + path;
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                message = "Acesso negado para o recurso solicitado.";
            } else if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
                message = "Autenticação requerida.";
            } else if (statusCode == HttpStatus.TOO_MANY_REQUESTS.value()) {
                message = "Taxa de requisições excedida (Rate Limit).";
            }
        }

        ErrorResponse response = new ErrorResponse(statusCode, errorName, message, path);
        return ResponseEntity.status(statusCode).body(response);
    }
}
