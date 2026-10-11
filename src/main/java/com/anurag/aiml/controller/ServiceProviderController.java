package com.anurag.aiml.controller;

import com.anurag.aiml.entity.ServiceProvider;
import com.anurag.aiml.service.ServiceProviderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/providers")
@CrossOrigin(origins = "http://localhost:5173")
public class ServiceProviderController {

    private final ServiceProviderService service;

    public ServiceProviderController(ServiceProviderService service) {
        this.service = service;
    }

    @GetMapping
    public List<ServiceProvider> getAllProviders() {
        return service.getAllProviders();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceProvider> getProviderById(
            @PathVariable Long id) {

        return service.getProviderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ServiceProvider addProvider(
            @RequestBody ServiceProvider provider) {

        return service.addProvider(provider);
    }

    @PutMapping("/{id}")
    public ServiceProvider updateProvider(
            @PathVariable Long id,
            @RequestBody ServiceProvider provider) {

        return service.updateProvider(id, provider);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProvider(
            @PathVariable Long id) {

        service.deleteProvider(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/location")
    public List<ServiceProvider> searchByLocation(
            @RequestParam String location) {

        return service.searchByLocation(location);
    }

    @GetMapping("/search/name")
    public List<ServiceProvider> searchByName(
            @RequestParam String name) {

        return service.searchByName(name);
    }
}