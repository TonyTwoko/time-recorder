package com.example.timerecorder.repository;

import com.example.timerecorder.model.TimeRecord;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.List;

public interface TimeRecordRepository extends CrudRepository<TimeRecord, Long> {

    @Modifying
    @Query("INSERT INTO time_records (recorded_at) VALUES (:recordedAt)")
    void insertInstant(Instant recordedAt);

    @Query("SELECT * FROM time_records")
    List<TimeRecord> findAllNoOrder();
}