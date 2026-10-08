package com.instakill.infrastructure.event;

import com.instakill.interaction.domain.DomainEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Null Object publisher: accepts events and intentionally does nothing with them.
 */
@Component
public class NoOpDomainEventPublisher implements DomainEventPublisher {

    @Override
    public void publish(Object event) {
        // intentionally empty
    }
}
