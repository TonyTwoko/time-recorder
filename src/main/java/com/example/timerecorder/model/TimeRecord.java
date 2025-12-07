package com.example.timerecorder.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;

@Table("time_records")
public record TimeRecord(
        @Id
        Long id,
        ZonedDateTime recordedAt
) {
    public TimeRecord(ZonedDateTime recordedAt) {
        this(null, recordedAt);
    }
}