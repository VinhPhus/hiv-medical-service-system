package com.hivcare.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;
import java.io.IOException;
import com.hivcare.entity.User;

import com.hivcare.security.jwt.JwtAuthenticationEntryPoint;
import com.hivcare.security.jwt.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true,
    prePostEnabled = true
)
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public SecurityFilterChain formLoginFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/**")
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
                .ignoringRequestMatchers("/auth/**")
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/auth/**",
                    "/css/**", "/js/**", "/img/**",
                    "/", "/about", "/contact", "/services",
                    "/anonymous-help",
                    "/error",
                    "/testResult/**" // <-- Thêm dòng này vào permitAll
                ).permitAll()
                .requestMatchers("/dashboard/admin", "/dashboard/admin/**", "/admin/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/dashboard/manager", "/dashboard/manager/**", "/manager/**").hasAuthority("ROLE_MANAGER")
                .requestMatchers("/dashboard/doctor", "/dashboard/doctor/**").hasAuthority("ROLE_DOCTOR")
                .requestMatchers("/dashboard/staff", "/dashboard/staff/**").hasAuthority("ROLE_STAFF")
                .requestMatchers("/dashboard/patient", "/dashboard/patient/**").hasAuthority("ROLE_PATIENT")
                .requestMatchers("/patient/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_DOCTOR", "ROLE_STAFF", "ROLE_MANAGER")
                .requestMatchers("/appointment/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_DOCTOR", "ROLE_STAFF", "ROLE_MANAGER", "ROLE_PATIENT")
                .requestMatchers("/medicalRecord/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_DOCTOR", "ROLE_STAFF", "ROLE_MANAGER", "ROLE_PATIENT")
                // .requestMatchers("/testResult/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_DOCTOR", "ROLE_STAFF", "ROLE_MANAGER", "ROLE_PATIENT") // <-- Comment hoặc xóa dòng này
                .requestMatchers("/arvProtocol/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_DOCTOR", "ROLE_STAFF", "ROLE_MANAGER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .successHandler(authenticationSuccessHandler())
                .failureUrl("/auth/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .invalidSessionUrl("/auth/login")
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/auth/login?expired=true")
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error/403")
            );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                    Authentication authentication) throws IOException, ServletException {
                logger.info("Authentication success for user: {}", authentication.getName());
                logger.info("Authorities from Authentication: {}", authentication.getAuthorities());
                
                User user = (User) authentication.getPrincipal();
                logger.info("User details - ID: {}, Username: {}, Role: {}, Authorities: {}", 
                    user.getId(), user.getUsername(), user.getRole(), user.getAuthorities());
                
                request.getSession(true).setAttribute("user", user);
                request.getSession().setAttribute("role", user.getRole().name());
                logger.info("Session attributes set - User: {}, Role: {}", user.getUsername(), user.getRole().name());
                
                String redirectUrl = determineTargetUrl(user.getRole());
                logger.info("Redirecting to: {}", redirectUrl);
                
                response.sendRedirect(redirectUrl);
            }

            private String determineTargetUrl(User.Role role) {
                switch (role) {
                    case ADMIN:
                        return "/dashboard/admin";
                    case MANAGER:
                        return "/dashboard/manager";
                    case DOCTOR:
                        return "/dashboard/doctor";
                    case STAFF:
                        return "/dashboard/staff";
                    case PATIENT:
                        return "/dashboard/patient";
                    default:
                        return "/";
                }
            }
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}