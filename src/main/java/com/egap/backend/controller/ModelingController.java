package com.egap.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ModelingController {
    @GetMapping("/modeling/pressure")
    public Map<String, Object> getModelPressure() {
        List<Integer> qps = List.of(120, 140, 160, 180, 200, 220, 240);
        List<Integer> time = List.of(1, 2, 3, 4, 5, 6, 7);
        return Map.of("qps", qps, "time", time);
    }

    @GetMapping("/modeling/training")
    public Map<String, Object> getModelTraining() {
        List<Double> auc = List.of(0.72, 0.76, 0.81, 0.85, 0.88, 0.90);
        List<Double> loss = List.of(0.62, 0.55, 0.46, 0.38, 0.30, 0.25);
        return Map.of("auc", auc, "loss", loss);
    }
}