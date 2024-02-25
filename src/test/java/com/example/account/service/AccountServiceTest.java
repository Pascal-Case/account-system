package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.example.account.type.AccountStatus.UNREGISTERED;
import static com.example.account.type.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


// Mockito 확장 기능을 사용하여 Mockito 애노테이션을 활성화
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository; // AccountRepository에 대한 모의 객체를 생성

    @Mock
    private AccountUserRepository accountUserRepository; // AccountUserRepository에 대한 모의 객체를 생성

    @Mock
    private AccountNumberGenerator accountNumberGenerator;

    @InjectMocks
    private AccountService accountService; // AccountService에 모의 객체를 주입하며, 여기서 AccountService는 테스트 대상

    @Test
    @DisplayName("계좌 생성 성공")
    void createAccountSuccess() {
        //given
        Long userId = 12L;
        Long initialBalance = 1000L;
        AccountUser user = AccountUser.builder().name("Pobi").build();
        user.setId(userId);
        given(accountUserRepository.findById(userId)).willReturn(Optional.of(user));

        String expectedAccountNumber = "uniqueAccountNumber";
        given(accountNumberGenerator.generateUniqueAccountNumber()).willReturn(expectedAccountNumber);

        Account account = Account.builder()
                .accountUser(user)
                .accountNumber(expectedAccountNumber)
                .balance(initialBalance)
                .accountStatus(AccountStatus.IN_USE)
                .registeredAt(LocalDateTime.now())
                .build();
        given(accountRepository.save(any(Account.class))).willReturn(account);

        // when
        AccountDto result = accountService.createAccount(userId, initialBalance);

        // then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(expectedAccountNumber, result.getAccountNumber());
        assertEquals(initialBalance, result.getBalance());
    }

    @Test
    @DisplayName("해당 유저 없음 - 계좌 생성 실패")
    void createAccount_UserNotFount() {

        //given
        // findById 결과로 empty 반환 -> exceptioon 발생 할 것으로 예상
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> accountService.createAccount(15L, 1000L));

        //then
        // exception 에러 코드가 예상과 일치하는지 확인
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("생성 가능한 최대 계좌 수를 초과 - 10개")
    void createAccount_maxAccountIs10() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(15L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);

        //when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> accountService.createAccount(15L, 1000L));

        //then
        // exception 에러 코드가 예상과 일치하는지 확인
        assertEquals(MAX_ACCOUNT_PER_USER_10, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 해지 성공")
    void deleteAccountSuccess() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .balance(0L)
                        .accountNumber("1000000012").build()));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountDto accountDto = accountService.deleteAccount(15L, "12345678910");

        //then
        verify(accountRepository, times(1)).save(captor.capture());

        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000012", captor.getValue().getAccountNumber());
        assertEquals(UNREGISTERED, captor.getValue().getAccountStatus());
    }

    @Test
    @DisplayName("해당 유저 없음 - 계좌 해지 실패")
    void deleteAccountFailed_UserNotFount() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> accountService.deleteAccount(15L, "1234567890"));

        //then
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 없음 - 계좌 해지 실패")
    void deleteAccountFailed_AccountNotFound() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> accountService.deleteAccount(15L, "1234567890"));

        //then
        assertEquals(ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 소유주 다름 - 계좌 해지 실패")
    void deleteAccountFailed_userUnMatch() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(12L);

        AccountUser otherUser = AccountUser.builder()
                .name("Harry").build();
        otherUser.setId(13L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(otherUser)
                        .balance(0L)
                        .accountNumber("1000000012").build()));

        //when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> accountService.deleteAccount(15L, "1234567890"));

        //then
        assertEquals(USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액이 남아있는 경우 - 계좌 해지 실패")
    void deleteAccountFailed_balanceNotEmpty() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .balance(100L)
                        .accountNumber("1000000012").build()));

        //when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> accountService.deleteAccount(15L, "1234567890"));

        //then
        assertEquals(BALANCE_NOT_EMPTY, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌가 이미 해지된 경우 - 계좌 해지 실패")
    void deleteAccountFailed_alreadyUnregistered() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(12L);
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
                () -> accountService.deleteAccount(15L, "1234567890"));

        //then
        assertEquals(ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
    }

    @Test
    @DisplayName("특정 사용자 ID로 계좌 목록 조회 성공")
    void successGetAccountsByUserId() {
        //given

        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        List<Account> accounts = Arrays.asList(
                Account.builder()
                        .accountUser(user)
                        .accountNumber("1111111111")
                        .balance(1000L)
                        .build(),
                Account.builder()
                        .accountUser(user)
                        .accountNumber("1111111112")
                        .balance(2000L)
                        .build(),
                Account.builder()
                        .accountUser(user)
                        .accountNumber("1111111113")
                        .balance(3000L)
                        .build()
        );

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountUser(any()))
                .willReturn(accounts);
        //when
        List<AccountDto> accountDtoList = accountService.getAccountsByUserId(1L);

        //then
        assertEquals(3, accountDtoList.size());
        assertEquals("1111111111", accountDtoList.get(0).getAccountNumber());
        assertEquals(1000, accountDtoList.get(0).getBalance());
        assertEquals("1111111112", accountDtoList.get(1).getAccountNumber());
        assertEquals(2000, accountDtoList.get(1).getBalance());
        assertEquals("1111111113", accountDtoList.get(2).getAccountNumber());
        assertEquals(3000, accountDtoList.get(2).getBalance());
    }

    @Test
    void failedToGetAccounts() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        //when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> accountService.getAccountsByUserId(1L));

        //then
        // exception 에러 코드가 예상과 일치하는지 확인
        assertEquals(USER_NOT_FOUND, exception.getErrorCode());
    }


}

