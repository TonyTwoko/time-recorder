package com.example.timerecorder.controller;

import com.example.timerecorder.model.TimeRecord;
import com.example.timerecorder.queue.TimeQueue;
import com.example.timerecorder.repository.TimeRecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TimeController.class)
class TimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TimeRecordRepository mockRepository;

    @MockitoBean
    private TimeQueue mockTimeQueue;

    @Test
    void times_returnsViewAndModelAttribute() throws Exception {
        Instant now = Instant.now();
        TimeRecord record1 = new TimeRecord(1L, now);
        TimeRecord record2 = new TimeRecord(2L, now.plusSeconds(1));
        List<TimeRecord> records = List.of(record1, record2);

        when(mockRepository.findAllNoOrder()).thenReturn(records);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("times"))
                .andExpect(model().attribute("records", records));

        verify(mockRepository).findAllNoOrder();
    }

    @Test
    void times_handlesEmptyRecords() throws Exception {
        when(mockRepository.findAllNoOrder()).thenReturn(List.of());

        mockMvc.perform(get("/times"))
                .andExpect(status().isOk())
                .andExpect(view().name("times"))
                .andExpect(model().attribute("records", List.of()));

        verify(mockRepository).findAllNoOrder();
    }
}