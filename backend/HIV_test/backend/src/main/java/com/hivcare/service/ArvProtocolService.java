package com.hivcare.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hivcare.entity.ArvProtocol;
import com.hivcare.repository.ArvProtocolRepository;

@Service
@Transactional
public class ArvProtocolService {

    @Autowired
    private ArvProtocolRepository arvProtocolRepository;

    public List<ArvProtocol> getAllProtocols() {
        return arvProtocolRepository.findAll();
    }

    public Page<ArvProtocol> getAllProtocols(Pageable pageable) {
        return arvProtocolRepository.findAll(pageable);
    }

    public Page<ArvProtocol> getAllProtocols(String search, Pageable pageable) {
        return arvProtocolRepository.findAllWithSearch(search, pageable);
    }

    public List<ArvProtocol> getActiveProtocols() {
        return arvProtocolRepository.findByActive(true);
    }

    public Optional<ArvProtocol> getProtocolById(Long id) {
        return arvProtocolRepository.findById(id);
    }

    public Optional<ArvProtocol> getProtocolByCode(String code) {
        return arvProtocolRepository.findByCode(code);
    }

    public List<ArvProtocol> getProtocolsByCategory(ArvProtocol.PatientCategory category) {
        return arvProtocolRepository.findByPatientCategory(category);
    }

    public List<ArvProtocol> searchProtocols(String searchTerm) {
        return arvProtocolRepository.findBySearchTerm(searchTerm);
    }

    public ArvProtocol createProtocol(ArvProtocol protocol) {
        // Ensure code is unique
        if (arvProtocolRepository.findByCode(protocol.getCode()).isPresent()) {
            throw new RuntimeException("Protocol code already exists: " + protocol.getCode());
        }
        
        protocol.setActive(true);
        return arvProtocolRepository.save(protocol);
    }

    public ArvProtocol updateProtocol(Long id, ArvProtocol protocolDetails) {
        ArvProtocol protocol = arvProtocolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ARV Protocol not found with id: " + id));

        // Check if code is being changed and ensure it's unique
        if (!protocol.getCode().equals(protocolDetails.getCode())) {
            if (arvProtocolRepository.findByCode(protocolDetails.getCode()).isPresent()) {
                throw new RuntimeException("Protocol code already exists: " + protocolDetails.getCode());
            }
        }

        protocol.setName(protocolDetails.getName());
        protocol.setCode(protocolDetails.getCode());
        protocol.setDescription(protocolDetails.getDescription());
        protocol.setMedications(protocolDetails.getMedications());
        protocol.setDosagePerDay(protocolDetails.getDosagePerDay());
        protocol.setRecommendedDurationWeeks(protocolDetails.getRecommendedDurationWeeks());
        protocol.setPatientCategory(protocolDetails.getPatientCategory());
        protocol.setContraindications(protocolDetails.getContraindications());
        protocol.setSideEffects(protocolDetails.getSideEffects());
        protocol.setMonitoringRequirements(protocolDetails.getMonitoringRequirements());
        protocol.setActive(protocolDetails.isActive());

        return arvProtocolRepository.save(protocol);
    }

    public ArvProtocol activateProtocol(Long id) {
        ArvProtocol protocol = arvProtocolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ARV Protocol not found"));

        protocol.setActive(true);
        return arvProtocolRepository.save(protocol);
    }

    public ArvProtocol deactivateProtocol(Long id) {
        ArvProtocol protocol = arvProtocolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ARV Protocol not found"));

        protocol.setActive(false);
        return arvProtocolRepository.save(protocol);
    }

    public void deleteProtocol(Long id) {
        ArvProtocol protocol = arvProtocolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ARV Protocol not found with id: " + id));
        
        arvProtocolRepository.delete(protocol);
    }

    public List<ArvProtocol> getRecommendedProtocols(ArvProtocol.PatientCategory category) {
        return arvProtocolRepository.findByPatientCategory(category)
                .stream()
                .filter(ArvProtocol::isActive)
                .toList();
    }

    public ArvProtocol cloneProtocol(Long id, String newCode, String newName) {
        ArvProtocol originalProtocol = arvProtocolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ARV Protocol not found"));

        // Check if new code is unique
        if (arvProtocolRepository.findByCode(newCode).isPresent()) {
            throw new RuntimeException("Protocol code already exists: " + newCode);
        }

        ArvProtocol clonedProtocol = new ArvProtocol();
        clonedProtocol.setName(newName);
        clonedProtocol.setCode(newCode);
        clonedProtocol.setDescription(originalProtocol.getDescription() + " (Cloned)");
        clonedProtocol.setMedications(originalProtocol.getMedications());
        clonedProtocol.setDosagePerDay(originalProtocol.getDosagePerDay());
        clonedProtocol.setRecommendedDurationWeeks(originalProtocol.getRecommendedDurationWeeks());
        clonedProtocol.setPatientCategory(originalProtocol.getPatientCategory());
        clonedProtocol.setContraindications(originalProtocol.getContraindications());
        clonedProtocol.setSideEffects(originalProtocol.getSideEffects());
        clonedProtocol.setMonitoringRequirements(originalProtocol.getMonitoringRequirements());
        clonedProtocol.setActive(true);

        return arvProtocolRepository.save(clonedProtocol);
    }

   
}
