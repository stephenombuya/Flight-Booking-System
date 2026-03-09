package com.flightbookingapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA auditing so that {@code @CreatedDate} and
 * {@code @LastModifiedDate} fields are automatically populated.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
