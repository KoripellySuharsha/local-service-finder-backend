
package com.anurag.aiml.controller;

import com.anurag.aiml.entity.Booking;
import com.anurag.aiml.entity.Service;
import com.anurag.aiml.entity.ServiceProvider;
import com.anurag.aiml.entity.User;

import com.anurag.aiml.repository.BookingRepository;
import com.anurag.aiml.repository.ServiceProviderRepository;
import com.anurag.aiml.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/provider")
public class ProviderDashboardController {

    private final BookingRepository bookingRepository;
    private final ServiceProviderRepository providerRepository;
    private final UserRepository userRepository;

    public ProviderDashboardController(
            BookingRepository bookingRepository,
            ServiceProviderRepository providerRepository,
            UserRepository userRepository) {

        this.bookingRepository = bookingRepository;
        this.providerRepository = providerRepository;
        this.userRepository = userRepository;
    }

    // Find the provider linked to the logged-in user's email.
    private Optional<ServiceProvider> getLoggedInProvider(
            HttpSession session) {

        Object sessionUserId = session.getAttribute("userId");

        if (!(sessionUserId instanceof Long)) {
            return Optional.empty();
        }

        Long userId = (Long) sessionUserId;
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty() || user.get().getEmail() == null) {
            return Optional.empty();
        }

        return providerRepository.findByEmailIgnoreCase(
                user.get().getEmail().trim()
        );
    }

    private Map<String, Object> bookingDetails(Booking booking) {
        Service service = booking.getService();
        User customer = booking.getUser();

        Map<String, Object> details = new HashMap<>();

        details.put("id", booking.getId());
        details.put("serviceName", service.getServiceName());
        details.put(
                "category",
                service.getCategory() == null
                        ? "" : service.getCategory()
        );
        details.put("price", service.getPrice());
        details.put("bookingDate", booking.getBookingDate().toString());
        details.put("bookingTime", booking.getBookingTime().toString());
        details.put("address", booking.getAddress());
        details.put(
                "notes",
                booking.getNotes() == null ? "" : booking.getNotes()
        );
        details.put("status", booking.getStatus());
        details.put("customerName", customer.getName());
        details.put("customerEmail", customer.getEmail());
        details.put(
                "customerPhone",
                customer.getPhone() == null ? "" : customer.getPhone()
        );

        return details;
    }

    // Get bookings for the logged-in provider's services.
    @GetMapping("/bookings")
    public ResponseEntity<?> getProviderBookings(HttpSession session) {

        Object sessionUserId = session.getAttribute("userId");

        if (!(sessionUserId instanceof Long)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in first."));
        }

        Optional<ServiceProvider> provider =
                getLoggedInProvider(session);

        if (provider.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "No provider account is linked to your login email."
                    ));
        }

        Long providerId = provider.get().getId();
        List<Map<String, Object>> results = new ArrayList<>();

        List<Booking> allBookings = bookingRepository.findAll();

        for (Booking booking : allBookings) {
            Service service = booking.getService();

            if (service != null && service.getProvider() != null
                    && service.getProvider().getId().equals(providerId)) {
                results.add(bookingDetails(booking));
            }
        }

        return ResponseEntity.ok(results);
    }

    // Accept or reject a pending booking.
    @PutMapping("/bookings/{id}/status")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            HttpSession session) {

        Object sessionUserId = session.getAttribute("userId");

        if (!(sessionUserId instanceof Long)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please log in first."));
        }

        Optional<ServiceProvider> provider =
                getLoggedInProvider(session);

        if (provider.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "No provider account is linked to your login email."
                    ));
        }

        String newStatus = request.get("status");

        if (!"ACCEPTED".equals(newStatus)
                && !"REJECTED".equals(newStatus)) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Status must be ACCEPTED or REJECTED."
                    ));
        }

        Optional<Booking> foundBooking =
                bookingRepository.findById(id);

        if (foundBooking.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Booking not found."));
        }

        Booking booking = foundBooking.get();
        Service service = booking.getService();

        // Verify this booking belongs to the logged-in provider.
        if (service == null || service.getProvider() == null
                || !service.getProvider().getId()
                        .equals(provider.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message",
                            "You cannot manage this booking."
                    ));
        }

        // Only pending bookings can be changed.
        if (!"PENDING".equals(booking.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Only pending bookings can be updated."
                    ));
        }

        booking.setStatus(newStatus);
        Booking savedBooking = bookingRepository.save(booking);

        return ResponseEntity.ok(bookingDetails(savedBooking));
    }
}
