package com.ht.repository;

import com.ht.entity.ProRecords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;


public interface ProRecordsRepo extends JpaRepository<ProRecords, String> {
    // @Query(nativeQuery = true, value="select * from prorecords where virtual")
    Optional<ProRecords> findByVirtualPartNumber(String virtualPN);
}
