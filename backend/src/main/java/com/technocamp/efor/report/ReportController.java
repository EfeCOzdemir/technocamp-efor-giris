package com.technocamp.efor.report;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.technocamp.efor.performance.PerformanceResponse;
import com.technocamp.efor.performance.PerformanceService;
import com.technocamp.efor.user.AppUserRepository;
import com.technocamp.efor.user.Role;

/** Sadece ADMIN rolü erişebilir (bkz. SecurityConfig). */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    public record AgentOption(String agentId, String fullName) {
    }

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final PerformanceService performanceService;
    private final AppUserRepository userRepository;
    private final ExcelExporter excelExporter;

    public ReportController(PerformanceService performanceService, AppUserRepository userRepository,
            ExcelExporter excelExporter) {
        this.performanceService = performanceService;
        this.userRepository = userRepository;
        this.excelExporter = excelExporter;
    }

    @GetMapping("/agents")
    public List<AgentOption> agents() {
        return userRepository.findByRoleOrderByFirstNameAscSurnameAsc(Role.AGENT).stream()
                .map(u -> new AgentOption(u.getAgentId(), u.getFirstName() + " " + u.getSurname()))
                .toList();
    }

    @GetMapping("/performances")
    public List<PerformanceResponse> performances(
            @RequestParam(required = false) String agentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return performanceService.search(agentId, from, to);
    }

    /** Ekrandaki filtrelerle aynı sonucu Excel olarak indirir. */
    @GetMapping("/performances/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String agentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to)
            throws IOException {
        byte[] file = excelExporter.export(performanceService.search(agentId, from, to));
        String filename = "performans-raporu-" + LocalDate.now() + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .contentType(XLSX)
                .body(file);
    }
}
