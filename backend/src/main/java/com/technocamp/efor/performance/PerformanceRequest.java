package com.technocamp.efor.performance;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PerformanceRequest(
        @NotNull(message = "Gün seçilmelidir") LocalDate dateInfo,
        @NotNull(message = "Mesai başlangıç saati seçilmelidir") LocalTime beginTime,
        @NotNull(message = "Mesai bitiş saati seçilmelidir") LocalTime endTime,
        @NotNull(message = "Mola girilmelidir") @Min(value = 0, message = "Mola negatif olamaz") @Max(value = 24, message = "Mola 24 saatten fazla olamaz") Integer timeout,
        String excuse,
        @Min(value = 0, message = "Mazeret saati negatif olamaz") @Max(value = 24, message = "Mazeret saati 24 saatten fazla olamaz") Integer excuseHours) {
}
