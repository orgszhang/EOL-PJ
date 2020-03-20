package com.ht.repository;

import com.ht.entity.Devices;
import com.ht.entity.LatestQRCodes;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DevicesRepo extends JpaRepository<Devices, String> {

}
