
package com.anurag.aiml.controller;

import com.anurag.aiml.entity.User;
import com.anurag.aiml.entity.ServiceProvider;
import com.anurag.aiml.repository.UserRepository;
import com.anurag.aiml.repository.ServiceProviderRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final ServiceProviderRepository serviceProviderRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public AuthController(
            UserRepository userRepository,
            ServiceProviderRepository serviceProviderRepository) {
        this.userRepository = userRepository;
        this.serviceProviderRepository = serviceProviderRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {

        if (user.getName() == null || user.getName().isBlank()
                || user.getEmail() == null || user.getEmail().isBlank()
                || user.getPassword() == null
                || user.getPassword().length() < 8) {

            return ResponseEntity.badRequest().body(
                    Map.of("message",
                            "Enter your name and email, and a password of at least 8 characters.")
            );
        }

        String email = user.getEmail().trim().toLowerCase();
        String role = user.getRole();

        if (role == null || role.isBlank()) {
            role = "USER";
        }

        role = role.trim().toUpperCase();

        if (!role.equals("USER") && !role.equals("WORKER")) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Role must be USER or WORKER.")
            );
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("message", "This email is already registered.")
            );
        }

        user.setId(null);
        user.setName(user.getName().trim());
        user.setEmail(email);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        // Create a provider profile for newly registered workers.
        if ("WORKER".equals(role)
                && serviceProviderRepository.findByEmailIgnoreCase(email).isEmpty()) {

            ServiceProvider provider = new ServiceProvider();
            provider.setName(savedUser.getName());
            provider.setEmail(email);
            provider.setPhone(savedUser.getPhone());
            provider.setLocation("Uppal");
            provider.setDescription("New service provider");
            provider.setExperience(0.0);

            serviceProviderRepository.save(provider);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "message", "Registration successful!",
                        "id", savedUser.getId(),
                        "name", savedUser.getName(),
                        "email", savedUser.getEmail(),
                        "role", savedUser.getRole()
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> credentials,
            HttpSession session) {

        String email = credentials.get("email");
        String password = credentials.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Email and password are required.")
            );
        }

        Optional<User> foundUser =
                userRepository.findByEmailIgnoreCase(email.trim());

        if (foundUser.isEmpty()
                || !passwordEncoder.matches(
                        password, foundUser.get().getPassword())) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Invalid email or password.")
            );
        }

        User user = foundUser.get();

        session.setAttribute("userId", user.getId());

        return ResponseEntity.ok(
                Map.of(
                        "message", "Login successful!",
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "role", user.getRole()
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> currentUser(HttpSession session) {

        Object userId = session.getAttribute("userId");

        if (!(userId instanceof Long)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Please log in.")
            );
        }

        Optional<User> foundUser =
                userRepository.findById((Long) userId);

        if (foundUser.isEmpty()) {
            session.invalidate();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "User account not found.")
            );
        }

        User user = foundUser.get();

        return ResponseEntity.ok(
                Map.of(
                        "id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "role", user.getRole()
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {

        session.invalidate();

        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully.")
        );
    }
}
