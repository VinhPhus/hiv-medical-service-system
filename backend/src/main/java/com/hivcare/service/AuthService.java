package main.java.com.hivcare.service;

import com.hivcare.dto.request.LoginRequest;
import com.hivcare.dto.request.UserRegistrationRequest;
import com.hivcare.dto.response.JwtResponse;
import com.hivcare.dto.response.UserResponse;
import com.hivcare.entity.User;
import com.hivcare.exception.BadRequestException;
import com.hivcare.repository.UserRepository;
import com.hivcare.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        claims.put("userId", user.getId());

        String jwt = jwtUtil.generateToken(claims, user);
        String refreshToken = jwtUtil.generateToken(user);

        // Update last login
        userService.updateLastLogin(user.getEmail());

        log.info("User {} logged in successfully", user.getEmail());

        return JwtResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken)
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .avatar(user.getAvatar())
                .build();
    }

    public UserResponse register(UserRegistrationRequest request) {
        return userService.createUser(request);
    }

    public JwtResponse refreshToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            if (jwtUtil.validateToken(jwt)) {
                String email = jwtUtil.extractUsername(jwt);
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new BadRequestException("User not found"));

                Map<String, Object> claims = new HashMap<>();
                claims.put("role", user.getRole().name());
                claims.put("userId", user.getId());

                String newToken = jwtUtil.generateToken(claims, user);
                String newRefreshToken = jwtUtil.generateToken(user);

                return JwtResponse.builder()
                        .token(newToken)
                        .refreshToken(newRefreshToken)
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(user.getRole())
                        .avatar(user.getAvatar())
                        .build();
            }
        }
        throw new BadRequestException("Invalid token");
    }

    public void logout(String token) {
        // In a production environment, you would typically blacklist the token
        // For now, we'll just log the logout event
        log.info("User logged out with token: {}", token.substring(0, 20) + "...");
    }
}
