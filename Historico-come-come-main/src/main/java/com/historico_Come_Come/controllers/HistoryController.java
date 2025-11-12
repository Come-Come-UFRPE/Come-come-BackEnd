package com.historico_Come_Come.controllers;

import com.historico_Come_Come.dtos.HistoryRecord;
import com.historico_Come_Come.services.HistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Set<HistoryRecord>> getHistoryByUser(@PathVariable UUID userId) {

        Set<HistoryRecord> historyList = historyService.getHistoryByUserId(userId);

        return ResponseEntity.ok(historyList);
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<Void> deleteUserHistory(@PathVariable UUID userId) {

        historyService.deleteByUserId(userId);
        return ResponseEntity.noContent().build();
    }

}
