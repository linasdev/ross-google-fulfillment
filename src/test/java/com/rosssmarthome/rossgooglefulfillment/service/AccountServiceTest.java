package com.rosssmarthome.rossgooglefulfillment.service;

import com.rosssmarthome.rossgooglefulfillment.entity.Account;
import com.rosssmarthome.rossgooglefulfillment.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService target;

    @Test
    void shouldCreateNewAccount_whenAccountDoesNotExist() {
        String tokenSubject = UUID.randomUUID().toString();

        when(accountRepository.findByTokenSubject(any())).thenReturn(null);

        Account result = target.findOrCreate(tokenSubject);

        verify(accountRepository).findByTokenSubject(tokenSubject);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue()).isSameAs(result).satisfies(a -> {
            assertThat(a.getId()).isNull();
            assertThat(a.getTokenSubject()).isEqualTo(tokenSubject);
            assertThat(a.getGateways()).isNotNull().isEmpty();
        });

        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void shouldGetAccount_whenAccountExists() {
        Account account = Account.builder()
                .id(UUID.randomUUID())
                .tokenSubject(UUID.randomUUID().toString())
                .build();

        when(accountRepository.findByTokenSubject(any())).thenReturn(account);

        Account result = target.findOrCreate(account.getTokenSubject());

        assertThat(result).isSameAs(account);

        verify(accountRepository).findByTokenSubject(account.getTokenSubject());
        verifyNoMoreInteractions(accountRepository);
    }
}
