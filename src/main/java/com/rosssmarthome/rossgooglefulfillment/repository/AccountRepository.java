package com.rosssmarthome.rossgooglefulfillment.repository;

import com.rosssmarthome.rossgooglefulfillment.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Account findByTokenSubject(String tokenSubject);
}
