package com.technocamp.efor.performance;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.technocamp.efor.user.CurrentUserService;

import jakarta.validation.Valid;

/** Oturum açan temsilcinin kendi efor kayıtları. */
@RestController
@RequestMapping("/api")
public class PerformanceController {

    private final PerformanceService service;
    private final CurrentUserService currentUserService;

    public PerformanceController(PerformanceService service, CurrentUserService currentUserService) {
        this.service = service;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/excuses")
    public List<String> excuses() {
        return Excuses.ALL;
    }

    @GetMapping("/performances")
    public List<PerformanceResponse> list(Authentication auth,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.listOwn(currentUserService.get(auth), from, to);
    }

    @PostMapping("/performances")
    @ResponseStatus(HttpStatus.CREATED)
    public PerformanceResponse create(Authentication auth, @Valid @RequestBody PerformanceRequest request) {
        return service.create(currentUserService.get(auth), request);
    }

    @PutMapping("/performances/{id}")
    public PerformanceResponse update(Authentication auth, @PathVariable Long id,
            @Valid @RequestBody PerformanceRequest request) {
        return service.update(currentUserService.get(auth), id, request);
    }

    @DeleteMapping("/performances/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable Long id) {
        service.delete(currentUserService.get(auth), List.of(id));
    }

    /** Seçili satırları toplu silme ("Remove Rows"). */
    @DeleteMapping("/performances")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMany(Authentication auth, @RequestParam List<Long> ids) {
        service.delete(currentUserService.get(auth), ids);
    }
}
