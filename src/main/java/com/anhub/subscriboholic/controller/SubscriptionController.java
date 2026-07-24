package com.anhub.subscriboholic.controller;

import com.anhub.subscriboholic.model.dto.CreateSubscriptionRequest;
import com.anhub.subscriboholic.model.dto.SubscriptionDTO;
import com.anhub.subscriboholic.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/subscriptions")
@AllArgsConstructor
class SubscriptionController {
    private SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<SubscriptionDTO> createSubscription(@RequestBody @Valid CreateSubscriptionRequest createSubscriptionRequest) {
        SubscriptionDTO createdSubscription = subscriptionService.createSubscription(createSubscriptionRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdSubscription.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdSubscription);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionDTO> getSubscriptionById(@PathVariable Integer id) {
        SubscriptionDTO subscriptionDTO = subscriptionService.getSubscriptionById(id);
        return ResponseEntity.ok(subscriptionDTO);
    }
}
