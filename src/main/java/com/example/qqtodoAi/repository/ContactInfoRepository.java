package com.example.qqtodoAi.repository;

import com.example.qqtodoAi.model.ContactInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContactInfoRepository extends JpaRepository<ContactInfo, Long> {

    @Query("SELECT COUNT(c) > 0 FROM ContactInfo c " +
            "WHERE c.qq = :qq AND c.isMarked = true " +
            "AND (c.groupId IS NULL OR c.groupId = :groupId)")
    boolean existsMarked(@Param("qq") Long qq, @Param("groupId") Long groupId);
    List<ContactInfo> findAllByOrderByIdDesc();

}
