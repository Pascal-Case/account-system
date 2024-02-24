package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import com.example.account.dto.TransactionDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.TransactionResultType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.example.account.type.ErrorCode.*;
import static com.example.account.type.TransactionResultType.F;
import static com.example.account.type.TransactionResultType.S;
import static com.example.account.type.TransactionType.USE;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountUserRepository accountUserRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public TransactionDto useBalance(Long userId, String accountNumber, Long amount) {

        AccountUser user = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND)); // 사용자가 없음
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND)); // 계좌가 없음

        validateUseBalance(user, account, amount);

        // 잔액 사용
        account.useBalance(amount);

        return TransactionDto.fromEntity(saveAndGetTransaction(S, account, amount));
    }

    private void validateUseBalance(AccountUser user, Account account, Long amount) {
        // 계좌 소유주 불일치
        if (!Objects.equals(user.getId(), account.getAccountUser().getId())) {
            throw new AccountException(USER_ACCOUNT_UN_MATCH);
        }

        // 해지된 계좌인 경우
        if (account.getAccountStatus() != AccountStatus.IN_USE) {
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
        }

        // 잔액 보다 큰 금액 사용
        if (account.getBalance() < amount) {
            throw new AccountException(AMOUNT_EXCEED_BALANCE);
        }

    }

    @Transactional
    public void saveFailedUseTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        TransactionDto.fromEntity(saveAndGetTransaction(F, account, amount));
    }

    private Transaction saveAndGetTransaction(
            TransactionResultType transactionResultType,
            Account account,
            Long amount) {
        return transactionRepository.save(
                Transaction.builder()
                        .transactionType(USE)
                        .transactionResultType(transactionResultType)
                        .account(account)
                        .amount(amount)
                        .balanceSnapShot(account.getBalance())
                        .transactionId(UUID.randomUUID().toString().replace("-", ""))
                        .transactedAt(LocalDateTime.now())
                        .build()
        );
    }
}
