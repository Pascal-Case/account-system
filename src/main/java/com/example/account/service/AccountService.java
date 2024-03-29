package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.account.type.ErrorCode.*;


@Service // 스프링 프레임워크에 이 클래스가 서비스 계층의 컴포넌트임을 알리고, 빈으로 관리될 것임을 선언
@RequiredArgsConstructor // Lombok 라이브러리를 사용하여 final로 선언된 모든 필드에 대한 생성자를 자동으로 생성
@Slf4j
public class AccountService {
    private final AccountRepository accountRepository; // Account 엔티티에 대한 CRUD 연산을 담당하는 JPA 리포지토리
    private final AccountUserRepository accountUserRepository; // AccountUser 엔티티에 대한 CRUD 연산을 담당하는 JPA 리포지토리
    private final AccountNumberGenerator accountNumberGenerator;

    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance) {
        // 사용자 존재 여부 확인, 없을 경우 사용자 없음 예외 발생
        AccountUser accountUser = getAccountUser(userId);

        validateCreateAccount(accountUser);

        // 중복이 없는 랜덤 계좌번호 발행
        String newAccountNumber = accountNumberGenerator.generateUniqueAccountNumber();
        // 계좌 생성 및 저장
        Account savedAccount = accountRepository.save(
                Account.builder()
                        .accountUser(accountUser) // 계좌 소유자 설정
                        .accountStatus(AccountStatus.IN_USE) // 계좌 상태를 '사용 중'으로 설정
                        .accountNumber(newAccountNumber) // 새 계좌 번호 설정
                        .balance(initialBalance) // 초기 잔액 설정
                        .registeredAt(LocalDateTime.now()) // 계좌 등록 시간을 현재 시간으로 설정
                        .build());

        // 생성된 계좌 정보를 AccountDto 객체로 변환하여 반환
        return AccountDto.fromEntity(savedAccount);
    }

    private AccountUser getAccountUser(Long userId) {
        return accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));
    }

    private void validateCreateAccount(AccountUser accountUser) {
        if (accountRepository.countByAccountUser(accountUser) >= 10) {
            throw new AccountException(MAX_ACCOUNT_PER_USER_10);
        }
    }

    @Transactional
    public AccountDto deleteAccount(Long userId, String accountNumber) {
        // 사용자가 없는 경우
        AccountUser accountUser = getAccountUser(userId);

        // 계좌가 없는 경우
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));


        validateDeleteAccount(accountUser, account);

        // 계좌 상태 해지로 변경하고 날짜 갱신
        account.setAccountStatus(AccountStatus.UNREGISTERED);
        account.setUnRegisteredAt(LocalDateTime.now());

        accountRepository.save(account);

        return AccountDto.fromEntity(account);
    }

    private void validateDeleteAccount(AccountUser accountUser, Account account) {
        // 계좌 소유주가 다른 경우
        if (!Objects.equals(accountUser.getId(), account.getAccountUser().getId())) {
            log.error(USER_ACCOUNT_UN_MATCH.getDescription());
            throw new AccountException(USER_ACCOUNT_UN_MATCH);
        }
        // 이미 해지 상태인 경우
        if (account.getAccountStatus() == AccountStatus.UNREGISTERED) {
            log.error(ACCOUNT_ALREADY_UNREGISTERED.getDescription());
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
        }
        // 잔액이 있는 경우
        if (account.getBalance() > 0) {
            log.error(BALANCE_NOT_EMPTY.getDescription());
            throw new AccountException(BALANCE_NOT_EMPTY);
        }
    }

    @Transactional
    public List<AccountDto> getAccountsByUserId(Long userId) {
        AccountUser accountUser = getAccountUser(userId);

        List<Account> accounts = accountRepository.findByAccountUser(accountUser);


        // List<Account> -> List<AccountDto>
        return accounts.stream()
                .map(AccountDto::fromEntity)
                .collect(Collectors.toList());
    }
}
