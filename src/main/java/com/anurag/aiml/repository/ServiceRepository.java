package com.anurag.aiml.repository;

import com.anurag.aiml.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findByCategoryIgnoreCase(String category);

    List<Service> findByLocationIgnoreCase(String location);

    List<Service> findByServiceNameContainingIgnoreCase(String serviceName);

    List<Service> findByCategoryIgnoreCaseAndLocationIgnoreCase(
            String category, String location);
}