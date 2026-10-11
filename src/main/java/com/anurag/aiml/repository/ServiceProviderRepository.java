
package com.anurag.aiml.repository;

import com.anurag.aiml.entity.ServiceProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceProviderRepository
        extends JpaRepository<ServiceProvider, Long> {

    List<ServiceProvider> findByLocationIgnoreCase(String location);

    List<ServiceProvider> findByNameContainingIgnoreCase(String name);

    Optional<ServiceProvider> findByEmailIgnoreCase(String email);
}
