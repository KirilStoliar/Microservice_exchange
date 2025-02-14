package com.stoliar.microservice_exchange.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    @Operation(
            summary = "Домашняя страница",
            description = "Возвращает приветственное сообщение.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Приветственное сообщение",
                            content = @Content(schema = @Schema(implementation = String.class))
                    )
            }
    )
    public String home() {
        return "Welcome to the Microservice Exchange by Stoliar Kiril!";
    }
}
