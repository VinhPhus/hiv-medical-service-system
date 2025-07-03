package com.hivcare.controller;

import com.hivcare.dto.request.SignupRequest;
import com.hivcare.entity.Doctor;
import com.hivcare.entity.User;
import com.hivcare.entity.Patient;
import com.hivcare.entity.User.Role;
import com.hivcare.service.UserService;
import com.hivcare.service.DoctorService;
import com.hivcare.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@Controller
public class HomeWebController {

    private static final Logger logger = LoggerFactory.getLogger(HomeWebController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {
        model.addAttribute("requestURI", request.getRequestURI());
        // Get public doctors list
        try {
            List<Doctor> doctors = doctorService.findAvailableDoctors();
            model.addAttribute("doctors", doctors.size() > 6 ? doctors.subList(0, 6) : doctors);
        } catch (Exception e) {
            model.addAttribute("doctors", List.of());
        }

        return "index";
    }

    @GetMapping("/auth/login")
    public String showLoginForm(@RequestParam(required = false) String error,
                              @RequestParam(required = false) String logout,
                              @RequestParam(required = false) String registered,
                              Model model) {
        if (error != null) {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng!");
        }
        if (logout != null) {
            model.addAttribute("message", "Bạn đã đăng xuất thành công!");
        }
        if (registered != null) {
            model.addAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
        }
        return "auth/login";
    }

    @GetMapping("/auth/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "auth/register";
    }

    @PostMapping("/auth/register")
    public String register(@ModelAttribute @Valid SignupRequest signupRequest, 
                         BindingResult result, 
                         Model model,
                         HttpServletRequest request) {
        if (result.hasErrors()) {
            String errorMsg = result.getAllErrors().get(0).getDefaultMessage();
            model.addAttribute("error", errorMsg);
            return getReturnView(request);
        }

        // Check if username exists
        if (userService.existsByUsername(signupRequest.getUsername())) {
            model.addAttribute("error", "Tên đăng nhập đã được sử dụng!");
            return getReturnView(request);
        }

        // Check if email exists
        if (userService.existsByEmail(signupRequest.getEmail())) {
            model.addAttribute("error", "Email đã được sử dụng!");
            return getReturnView(request);
        }

        // Xác định role hợp lệ
        Role role;
        String requestedRole = signupRequest.getRole();
        if (requestedRole == null || requestedRole.isEmpty()) {
            role = Role.PATIENT; // Default role
        } else {
            try {
                role = Role.valueOf(requestedRole.toUpperCase());
                // Validate role based on request source
                String referer = request.getHeader("Referer");
                if (referer != null) {
                    if (referer.contains("/admin/add-staff") && role != Role.STAFF) {
                        model.addAttribute("error", "Role không hợp lệ cho nhân viên!");
                        return getReturnView(request);
                    } else if (referer.contains("/admin/add-doctor") && role != Role.DOCTOR) {
                        model.addAttribute("error", "Role không hợp lệ cho bác sĩ!");
                        return getReturnView(request);
                    } else if (referer.contains("/admin/add-manager") && role != Role.MANAGER) {
                        model.addAttribute("error", "Role không hợp lệ cho quản lý!");
                        return getReturnView(request);
                    }
                }
            } catch (IllegalArgumentException e) {
                model.addAttribute("error", "Role không hợp lệ!");
                return getReturnView(request);
            }
        }

        // Create new user account
        User user = new User(signupRequest.getUsername(), 
                           signupRequest.getEmail(),
                           passwordEncoder.encode(signupRequest.getPassword()),
                           signupRequest.getFullName(),
                           role);

        user.setPhoneNumber(signupRequest.getPhoneNumber());
        user.setDateOfBirth(signupRequest.getDateOfBirth());
        user.setGender(signupRequest.getGender());
        user.setAddress(signupRequest.getAddress());
        
        User savedUser = userService.save(user);

        // Create patient profile for customer
        if (user.getRole() == Role.PATIENT) {
            Patient patient = new Patient(savedUser);
            // Map thêm các trường từ user sang patient
            patient.setDateOfBirth(user.getDateOfBirth());
            patient.setGender(user.getGender());
            patient.setAddress(user.getAddress());
            patientService.save(patient);
        } else if (user.getRole() == Role.DOCTOR) {
            // Create doctor profile
            Doctor doctor = new Doctor(savedUser);
            doctorService.createDoctor(doctor);
        }

        // Xác định trang trả về và thông báo thành công
        String referer = request.getHeader("Referer");
        if (referer != null) {
            String successMessage = "";
            if (referer.contains("/admin/add-staff")) {
                successMessage = "Thêm nhân viên thành công!";
            } else if (referer.contains("/admin/add-doctor")) {
                successMessage = "Thêm bác sĩ thành công!";
            } else if (referer.contains("/admin/add-manager")) {
                successMessage = "Thêm quản lý thành công!";
            }
            
            if (!successMessage.isEmpty()) {
                model.addAttribute("success", successMessage);
                model.addAttribute("signupRequest", new SignupRequest()); // Reset form
                return getReturnView(request);
            }
        }

        // Nếu là đăng ký thông thường, chuyển về trang đăng nhập
        return "redirect:/auth/login?registered=true";
    }

    // Helper method to determine the return view
    private String getReturnView(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null) {
            if (referer.contains("/admin/add-staff")) {
                return "admin/add-staff";
            } else if (referer.contains("/admin/add-doctor")) {
                return "admin/add-doctor";
            } else if (referer.contains("/admin/add-manager")) {
                return "admin/add-manager";
            }
        }
        return "auth/register";
    }

    @GetMapping("/auth/register-anonymous")
    public String showAnonymousRegisterForm(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "auth/register-anonymous";
    }

    @PostMapping("/auth/register-anonymous")
    public String registerAnonymous(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        String anonymousCode = request.getParameter("anonymousCode");
        String password = request.getParameter("password");
        String gender = request.getParameter("gender");
        String ageStr = request.getParameter("age");
        Integer age = null;
        try {
            if (ageStr != null && !ageStr.isBlank()) {
                age = Integer.parseInt(ageStr);
            }
        } catch (NumberFormatException e) {
            age = null;
        }
        // Validate tối thiểu: mật khẩu không rỗng
        if (password == null || password.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu không được để trống!");
            return "redirect:/auth/register-anonymous";
        }
        // Tạo user ẩn danh
        User user = new User();
        // Sinh suffix ngẫu nhiên để đảm bảo duy nhất
        String uniqueSuffix = System.currentTimeMillis() + "-" + java.util.UUID.randomUUID().toString().substring(0, 6);
        String username = (anonymousCode != null && !anonymousCode.isBlank()) ? anonymousCode : ("ANON-" + uniqueSuffix);
        user.setUsername(username);
        user.setEmail("anonymous-" + uniqueSuffix + "@noemail.com");
        user.setFullName("Ẩn danh " + uniqueSuffix);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(User.Role.PATIENT);
        user.setEnabled(true);
        user.setAnonymous(true);
        // Nếu có giới tính và tuổi thì lưu
        if (gender != null && !gender.isBlank()) {
            try {
                user.setGender(User.Gender.valueOf(gender));
            } catch (Exception ignored) {}
        }
        if (age != null) {
            // Không có trường age trong User, nếu cần lưu thì mở rộng entity
        }
        User savedUser = userService.save(user);

        // Tạo patient profile
        Patient patient = new Patient(savedUser);
        patientService.save(patient);

        // Tự động đăng nhập
        try {
            logger.info("Bắt đầu quá trình tự động đăng nhập cho user ẩn danh: {}", username);
            
            // Load user details từ UserDetailsService
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            logger.info("Đã load UserDetails: authorities={}", userDetails.getAuthorities());
            
            // Tạo authentication token với userDetails
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(userDetails, password);
            authToken.setDetails(new WebAuthenticationDetails(request));
            logger.info("Đã tạo authentication token");
            
            // Xác thực qua AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(authToken);
            logger.info("Xác thực thành công qua AuthenticationManager: authorities={}", authentication.getAuthorities());
            
            // Set authentication vào context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("Đã set authentication vào SecurityContext");

            // Đảm bảo session lưu SPRING_SECURITY_CONTEXT
            request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            // Tạo session mới và lưu thông tin
            request.getSession().invalidate();
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("user", savedUser);
            newSession.setAttribute("role", savedUser.getRole());
            logger.info("Đã tạo session mới và lưu thông tin user: role={}", savedUser.getRole());

            redirectAttributes.addFlashAttribute("message", "Đăng ký ẩn danh thành công!");
            return "redirect:/dashboard/patient";
        } catch (Exception e) {
            logger.error("Lỗi khi tự động đăng nhập sau đăng ký ẩn danh", e);
            logger.error("Chi tiết lỗi:", e);
            redirectAttributes.addFlashAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "redirect:/auth/login";
        }
    }

    @GetMapping("/auth/forgot-password")
    public String showForgotPasswordForm() {
        return "auth/forgot-password";
    }

    @GetMapping("/auth/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/anonymous-help")
    public String showAnonymousHelpDirect() {
        return "anonymous-help";
    }

    @GetMapping("/privacy-policy")
    public String privacyPolicy(Model model) {
        return "privacy-policy";
    }

    private String generatePatientCode() {
        int year = LocalDateTime.now().getYear();
        int randomId = (int) (Math.random() * 9000) + 1000;
        return "PT-" + year + "-" + randomId;
    }
}
