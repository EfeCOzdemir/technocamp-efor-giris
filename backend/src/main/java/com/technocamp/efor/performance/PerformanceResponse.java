package com.technocamp.efor.performance;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record PerformanceResponse(
        Long id,
        String agentId,
        String firstName,
        String surname,
        LocalDate dateInfo,
        @JsonFormat(pattern = "HH:mm") LocalTime beginTime,
        @JsonFormat(pattern = "HH:mm") LocalTime endTime,
        Integer timeout,
        String excuse,
        Integer excuseHours) {

    public static PerformanceResponse from(Performance p) {
        return new PerformanceResponse(
                p.getId(),
                p.getAgentId(),
                p.getFirstName(),
                p.getSurname(),
                p.getDateInfo(),
                p.getBegin() == null ? null : p.getBegin().toLocalTime(),
                p.getEnd() == null ? null : p.getEnd().toLocalTime(),
                p.getTimeout(),
                p.getExcuse(),
                p.getExcuseHours());
    }
}
