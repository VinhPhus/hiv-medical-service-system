package com.hivcare.repository;

import com.hivcare.entity.ArvProtocol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArvProtocolRepository extends JpaRepository<ArvProtocol, Long> {
    
    Optional<ArvProtocol> findByCode(String code);
    
    List<ArvProtocol> findByPatientCategory(ArvProtocol.PatientCategory category);
    
    List<ArvProtocol> findByActive(boolean active);
    
    @Query("SELECT a FROM ArvProtocol a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(a.code) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<ArvProtocol> findBySearchTerm(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT a FROM ArvProtocol a WHERE a.active = true ORDER BY a.createdAt DESC")
    List<ArvProtocol> findAllActiveOrderByCreatedAtDesc();
    
    @Query("SELECT COUNT(a) FROM ArvProtocol a WHERE a.active = true")
    long countActiveProtocols();

    @Query("SELECT a FROM ArvProtocol a WHERE (:search IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ArvProtocol> findAllWithSearch(@Param("search") String search, Pageable pageable);
}
