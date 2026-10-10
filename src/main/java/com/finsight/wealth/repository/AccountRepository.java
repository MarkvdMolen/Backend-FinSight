package com.finsight.wealth.repository;

import com.finsight.wealth.entity.Account;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account,Long> {
    
}
