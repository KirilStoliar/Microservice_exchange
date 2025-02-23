package com.stoliar.microservice_exchange.controllers;

import com.stoliar.microservice_exchange.entities.Limit;
import com.stoliar.microservice_exchange.entities.Transaction;
import com.stoliar.microservice_exchange.services.LimitService;
import com.stoliar.microservice_exchange.services.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bank")
@Tag(name = "Bank API", description = "API для работы с транзакциями и лимитами")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class BankController {

    private final TransactionService transactionService;

    private final LimitService limitService;

    @PostMapping("/transaction")
    @Operation(
            summary = "Добавить транзакцию",
            description = "Добавляет новую транзакцию в систему.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Транзакция успешно добавлена",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Transaction.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Некорректный запрос",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    public Transaction addTransaction(
            @Parameter(description = "Добавление транзакции", required = true)
            @RequestBody Transaction transaction) {
        return transactionService.saveTransaction(transaction);
    }

    @PostMapping("/limit")
    @Operation(
            summary = "Установить лимит",
            description = "Устанавливает новый лимит для указанного счёта и категории расходов.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Лимит успешно установлен",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Limit.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Некорректный запрос",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    public Limit setLimit(
            @Parameter(description = "Установка лимита", required = true)
            @RequestBody Limit limit) {
        return limitService.setLimit(limit);
    }
}
