package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

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

    @InjectMocks
    private AccountService accountService; // AccountService에 모의 객체를 주입하며, 여기서 AccountService는 테스트 대상


    @Test
    void createAccountSuccess() {
        /*
         * - given
         *      accountRepositry의 findFirsyByOrderByIdDesc 와 save
         *      accountUserRepository의 findById 메서드가 호출될 때 반환할 객체를 설정
         * - when
         *      테스트 대상 메서드 AccountService.createAccount 메서드 호출
         * - then
         *      verify 사용하여 accountRepository.save()가 한 번 호출되었는지 확인하고,
         *      ArgumentCaputre를 사용하여 호출 시 전달된 Account 객체 capture.
         *      AccountDto의 userId와 accountNumber가 예상 값과 일치하는지 검증
         */

        //given
        // 사용자 엔티티 생성 및 accountUserRepository.findById 메서드 호출 시 반환할 객체 설정
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        // accountRepository.findFirstByOrderByIdDesc 메서드 호출 시 반환할 객체 설정
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                        .accountNumber("1000000012").build()));

        // accountRepository.save 메서드 호출 시 반환할 객체 설정
        given(accountRepository.save(any(Account.class)))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000013").build());

        // ArgumentCaptor를 사용하여 accountRepository.save() 메서드에 전달되는 Account 객체 포착
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        // 테스트 대상 메서드 실행
        accountService.createAccount(15L, 1000L);

        //then
        // accountRepository.save()가 호출되었는지 확인하고, 호출 시 전달된 인자 포착
        verify(accountRepository, times(1)).save(captor.capture());

        // 포착된 Account 객체의 값 검증

        Account capturedAccount = captor.getValue();
        System.out.println("Account ID: " + capturedAccount.getAccountUser().getId());
        System.out.println("Account Number: " + capturedAccount.getAccountNumber());

        Assertions.assertEquals(12L, capturedAccount.getAccountUser().getId()); // 사용자 ID 검증
        Assertions.assertEquals("1000000013", capturedAccount.getAccountNumber()); // 계좌 번호 검증
    }

    @Test
    void createFirstAccount() {
        // 유저의 첫번째 계좌 생성 테스트

        //given
        // 사용자 엔티티 생성 및 accountUserRepository.findById 메서드 호출 시 반환할 객체 설정
        AccountUser user = AccountUser.builder()
                .id(15L)
                .name("Pobi").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        // accountRepository.findFirstByOrderByIdDesc 메서드 호출 시 반환할 객체 설정
        // 첫번째 계좌 생성 전 이므로 empty 반환
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty());

        // accountRepository.save 메서드 호출 시 반환할 객체 설정
        given(accountRepository.save(any(Account.class)))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000000")
                        .build());

        // ArgumentCaptor를 사용하여 accountRepository.save() 메서드에 전달되는 Account 객체 포착
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        // 테스트 대상 메서드 실행
        accountService.createAccount(15L, 1000L);

        //then
        // accountRepository.save()가 호출되었는지 확인하고, 호출 시 전달된 인자 포착
        verify(accountRepository, times(1)).save(captor.capture());

        // 포착된 Account 객체의 값 검증
        Account capturedAccount = captor.getValue();
        System.out.println("Account ID: " + capturedAccount.getAccountUser().getId());
        System.out.println("Account Number: " + capturedAccount.getAccountNumber());

        Assertions.assertEquals(15L, capturedAccount.getAccountUser().getId()); // 사용자 ID 검증
        Assertions.assertEquals("1000000000", capturedAccount.getAccountNumber()); // 계좌 번호 검증
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
        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("생성 가능한 최대 계좌 수를 초과 - 10개")
    void createAccount_maxAccountIs10() {
        //given
        AccountUser user = AccountUser.builder()
                .id(15L)
                .name("Pobi").build();
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);

        //when
        AccountException exception = Assertions.assertThrows(AccountException.class,
                () -> accountService.createAccount(15L, 1000L));

        //then
        // exception 에러 코드가 예상과 일치하는지 확인
        Assertions.assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_10, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 해지 성공")
    void deleteAccountSuccess() {
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi").build();
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

        Assertions.assertEquals(12L, accountDto.getUserId());
        Assertions.assertEquals("1000000012", captor.getValue().getAccountNumber());
        Assertions.assertEquals(AccountStatus.UNREGISTERED, captor.getValue().getAccountStatus());
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
        Assertions.assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 없음 - 계좌 해지 실패")
    void deleteAccountFailed_AccountNotFound() {
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
                () -> accountService.deleteAccount(15L, "1234567890"));

        //then
        Assertions.assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("계좌 소유주 다름 - 계좌 해지 실패")
    void deleteAccountFailed_userUnMatch() {
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
                () -> accountService.deleteAccount(15L, "1234567890"));

        //then
        Assertions.assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("잔액이 남아있는 경우 - 계좌 해지 실패")
    void deleteAccountFailed_balanceNotEmpty() {
        //given
        AccountUser user = AccountUser.builder()
                .id(12L)
                .name("Pobi").build();
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
        Assertions.assertEquals(ErrorCode.BALANCE_NOT_EMPTY, exception.getErrorCode());
    }
}
