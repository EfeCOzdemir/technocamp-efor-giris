package com.technocamp.efor.config;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.technocamp.efor.performance.PerformanceRequest;
import com.technocamp.efor.performance.PerformanceService;
import com.technocamp.efor.user.AppUser;
import com.technocamp.efor.user.AppUserRepository;
import com.technocamp.efor.user.Role;

/** Veritabanı boşsa demo kullanıcıları ve örnek efor kayıtlarını oluşturur. */
@Component
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final PerformanceService performanceService;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(AppUserRepository userRepository, PerformanceService performanceService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.performanceService = performanceService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        userRepository.save(new AppUser("admin", passwordEncoder.encode("admin123"), Role.ADMIN,
                "glb00000001", "Sistem", "Yöneticisi"));
        AppUser ayse = userRepository.save(new AppUser("ayse", passwordEncoder.encode("ayse123"), Role.AGENT,
                "glb90084448", "Ayşe", "Yılmaz"));
        AppUser mehmet = userRepository.save(new AppUser("mehmet", passwordEncoder.encode("mehmet123"), Role.AGENT,
                "glb12312312", "Mehmet", "Demir"));

        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate lastMonday = monday.minusWeeks(1);
        for (int i = 0; i < 5; i++) {
            performanceService.create(ayse, entry(lastMonday.plusDays(i), "08:30", "17:30", 1,
                    i == 2 ? "Doktor Randevusu" : null, i == 2 ? 2 : 0));
            performanceService.create(mehmet, entry(lastMonday.plusDays(i), "09:00", "18:00", 1,
                    null, 0));
        }
        performanceService.create(ayse, entry(monday, "08:30", "17:30", 1, null, 0));
        performanceService.create(mehmet, entry(monday, "09:00", "13:00", 0, "Mazeret İzni", 4));
    }

    private static PerformanceRequest entry(LocalDate day, String begin, String end, int timeout, String excuse,
            int excuseHours) {
        return new PerformanceRequest(day, LocalTime.parse(begin), LocalTime.parse(end), timeout, excuse, excuseHours);
    }
}
