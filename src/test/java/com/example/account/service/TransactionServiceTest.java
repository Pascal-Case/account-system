package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import com.example.account.dto.TransactionDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.account.type.AccountStatus.IN_USE;
import static com.example.account.type.AccountStatus.UNREGISTERED;
import static com.example.account.type.ErrorCode.*;
import static com.example.account.type.TransactionResultType.F;
import static com.example.account.type.TransactionResultType.S;
import static com.example.account.type.TransactionType.USE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


// Mockito 확장 기능을 사용하여 Mockito 애노테이션을 활성화
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void successUseBalance() {
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi").build();

        // 사용자 조회에 대한 모킹 처리
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000012").build();

        // 계좌 번호로 계좌 조회에 대한 모킹 처리
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // 거래 저장에 대한 모킹 처리
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(USE)
                        .transactionResultType(S)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .amount(1000L)
                        .balanceSnapShot(9000L)
                        .build()

                );

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        //when
        TransactionDto transactionDto = transactionService.useBalance(
                1L, "1000000000", 200L);
        //then
        verify(transactionRepository, times(1)).save(captor.capture());

        assertEquals(200L, captor.getValue().getAmount());
        assertEquals(9800L, captor.getValue().getBalanceSnapShot());


        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(USE, transactionDto.getTransactionType());
        assertEquals(9000L, transactionDto.getBalanceSnapShot());
        assertEquals(1000L, transactionDto.getAmount());

    }

    @Test
    @DisplayName("해당 유저 없음 - 잔액 사용 실패")
    void useBalanceFailed_UserNotFount() {

        //given
        // findById 결과로 empty 반환 -> exceptioon 발생 할 것으로 예상
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1234567890", 1000L));

        //then
        // exception 에러 코드가 예상과 일치하는지 확인
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 없음 - 잔액 사용 실패")
    void useBalanceFailed_AccountNotFound() {
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1234567890", 1000L));

        //then
        assertEquals(ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 소유주 다름 - 잔액 사용 실패")
    void useBalanceFailed_userUnMatch() {
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi").build();
        AccountUser otherUser = AccountUser.builder()
                .id(13L)
                .name("Harry").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(otherUser)
                        .balance(0L)
                        .accountNumber("1000000012").build()));

        //when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1234567890", 1000L));

        //then
        assertEquals(USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌가 이미 해지된 경우 - 잔액 사용 실패")
    void useBalanceFailed__alreadyUnregistered() {
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .accountStatus(UNREGISTERED)
                        .balance(0L)
                        .accountNumber("1000000012").build()));

        //when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1234567890", 1000L));

        //then
        assertEquals(ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
    }

    @Test
    @DisplayName("거래 금액이 잔액보다 큰 경우 - 잔액 사용 실패")
    void useBalanceFailed_exceedAmount() {
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi").build();

        // 사용자 조회에 대한 모킹 처리
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(100L)
                .accountNumber("1000000012").build();

        // 계좌 번호로 계좌 조회에 대한 모킹 처리
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        //when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1234567890", 1000L));

        //then
        assertEquals(AMOUNT_EXCEED_BALANCE, exception.getErrorCode());
        verify(transactionRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("실패 트랜잭션 저장 성공")
    void saveFailedUseTransactionSuccess() {
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi").build();

        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .balance(10000L)
                .accountNumber("1000000012").build();
        
        // 계좌 번호로 계좌 조회에 대한 모킹 처리
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // 거래 저장에 대한 모킹 처리
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(USE)
                        .transactionResultType(F)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .amount(1000L)
                        .balanceSnapShot(9000L)
                        .build()
                );

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        //when
        transactionService.saveFailedUseTransaction("1000000000", 200L);

        //then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(200L, captor.getValue().getAmount());
        assertEquals(10000L, captor.getValue().getBalanceSnapShot());
        assertEquals(F, captor.getValue().getTransactionResultType());

    }
}

