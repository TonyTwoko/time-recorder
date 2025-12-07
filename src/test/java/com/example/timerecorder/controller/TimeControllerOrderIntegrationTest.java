package com.example.timerecorder.controller;

import com.example.timerecorder.TestcontainersConfiguration;
import com.example.timerecorder.model.TimeRecord;
import com.example.timerecorder.repository.TimeRecordRepository;
import com.example.timerecorder.service.DbWriterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.task.scheduling.enabled=false")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TestcontainersConfiguration.class)
class TimeControllerOrderIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired TimeRecordRepository repository;
    @Autowired DbWriterService dbWriterService;

    @Test
    void orderIsStrictlyPreservedWithoutOrderBy() throws Exception {
        try {
            dbWriterService.stop();
        } catch (Exception ignored) {
        }

        repository.deleteAll();

        List<ZonedDateTime> expected = new ArrayList<>();
        ZonedDateTime base = ZonedDateTime.now(ZoneId.of("Europe/Moscow")).truncatedTo(ChronoUnit.SECONDS);

        for (int i = 0; i < 100; i++) {
            ZonedDateTime t = base.plusSeconds(i);
            repository.insertZonedDateTime(t);
            expected.add(t);
        }

        List<ZonedDateTime> fromRepo = repository.findAllNoOrder().stream()
                .map(TimeRecord::recordedAt)
                .toList();

        assertThat(fromRepo).hasSize(100).isEqualTo(expected);

        var result = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn();

        @SuppressWarnings("unchecked")
        List<TimeRecord> fromModel =
                (List<TimeRecord>) result.getModelAndView().getModel().get("records");

        assertThat(fromModel)
                .hasSize(100)
                .extracting(TimeRecord::recordedAt)
                .isEqualTo(expected);
    }
}