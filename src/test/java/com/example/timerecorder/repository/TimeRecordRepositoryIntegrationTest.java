package com.example.timerecorder.repository;

import com.example.timerecorder.model.TimeRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DataJdbcTest
@ContextConfiguration(classes = com.example.timerecorder.TestcontainersConfiguration.class)
class TimeRecordRepositoryIntegrationTest {

    private static final long TOLERANCE_MILLIS = 1L;

    @Autowired
    private TimeRecordRepository repository;

    @Test
    void insertInstant_savesRecordToDatabase() {
        Instant instantToSave = Instant.now();

        repository.insertInstant(instantToSave);

        List<TimeRecord> allRecords = (List<TimeRecord>) repository.findAll();

        assertThat(allRecords)
                .hasSize(1);

        TimeRecord savedRecord = allRecords.get(0);
        assertThat(savedRecord.id()).isNotNull();

        long savedMillis = savedRecord.recordedAt().toEpochMilli();
        long expectedMillis = instantToSave.toEpochMilli();
        assertThat(savedMillis).isCloseTo(expectedMillis, within(TOLERANCE_MILLIS));
    }

    @Test
    void findAllNoOrder_returnsAllRecords() {
        Instant instant1 = Instant.now();
        Instant instant2 = instant1.plusSeconds(10);
        Instant instant3 = instant1.minusSeconds(5);

        repository.insertInstant(instant1);
        repository.insertInstant(instant2);
        repository.insertInstant(instant3);

        List<TimeRecord> retrievedRecords = repository.findAllNoOrder();

        assertThat(retrievedRecords).hasSize(3);

        List<Long> retrievedMillis = retrievedRecords.stream()
                .map(record -> record.recordedAt().toEpochMilli())
                .toList();

        List<Long> expectedMillis = List.of(
                instant1.toEpochMilli(),
                instant2.toEpochMilli(),
                instant3.toEpochMilli()
        );

        for (Long dbMillis : retrievedMillis) {
            assertThat(expectedMillis).anySatisfy(expectedMs
                    -> assertThat(dbMillis).isCloseTo(expectedMs, within(TOLERANCE_MILLIS)));
        }

        assertThat(retrievedRecords).allMatch(record -> record.id() != null);
    }

    @Test
    void crudOperations_workCorrectly() {
        Instant instant = Instant.now();

        TimeRecord savedRecord = repository.save(new TimeRecord(instant));

        assertThat(savedRecord.id()).isNotNull();
        long savedMillis = savedRecord.recordedAt().toEpochMilli();
        long expectedMillis = instant.toEpochMilli();
        assertThat(savedMillis).isCloseTo(expectedMillis, within(TOLERANCE_MILLIS));

        Long savedId = savedRecord.id();
        TimeRecord foundRecord = repository.findById(savedId).orElse(null);

        assertThat(foundRecord).isNotNull();
        assertThat(foundRecord.id()).isEqualTo(savedId);
        long foundMillis = foundRecord.recordedAt().toEpochMilli();
        assertThat(foundMillis).isCloseTo(savedMillis, within(TOLERANCE_MILLIS));

        repository.deleteById(savedId);

        boolean existsAfterDelete = repository.existsById(savedId);
        assertThat(existsAfterDelete).isFalse();

        List<TimeRecord> allRecordsAfterDelete = repository.findAllNoOrder();
        assertThat(allRecordsAfterDelete).isEmpty();
    }
}