package com.anurag.aiml.service;

import com.anurag.aiml.entity.ServiceProvider;
import com.anurag.aiml.repository.ServiceProviderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceProviderService {

    private final ServiceProviderRepository repository;

    public ServiceProviderService(ServiceProviderRepository repository) {
        this.repository = repository;
    }

    public List<ServiceProvider> getAllProviders() {
        return repository.findAll();
    }

    public Optional<ServiceProvider> getProviderById(Long id) {
        return repository.findById(id);
    }

    public ServiceProvider addProvider(ServiceProvider provider) {
        return repository.save(provider);
    }

    public ServiceProvider updateProvider(Long id, ServiceProvider provider) {

        ServiceProvider existingProvider =
                repository.findById(id).orElseThrow();

        existingProvider.setName(provider.getName());
        existingProvider.setEmail(provider.getEmail());
        existingProvider.setPhone(provider.getPhone());
        existingProvider.setLocation(provider.getLocation());
        existingProvider.setDescription(provider.getDescription());
        existingProvider.setExperience(provider.getExperience());

        return repository.save(existingProvider);
    }

    public void deleteProvider(Long id) {
        repository.deleteById(id);
    }

    public List<ServiceProvider> searchByLocation(String location) {
        return repository.findByLocationIgnoreCase(location);
    }

    public List<ServiceProvider> searchByName(String name) {
        return repository.findByNameContainingIgnoreCase(name);
    }
}