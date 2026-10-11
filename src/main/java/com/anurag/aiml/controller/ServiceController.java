package com.anurag.aiml.controller;

import com.anurag.aiml.entity.Service;
import com.anurag.aiml.service.ServiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "http://localhost:5173")
public class ServiceController {

    private final ServiceService service;

    public ServiceController(ServiceService service) {
        this.service = service;
    }

    @GetMapping
    public List<Service> getAllServices() {
        return service.getAllServices();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Service> getServiceById(
            @PathVariable Long id) {

        Service result = service.getServiceById(id);

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping
    public Service addService(
            @RequestBody Service serviceData) {

        return service.addService(serviceData);
    }

    @PutMapping("/{id}")
    public Service updateService(
            @PathVariable Long id,
            @RequestBody Service serviceData) {

        return service.updateService(id, serviceData);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(
            @PathVariable Long id) {

        service.deleteService(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/category")
    public List<Service> searchByCategory(
            @RequestParam String category) {

        return service.searchByCategory(category);
    }

    @GetMapping("/search/location")
    public List<Service> searchByLocation(
            @RequestParam String location) {

        return service.searchByLocation(location);
    }

    @GetMapping("/search/name")
    public List<Service> searchByName(
            @RequestParam String name) {

        return service.searchByName(name);
    }

    @GetMapping("/search")
    public List<Service> searchByCategoryAndLocation(
            @RequestParam String category,
            @RequestParam String location) {

        return service.searchByCategoryAndLocation(
                category, location);
    }
}