package com.ht.repository;

import com.ht.entity.ProRecords;
import com.ht.entity.ShuntResistors;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface ProRecordsRepo extends JpaRepository<ProRecords, String> {

}
