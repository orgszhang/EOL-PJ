package com.ht.repository;

import com.ht.entity.LatestQRCodes;
import org.springframework.data.jpa.repository.JpaRepository;


public interface LatestQRCodeRepo extends JpaRepository<LatestQRCodes, String> {

}
