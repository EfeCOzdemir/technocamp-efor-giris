package com.technocamp.efor.performance;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Ödevde verilen PERFORMANCE tablosunun birebir karşılığı. */
@Entity
@Table(name = "PERFORMANCE")
public class Performance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "AGENTID")
    private String agentId;

    @Column(name = "FIRSTNAME")
    private String firstName;

    @Column(name = "SURNAME")
    private String surname;

    // BEGIN ve END Oracle'da ayrılmış kelimeler olduğu için tırnaklı kullanılıyor.
    @Column(name = "`BEGIN`")
    private LocalDateTime begin;

    @Column(name = "`END`")
    private LocalDateTime end;

    @Column(name = "DATEINFO")
    private LocalDate dateInfo;

    @Column(name = "EXCUSE")
    private String excuse;

    @Column(name = "EXCUSEHOURS")
    private Integer excuseHours;

    /** Kullanılan mola (saat). */
    @Column(name = "TIMEOUT")
    private Integer timeout;

    public Long getId() {
        return id;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public LocalDateTime getBegin() {
        return begin;
    }

    public void setBegin(LocalDateTime begin) {
        this.begin = begin;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public LocalDate getDateInfo() {
        return dateInfo;
    }

    public void setDateInfo(LocalDate dateInfo) {
        this.dateInfo = dateInfo;
    }

    public String getExcuse() {
        return excuse;
    }

    public void setExcuse(String excuse) {
        this.excuse = excuse;
    }

    public Integer getExcuseHours() {
        return excuseHours;
    }

    public void setExcuseHours(Integer excuseHours) {
        this.excuseHours = excuseHours;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}
