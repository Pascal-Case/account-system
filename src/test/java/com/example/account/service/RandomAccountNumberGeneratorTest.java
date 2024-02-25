package com.example.account.service;

import com.example.account.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RandomAccountNumberGeneratorTest {
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private RandomAccountNumberGenerator generator;

    @Test
    void generateUniqueAccountNumber_generatesNumber() {
        // 계좌 번호가 중복되지 않는 경우를 시뮬레이션
        when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.empty());


        String accountNumber = generator.generateUniqueAccountNumber();

        // 계좌 번호가 생성되었는지 확인
        assertNotNull(accountNumber);
    }


}