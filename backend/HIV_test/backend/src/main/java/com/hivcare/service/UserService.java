package com.hivcare.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hivcare.entity.Doctor;
import com.hivcare.entity.Patient;
import com.hivcare.entity.User;
import com.hivcare.entity.User.Role;
import com.hivcare.repository.DoctorRepository;
import com.hivcare.repository.PatientRepository;
import com.hivcare.repository.UserRepository;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAllDistinct(pageable);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    public Page<User> searchUsers(String searchTerm, Pageable pageable) {
        return userRepository.findBySearchTerm(searchTerm, pageable);
    }

    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        // Create associated profile based on role
        if (user.getRole() == User.Role.PATIENT) {
            Patient patient = new Patient(savedUser);
            patientRepository.save(patient);
        } else if (user.getRole() == User.Role.DOCTOR) {
            Doctor doctor = new Doctor(savedUser);
            doctorRepository.save(doctor);
        }

        return savedUser;
    }

    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setFullName(userDetails.getFullName());
        user.setEmail(userDetails.getEmail());
        user.setPhoneNumber(userDetails.getPhoneNumber());
        user.setEnabled(userDetails.isEnabled());
        user.setLastLogin(userDetails.getLastLogin());

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        userRepository.delete(user);
    }

    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    public long getTotalUsers() {
        return userRepository.count();
    }

    public long countUsersByRole(User.Role role) {
        return userRepository.countByRole(role);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public Page<User> getUsersByRole(Role role, Pageable pageable) {
        return userRepository.findByRole(role, pageable);
    }

    public List<User> getUsersByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return userRepository.findByCreatedAtBetween(startDate, endDate);
    }

    public List<User> getActiveUsersByRole(Role role) {
        return userRepository.findActiveUsersByRole(role);
    }

    public long countActiveUsers() {
        return userRepository.countActiveUsers();
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    public User save(User user) {
        // Log user details before saving
        System.out.println("Saving user: " + user.getUsername() + " with role: " + user.getRole());
        
        // Ensure password is encoded
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            System.out.println("Encoding password for user: " + user.getUsername());
            user.setPassword(encodedPassword);
        }
        
        User savedUser = userRepository.save(user);
        System.out.println("User saved successfully with ID: " + savedUser.getId());
        return savedUser;
    }

    public Optional<Long> getUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::getId);
    }
}
