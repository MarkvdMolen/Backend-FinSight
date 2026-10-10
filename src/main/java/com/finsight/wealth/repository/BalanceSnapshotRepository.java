package com.finsight.wealth.repository;

import com.finsight.wealth.entity.BalanceSnapshot;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceSnapshotRepository extends JpaRepository<BalanceSnapshot, Long> {

    List<BalanceSnapshot> findAllByDateLessThanEqualOrderByDateAsc(LocalDate date);
}
