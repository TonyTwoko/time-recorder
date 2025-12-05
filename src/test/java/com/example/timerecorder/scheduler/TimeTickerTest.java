package com.example.timerecorder.scheduler;

import com.example.timerecorder.queue.TimeQueue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeTickerTest {

    @Mock
    private TimeQueue mockQueue;

    @Captor
    private ArgumentCaptor<Instant> instantCaptor;

    @Test
    void tick_adds_current_time_from_clock_to_queue() {
        Instant fixedInstant = Instant.parse("2023-10-27T10:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
        TimeTicker ticker = new TimeTicker(mockQueue, fixedClock);
        ticker.tick();
        verify(mockQueue).add(instantCaptor.capture());
        Instant capturedInstant = instantCaptor.getValue();
        assertEquals(fixedInstant, capturedInstant);
    }

    @Test
    void tick_adds_system_time_if_clock_not_provided() {
        tick_adds_current_time_from_clock_to_queue();
    }
}