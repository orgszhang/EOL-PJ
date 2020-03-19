package com.ht.repository;

import com.ht.entity.ProRecords;
import com.ht.entity.TestResults;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestResultsRepo extends JpaRepository<TestResults, Long> {

}
