package com.example.timerecorder.repository;

import com.example.timerecorder.model.TimeRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DataJdbcTest
@ContextConfiguration(classes = com.example.timerecorder.TestcontainersConfiguration.class)
class TimeRecordRepositoryIntegrationTest {

    private static final long TOLERANCE_MILLIS = 1;

    @Autowired
    private TimeRecordRepository repository;

    @Test
    void insertInstant_savesRecordToDatabase() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));

        repository.insertZonedDateTime(now);

        List<TimeRecord> list = (List<TimeRecord>) repository.findAll();
        assertThat(list).hasSize(1);

        TimeRecord saved = list.getFirst();

        assertThat(saved.recordedAt().toInstant().toEpochMilli())
                .isCloseTo(now.toInstant().toEpochMilli(), within(TOLERANCE_MILLIS));
    }

    @Test
    void findAllNoOrder_returnsAllRecords() {
        ZonedDateTime t1 = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
        ZonedDateTime t2 = t1.plusSeconds(10);
        ZonedDateTime t3 = t1.minusSeconds(5);

        repository.insertZonedDateTime(t1);
        repository.insertZonedDateTime(t2);
        repository.insertZonedDateTime(t3);

        List<TimeRecord> records = repository.findAllNoOrder();

        assertThat(records).hasSize(3);
        assertThat(records).allMatch(r -> r.id() != null);

        List<Long> retrieved = records.stream()
                .map(r -> r.recordedAt().toInstant().toEpochMilli())
                .toList();

        List<Long> expected = List.of(
                t1.toInstant().toEpochMilli(),
                t2.toInstant().toEpochMilli(),
                t3.toInstant().toEpochMilli()
        );

        for (Long dbMillis : retrieved) {
            assertThat(expected)
                    .anySatisfy(exp -> assertThat(dbMillis)
                            .isCloseTo(exp, within(TOLERANCE_MILLIS)));
        }
    }

    @Test
    void crudOperations_workCorrectly() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));

        repository.insertZonedDateTime(now);

        TimeRecord saved = repository.findAll().iterator().next();

        assertThat(saved.id()).isNotNull();
        assertThat(saved.recordedAt().toInstant().toEpochMilli())
                .isCloseTo(now.toInstant().toEpochMilli(), within(TOLERANCE_MILLIS));

        Long id = saved.id();
        TimeRecord found = repository.findById(id).orElseThrow();

        assertThat(found.recordedAt().toInstant().toEpochMilli())
                .isCloseTo(now.toInstant().toEpochMilli(), within(TOLERANCE_MILLIS));

        repository.deleteById(id);
        assertThat(repository.existsById(id)).isFalse();
    }
}