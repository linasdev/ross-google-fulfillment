package com.rosssmarthome.rossgooglefulfillment.service;

import com.rosssmarthome.rossgooglefulfillment.entity.Account;
import com.rosssmarthome.rossgooglefulfillment.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public Account findOrCreate(String tokenSubject) {
        Account account = accountRepository.findByTokenSubject(tokenSubject);

        if (account != null) {
            return account;
        }

        account = Account.builder()
                .tokenSubject(tokenSubject)
                .build();

        accountRepository.save(account);

        return account;
    }
}
