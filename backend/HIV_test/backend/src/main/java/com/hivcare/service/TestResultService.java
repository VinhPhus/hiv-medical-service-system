
package com.hivcare.service;

import com.hivcare.entity.TestResult;
import com.hivcare.entity.Patient;
import com.hivcare.repository.TestResultRepository;
import com.hivcare.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TestResultService {

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private PatientRepository patientRepository;

    @SuppressWarnings("unused")
    @Autowired
    private NotificationService notificationService;

    private final Path fileStorageLocation;

    public TestResultService() {
        this.fileStorageLocation = Paths.get("uploads/test-results")
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public List<TestResult> getAllTestResults() {
        return testResultRepository.findAll();
    }

    public Page<TestResult> getAllTestResults(String search, Pageable pageable) {
        return testResultRepository.findAllWithSearch(search, pageable);
    }

    public Page<TestResult> getTestResultsByPatientId(Long patientId, String search, Pageable pageable) {
        return testResultRepository.findByPatientIdWithSearch(patientId, search, pageable);
    }

    public Optional<TestResult> getTestResultById(Long id) {
        return testResultRepository.findById(id);
    }

    public Optional<TestResult> getTestResultByCode(String code) {
        return testResultRepository.findByTestCode(code);
    }

    public List<TestResult> getTestResultsByPatient(Long patientId) {
        return testResultRepository.findByPatientId(patientId);
    }

    public Page<TestResult> getTestResultsByPatient(Long patientId, Pageable pageable) {
        return testResultRepository.findByPatientId(patientId, pageable);
    }

    public List<TestResult> getTestResultsByType(TestResult.TestType testType) {
        return testResultRepository.findByTestType(testType);
    }

    public List<TestResult> getTestResultsByPatientAndType(Long patientId, TestResult.TestType testType) {
        return testResultRepository.findByPatientIdAndTestTypeOrderByTestDateDesc(patientId, testType);
    }

    public List<TestResult> getTestResultsListByStatus(TestResult.TestStatus status) {
        return testResultRepository.findByStatus(status);
    }

    public List<TestResult> getTestResultsByDateRange(LocalDate startDate, LocalDate endDate) {
        return testResultRepository.findByTestDateBetween(startDate, endDate);
    }

    public List<TestResult> getPatientTestResultsByDateRange(Long patientId, LocalDate startDate, LocalDate endDate) {
        return testResultRepository.findByPatientIdAndTestDateBetween(patientId, startDate, endDate);
    }

    public TestResult createTestResult(TestResult testResult) {
        validateTestResult(testResult);
        return testResultRepository.save(testResult);
    }

    public TestResult updateTestResult(Long id, TestResult testResultDetails) {
        TestResult testResult = testResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test Result not found with id: " + id));

        validateTestResult(testResultDetails);

        // Update fields
        testResult.setPatient(testResultDetails.getPatient());
        testResult.setTestType(testResultDetails.getTestType());
        testResult.setTestDate(testResultDetails.getTestDate());
        testResult.setResultValue(testResultDetails.getResultValue());
        testResult.setUnit(testResultDetails.getUnit());
        testResult.setReferenceRange(testResultDetails.getReferenceRange());
        testResult.setStatus(testResultDetails.getStatus());
        testResult.setLabTechnician(testResultDetails.getLabTechnician());
        testResult.setNotes(testResultDetails.getNotes());
        if (testResultDetails.getFilePath() != null) {
            testResult.setFilePath(testResultDetails.getFilePath());
        }

        return testResultRepository.save(testResult);
    }

    public TestResult completeTestResult(Long id, String resultValue, String notes) {
        TestResult testResult = testResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test result not found"));

        testResult.setResultValue(resultValue);
        testResult.setStatus(TestResult.TestStatus.COMPLETED);
        testResult.setNotes(notes);

        TestResult savedResult = testResultRepository.save(testResult);

        // Check if result is abnormal and needs attention
        if (isResultAbnormal(testResult)) {
            testResult.setStatus(TestResult.TestStatus.ABNORMAL);
            savedResult = testResultRepository.save(testResult);
            // notificationService.sendAbnormalTestResultAlert(savedResult);
        }

        return savedResult;
    }

    public TestResult reviewTestResult(Long id, String reviewNotes) {
        TestResult testResult = testResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test result not found"));

        testResult.setStatus(TestResult.TestStatus.REVIEWED);
        testResult.setNotes(testResult.getNotes() + "\nReview: " + reviewNotes);

        return testResultRepository.save(testResult);
    }

    public void deleteTestResult(Long id) {
        TestResult testResult = testResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test Result not found with id: " + id));
        
        // Delete associated file if exists
        if (testResult.getFilePath() != null) {
            try {
                Path filePath = this.fileStorageLocation.resolve(testResult.getFilePath());
                Files.deleteIfExists(filePath);
            } catch (IOException ex) {
                // Log error but continue with deletion
                System.err.println("Error deleting file: " + ex.getMessage());
            }
        }
        
        testResultRepository.delete(testResult);
    }

    public TestResult getLatestTestResult(Long patientId, TestResult.TestType testType) {
        List<TestResult> results = testResultRepository
                .findByPatientIdAndTestTypeOrderByTestDateDesc(patientId, testType);
        
        return results.isEmpty() ? null : results.get(0);
    }

    public List<TestResult> getCd4History(Long patientId) {
        return testResultRepository.findByPatientIdAndTestTypeOrderByTestDateDesc(
                patientId, TestResult.TestType.CD4_COUNT);
    }

    public List<TestResult> getViralLoadHistory(Long patientId) {
        return testResultRepository.findByPatientIdAndTestTypeOrderByTestDateDesc(
                patientId, TestResult.TestType.VIRAL_LOAD);
    }

    public boolean isResultAbnormal(TestResult testResult) {
        // Basic logic to determine if result is abnormal
        // This should be enhanced based on medical standards
        
        if (testResult.getResultValue() == null || testResult.getReferenceRange() == null) {
            return false;
        }

        switch (testResult.getTestType()) {
            case CD4_COUNT:
                try {
                    double cd4Value = Double.parseDouble(testResult.getResultValue());
                    return cd4Value < 200; // Below 200 is critically low
                } catch (NumberFormatException e) {
                    return false;
                }
            
            case VIRAL_LOAD:
                try {
                    double viralLoad = Double.parseDouble(testResult.getResultValue());
                    return viralLoad > 1000; // Above 1000 copies/ml is concerning
                } catch (NumberFormatException e) {
                    return false;
                }
            
            default:
                return false;
        }
    }

    public long getTotalTestResults() {
        return testResultRepository.count();
    }

    public int getTestResultsByStatus(TestResult.TestStatus status) {
        return testResultRepository.findByStatus(status).size();
    }

    public List<TestResult> getPendingTestResults() {
        return testResultRepository.findByStatus(TestResult.TestStatus.PENDING);
    }

    public List<TestResult> getAbnormalTestResults() {
        return testResultRepository.findByStatus(TestResult.TestStatus.ABNORMAL);
    }

    public List<TestResult> getPendingTests() {
        return testResultRepository.findByStatus(TestResult.TestStatus.PENDING);
    }

    public List<TestResult> getMonthlyTests() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        return testResultRepository.findByCreatedAtBetween(startOfMonth, endOfMonth);
    }

    private void validateTestResult(TestResult testResult) {
        // Validate patient
        Patient patient = patientRepository.findById(testResult.getPatient().getId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        testResult.setPatient(patient);

        // Validate test date
        if (testResult.getTestDate() == null) {
            testResult.setTestDate(LocalDate.now());
        }

        // Validate test code
        if (testResult.getTestCode() == null || testResult.getTestCode().trim().isEmpty()) {
            testResult.setTestCode(generateTestCode());
        }

        // Validate status
        if (testResult.getStatus() == null) {
            testResult.setStatus(TestResult.TestStatus.PENDING);
        }
    }

    private String generateTestCode() {
        return "TST" + System.currentTimeMillis();
    }

    public String uploadFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        // Generate unique filename
        String fileExtension = "";
        if (fileName.contains(".")) {
            fileExtension = fileName.substring(fileName.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return uniqueFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public byte[] getFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            return Files.readAllBytes(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("File not found " + fileName, ex);
        }
    }

    public long countTotalTests() {
        return testResultRepository.count();
    }

    public long countTestsByStatus(TestResult.TestStatus status) {
        return testResultRepository.countByStatus(status);
    }

    public List<TestResult> getRecentTestResultsByPatient(Long patientId, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "testDate"));
        Page<TestResult> page = testResultRepository.findByPatientId(patientId, pageRequest);
        return page.getContent();
    }

    public long countTotalTestResults() {
        return testResultRepository.count();
    }

    public long countPendingTestResults() {
        return testResultRepository.countByStatus(TestResult.TestStatus.PENDING);
    }

    public List<TestResult> getPendingTestsByDoctor(Long doctorId) {
        return testResultRepository.findPendingTestsByDoctor(doctorId);
    }
}
