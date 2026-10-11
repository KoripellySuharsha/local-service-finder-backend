package com.anurag.aiml.service;

import com.anurag.aiml.repository.ServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceService {

    private final ServiceRepository repository;

    public ServiceService(ServiceRepository repository) {
        this.repository = repository;
    }

    public List<com.anurag.aiml.entity.Service> getAllServices() {
        return repository.findAll();
    }

    public com.anurag.aiml.entity.Service getServiceById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public com.anurag.aiml.entity.Service addService(
            com.anurag.aiml.entity.Service service) {

        return repository.save(service);
    }

    public com.anurag.aiml.entity.Service updateService(
            Long id,
            com.anurag.aiml.entity.Service service) {

        com.anurag.aiml.entity.Service existingService =
                repository.findById(id).orElseThrow();

        existingService.setServiceName(service.getServiceName());
        existingService.setCategory(service.getCategory());
        existingService.setDescription(service.getDescription());
        existingService.setPrice(service.getPrice());
        existingService.setLocation(service.getLocation());
        existingService.setProvider(service.getProvider());

        return repository.save(existingService);
    }

    public void deleteService(Long id) {
        repository.deleteById(id);
    }

    public List<com.anurag.aiml.entity.Service> searchByCategory(
            String category) {

        return repository.findByCategoryIgnoreCase(category);
    }

    public List<com.anurag.aiml.entity.Service> searchByLocation(
            String location) {

        return repository.findByLocationIgnoreCase(location);
    }

    public List<com.anurag.aiml.entity.Service> searchByName(
            String serviceName) {

        return repository.findByServiceNameContainingIgnoreCase(serviceName);
    }

    public List<com.anurag.aiml.entity.Service> searchByCategoryAndLocation(
            String category,
            String location) {

        return repository.findByCategoryIgnoreCaseAndLocationIgnoreCase(
                category, location);
    }
}