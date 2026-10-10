package com.finsight.wealth.repository;

import com.finsight.wealth.entity.Account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account,Long> {
    Optional<Account> findByEnableBankingIdentificationHash(String hash);

    List<Account> findAllByBankConnectionId(Long connectionId);

    List<Account> findAllByBankConnectionIsNotNull();
}
