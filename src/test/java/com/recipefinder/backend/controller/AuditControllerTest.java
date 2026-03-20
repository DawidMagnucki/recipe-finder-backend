package com.recipefinder.backend.controller;

import com.recipefinder.backend.domain.AuditLog;
import com.recipefinder.backend.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditController.class)
@ActiveProfiles("test")
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private AuditLogRepository auditLogRepository;

    @Test
    void shouldReturnAllLogs() throws Exception {
        AuditLog log = AuditLog.builder()
                .id(1L)
                .action("SAVE")
                .details("Saved item")
                .timestamp(LocalDateTime.of(2026, 3, 18, 10, 0))
                .build();
        when(auditLogRepository.findAll()).thenReturn(List.of(log));

        mockMvc.perform(get("/api/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("SAVE"))
                .andExpect(jsonPath("$[0].details").value("Saved item"));
    }

    @Test
    void shouldReturnLogsCount() throws Exception {
        when(auditLogRepository.count()).thenReturn(7L);

        mockMvc.perform(get("/api/logs/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("7"));
    }

    @Test
    void shouldClearLogs() throws Exception {
        mockMvc.perform(delete("/api/logs/clear"))
                .andExpect(status().isOk());

        verify(auditLogRepository).deleteAll();
    }

    @Test
    void shouldFilterLogsByAction() throws Exception {
        AuditLog matching = AuditLog.builder().id(1L).action("LOGIN").details("ok").build();
        AuditLog ignored = AuditLog.builder().id(2L).action("SAVE").details("skip").build();
        when(auditLogRepository.findAll()).thenReturn(List.of(matching, ignored));

        mockMvc.perform(get("/api/logs/filter").param("action", "login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].action").value("LOGIN"));
    }

    @Test
    void shouldReturnAuditStatus() throws Exception {
        when(auditLogRepository.count()).thenReturn(12L);

        mockMvc.perform(get("/api/logs/status"))
                .andExpect(status().isOk())
                .andExpect(content().string("Audit System is online. Total logs: 12"));
    }
}
