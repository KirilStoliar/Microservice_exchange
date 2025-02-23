package com.stoliar.microservice_exchange.controllers;

import com.stoliar.microservice_exchange.dto.ExceededTransactionResponseDTO;
import com.stoliar.microservice_exchange.entities.Transaction;
import com.stoliar.microservice_exchange.services.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/client")
@Tag(name = "Client API", description = "API для работы с транзакциями клиентов")
public class ClientController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/transactions")
    @Operation(
            summary = "Получить все транзакции",
            description = "Возвращает список всех транзакций.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Список транзакций",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Transaction.class)
                            )
                    )
            }
    )
    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/transaction/{id}")
    @Operation(
            summary = "Получить транзакцию по ID",
            description = "Возвращает транзакцию по указанному идентификатору.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Транзакция найдена",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Transaction.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Транзакция не найдена",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    public Transaction getTransactionById(
            @Parameter(description = "Идентификатор транзакции", example = "1", required = true)
            @PathVariable Long id) {
        Transaction transaction = transactionService.getTransactionById(id);
        if (transaction == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found");
        }
        return transaction;
    }

    @GetMapping("/exceeded-transactions")
    @Operation(
            summary = "Получить список транзакций, превысивших лимит",
            description = "Возвращает список транзакций, которые превысили установленный лимит, с указанием лимита.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Список транзакций",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ExceededTransactionResponseDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Некорректный запрос",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    public List<ExceededTransactionResponseDTO> getExceededTransactions(
            @Parameter(description = "Идентификатор счёта отправителя", example = "1234567890", required = true)
            @RequestParam Long accountFrom) {
        return transactionService.getExceededTransactions(accountFrom);
    }
}
