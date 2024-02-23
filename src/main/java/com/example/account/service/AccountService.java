package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service // 스프링에게 이 클래스가 서비스 계층의 컴포넌트임을 알림
@RequiredArgsConstructor // Lombok을 사용하여 final 필드에 대한 생성자를 자동으로 생성
public class AccountService {
    private final AccountRepository accountRepository; // 계좌 관련 데이터 접근을 위한 리포지토리
    private final AccountUserRepository accountUserRepository; // 계좌 사용자 관련 데이터 접근을 위한 리포지토리

    /**
     * 새 계좌를 생성하는 메서드.
     * 사용자 존재 여부를 확인하고, 새 계좌 번호를 생성하여 계좌를 생성 및 저장한다.
     *
     * @param userId         사용자 ID
     * @param initialBalance 초기 잔액
     * @return 생성된 계좌의 정보
     */
    @Transactional // 메서드 실행을 트랜잭션으로 처리. 메서드 실행 중 오류 발생 시 롤백
    public Account createAccount(Long userId, Long initialBalance) {
        // 사용자 조회. 존재하지 않을 경우 사용자 없음 오류 발생
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));

        // 가장 최근 계좌번호를 조회하여 새 계좌번호 생성. 첫 계좌인 경우 기본값 설정
        String newAccountNumber = accountRepository.findFirstByOrderByIdDesc()
                .map(account -> (Integer.parseInt(account.getAccountNumber())) + 1 + "")
                .orElse("1000000000");

        // 새 계좌 객체 생성 및 저장 후 반환
        return accountRepository.save(
                Account.builder()
                        .accountUser(accountUser) // 계좌 소유자 설정
                        .accountStatus(AccountStatus.IN_USE) // 계좌 상태를 사용 중으로 설정
                        .accountNumber(newAccountNumber) // 새 계좌 번호 설정
                        .balance(initialBalance) // 초기 잔액 설정
                        .registeredAt(LocalDateTime.now()) // 등록 시간을 현재 시간으로 설정
                        .build()
        );
    }
}
