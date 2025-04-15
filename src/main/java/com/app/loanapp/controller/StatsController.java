package com.app.loanapp.controller;

import com.app.loanapp.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    @Autowired
    private StatsService statsService;

    @GetMapping("/loans")
    public Map<String, Object> getLoanStats() {
        return statsService.getLoanStats();
    }
}