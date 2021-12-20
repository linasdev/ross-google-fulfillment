package com.rosssmarthome.rossgooglefulfillment.controller;

import com.rosssmarthome.rossgooglefulfillment.service.FulfillmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class FulfillmentController {
    private final FulfillmentService fulfillmentService;

    @PostMapping(produces = { "application/json" })
    public String fulfill(@RequestBody String body, @RequestHeader Map<String, String> headers) {
        try {
            return fulfillmentService.handleRequest(body, headers).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to handle fulfillment request", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
