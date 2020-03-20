package com.ht.repository;

import com.ht.entity.ShuntResistors;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShuntResistorsRepo extends JpaRepository<ShuntResistors, String>{
    Optional<ShuntResistors> findById(String resistorID);
}
