package com.technocamp.efor.performance;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.technocamp.efor.user.AppUser;
import com.technocamp.efor.user.Role;

@Service
public class PerformanceService {

    static final Sort DEFAULT_SORT = Sort.by(Sort.Order.desc("dateInfo"), Sort.Order.desc("begin"));

    private final PerformanceRepository repository;

    public PerformanceService(PerformanceRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PerformanceResponse> listOwn(AppUser user, LocalDate from, LocalDate to) {
        return search(user.getAgentId(), from, to);
    }

    @Transactional(readOnly = true)
    public List<PerformanceResponse> search(String agentId, LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Başlangıç tarihi bitiş tarihinden sonra olamaz");
        }
        return repository.findAll(PerformanceRepository.filter(agentId, from, to), DEFAULT_SORT).stream()
                .map(PerformanceResponse::from)
                .toList();
    }

    @Transactional
    public PerformanceResponse create(AppUser user, PerformanceRequest request) {
        Performance p = new Performance();
        p.setAgentId(user.getAgentId());
        p.setFirstName(user.getFirstName());
        p.setSurname(user.getSurname());
        apply(p, request);
        return PerformanceResponse.from(repository.save(p));
    }

    @Transactional
    public PerformanceResponse update(AppUser user, Long id, PerformanceRequest request) {
        Performance p = findOwned(user, id);
        apply(p, request);
        return PerformanceResponse.from(p);
    }

    @Transactional
    public void delete(AppUser user, Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Silinecek kayıt seçilmedi");
        }
        List<Performance> records = ids.stream().distinct().map(id -> findOwned(user, id)).toList();
        repository.deleteAll(records);
    }

    private Performance findOwned(AppUser user, Long id) {
        Performance p = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kayıt bulunamadı: " + id));
        if (user.getRole() != Role.ADMIN && !user.getAgentId().equals(p.getAgentId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu kayıt üzerinde yetkiniz yok");
        }
        return p;
    }

    private static void apply(Performance p, PerformanceRequest r) {
        if (!r.endTime().isAfter(r.beginTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mesai bitiş saati başlangıç saatinden sonra olmalıdır");
        }
        long shiftMinutes = Duration.between(r.beginTime(), r.endTime()).toMinutes();
        if (r.timeout() * 60L > shiftMinutes) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mola süresi mesai süresinden uzun olamaz");
        }

        String excuse = r.excuse() == null || r.excuse().isBlank() ? null : r.excuse().trim();
        int excuseHours = r.excuseHours() == null ? 0 : r.excuseHours();
        if (excuse == null && excuseHours > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mazeret saati girildiyse mazeret seçilmelidir");
        }
        if (excuse != null) {
            if (!Excuses.ALL.contains(excuse)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Geçersiz mazeret: " + excuse);
            }
            if (excuseHours == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mazeret seçildiyse mazeret saati girilmelidir");
            }
        }

        p.setDateInfo(r.dateInfo());
        p.setBegin(r.dateInfo().atTime(r.beginTime()));
        p.setEnd(r.dateInfo().atTime(r.endTime()));
        p.setTimeout(r.timeout());
        p.setExcuse(excuse);
        p.setExcuseHours(excuseHours);
    }
}
