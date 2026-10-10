package com.finsight.wealth.repository;

import com.finsight.wealth.entity.Category;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category,Long> {
    
}
