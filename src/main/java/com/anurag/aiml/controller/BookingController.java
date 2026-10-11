
package com.anurag.aiml.controller;

import com.anurag.aiml.entity.Booking;
import com.anurag.aiml.entity.Service;
import com.anurag.aiml.entity.User;
import com.anurag.aiml.repository.BookingRepository;
import com.anurag.aiml.repository.ServiceRepository;
import com.anurag.aiml.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    public BookingController(
            BookingRepository bookingRepository,
            ServiceRepository serviceRepository,
            UserRepository userRepository) {

        this.bookingRepository = bookingRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
    }

    public record BookingRequest(
            Long serviceId,
            LocalDate bookingDate,
            LocalTime bookingTime,
            String address,
            String notes
    ) {}

    @PostMapping
    public ResponseEntity<?> createBooking(
            @RequestBody BookingRequest request,
            HttpSession session) {

        Object sessionUserId = session.getAttribute("userId");

        if (!(sessionUserId instanceof Long userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please login to book a service."));
        }

        if (request.serviceId() == null
                || request.bookingDate() == null
                || request.bookingTime() == null
                || request.address() == null
                || request.address().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Please fill all required fields."));
        }

        if (request.bookingDate().isBefore(LocalDate.now())
                || (request.bookingDate().isEqual(LocalDate.now())
                    && !request.bookingTime().isAfter(LocalTime.now()))) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Please select a future date and time."));
        }

        Optional<User> user = userRepository.findById(userId);
        Optional<Service> service =
                serviceRepository.findById(request.serviceId());

        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "User account not found."));
        }

        if (service.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Selected service does not exist."));
        }

        Booking booking = new Booking();
        booking.setUser(user.get());
        booking.setService(service.get());
        booking.setBookingDate(request.bookingDate());
        booking.setBookingTime(request.bookingTime());
        booking.setAddress(request.address().trim());
        booking.setNotes(request.notes());
        booking.setStatus("PENDING");

        Booking savedBooking = bookingRepository.save(booking);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedBooking);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyBookings(HttpSession session) {

        Object sessionUserId = session.getAttribute("userId");

        if (!(sessionUserId instanceof Long userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please login first."));
        }

        List<Booking> bookings =
                bookingRepository.findByUserIdOrderByIdDesc(userId);

        return ResponseEntity.ok(bookings);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(
            @PathVariable Long id,
            HttpSession session) {

        Object sessionUserId = session.getAttribute("userId");

        if (!(sessionUserId instanceof Long userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please login first."));
        }

        Optional<Booking> foundBooking = bookingRepository.findById(id);

        if (foundBooking.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Booking not found."));
        }

        Booking booking = foundBooking.get();

        if (!booking.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You cannot cancel this booking."));
        }

        if (!"PENDING".equals(booking.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "Only pending bookings can be cancelled."));
        }

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        return ResponseEntity.ok(
                Map.of("message", "Booking cancelled successfully.")
        );
    }
}
