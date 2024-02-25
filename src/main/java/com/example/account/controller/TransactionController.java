package com.example.account.controller;

import com.example.account.dto.CancelBalance;
import com.example.account.dto.QueryTransactionResponse;
import com.example.account.dto.UseBalance;
import com.example.account.exception.AccountException;
import com.example.account.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 잔액 관련 컨트롤러
 * 1. 거래
 * 2. 거래 취소
 * 3. 거래 확인
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    /**
     * 잔액을 사용하는 거래를 처리
     *
     * @param request 잔액 사용 요청에 대한 데이터를 담고 있는 DTO 객체
     * @return 거래가 성공적으로 완료되면, 거래 후 잔액 정보를 담은 응답 DTO 반환
     * @throws AccountException 계좌 관련 예외가 발생하면, 예외 처리 후 로그 기록하고 예외를 다시 throw
     */
    @PostMapping("/transaction/use")
    public UseBalance.Response useBalance(
            @Valid @RequestBody UseBalance.Request request
    ) {
        try {
            // TransactionService를 통해 잔액 사용 처리 후, 성공 응답 반환
            return UseBalance.Response.from(
                    transactionService.useBalance(
                            request.getUserId(),
                            request.getAccountNumber(),
                            request.getAmount()
                    )
            );
        } catch (AccountException e) {
            // 잔액 사용 처리 중 발생한 예외를 로그에 기록 후, 실패한 거래 정보 저장
            log.error("Failed to use balance. ");
            transactionService.saveFailedUseTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );

            throw e; // 처리 중 발생한 예외를 다시 throw하여 상위로 전파
        }
    }

    @PostMapping("/transaction/cancel")
    public CancelBalance.Response cancelBalance(
            @Valid @RequestBody CancelBalance.Request request
    ) {
        try {
            // TransactionService를 통해 잔액 취소 처리 후, 성공 응답 반환
            return CancelBalance.Response.from(
                    transactionService.cancelBalance(
                            request.getTransactionId(),
                            request.getAccountNumber(),
                            request.getAmount()
                    )
            );
        } catch (AccountException e) {
            // 잔액 사용 취소 처리 중 발생한 예외를 로그에 기록 후, 실패한 거래 취소 정보 저장
            log.error("Failed to cancel balance. ");
            transactionService.saveFailedCancelTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );

            throw e; // 처리 중 발생한 예외를 다시 throw하여 상위로 전파
        }
    }

    @GetMapping("/transaction/{transactionId}")
    public QueryTransactionResponse queryTransaction(
            @PathVariable String transactionId
    ) {
        return QueryTransactionResponse.from(
                transactionService.queryTransaction(transactionId)
        );
    }
}
