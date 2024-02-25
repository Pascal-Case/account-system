package com.example.account.service;

import com.example.account.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class RandomAccountNumberGenerator implements AccountNumberGenerator {
    private final AccountRepository accountRepository;

    public RandomAccountNumberGenerator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public String generateUniqueAccountNumber() {
        Random rnd = new Random();
        String accountNumber;
        do {
            accountNumber = Long.toString(Math.abs(1000000000L + (rnd.nextLong() % 9000000000L)));
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());
        return accountNumber;
    }
}
