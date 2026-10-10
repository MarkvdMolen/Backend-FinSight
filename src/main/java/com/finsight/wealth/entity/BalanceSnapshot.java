package com.finsight.wealth.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "wealth_balance_snapshots",
        uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "date"})
)
@Getter
@Setter
@NoArgsConstructor
public class BalanceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;
}
