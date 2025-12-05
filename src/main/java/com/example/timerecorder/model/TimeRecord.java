package com.example.timerecorder.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("time_records")
public record TimeRecord(
        @Id Long id,
        Instant recordedAt
) {
    public TimeRecord(Instant recordedAt) {
        this(null, recordedAt);
    }
}