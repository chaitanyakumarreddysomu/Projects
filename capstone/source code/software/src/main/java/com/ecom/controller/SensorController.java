package com.ecom.controller;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@RestController
@RequestMapping("/api/sensors")
public class SensorController {

    private static final Logger logger = LoggerFactory.getLogger(SensorController.class);

    // Store the latest sensor data
    private Map<String, Object> latestSensorData;

    // Endpoint to receive sensor data from the Arduino
    @PostMapping
    public ResponseEntity<String> receiveSensorData(@RequestBody Map<String, Object> sensorData) {
        // Store the received data
        latestSensorData = sensorData;

        // Log the received data for debugging
        logger.info("Received sensor data: {}", sensorData);

        return ResponseEntity.ok("Data received successfully");
    }

    // Endpoint to get the latest sensor data
    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> getLatestSensorData() {
        if (latestSensorData == null) {
            return ResponseEntity.ok(Map.of("message", "No data received yet"));
        }
        return ResponseEntity.ok(latestSensorData);
    }
}

