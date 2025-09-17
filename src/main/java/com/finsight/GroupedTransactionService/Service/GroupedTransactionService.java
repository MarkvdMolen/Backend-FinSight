package com.finsight.GroupedTransactionService.Service;

import com.finsight.GroupedTransactionService.Repository.GroupedTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GroupedTransactionService {

    @Autowired
    private GroupedTransactionRepository groupedTransactionRepository;


}
