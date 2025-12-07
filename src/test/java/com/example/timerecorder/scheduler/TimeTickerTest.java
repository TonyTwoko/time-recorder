package com.example.timerecorder.scheduler;

import com.example.timerecorder.queue.TimeQueue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TimeTickerTest {

    @Mock
    TimeQueue mockQueue;

    @Captor
    ArgumentCaptor<ZonedDateTime> captor;

    @Test
    void tick_adds_current_time_to_queue() {
        TimeTicker ticker = new TimeTicker(mockQueue);

        ticker.tick();

        verify(mockQueue).add(captor.capture());

        ZonedDateTime captured = captor.getValue();
        assertEquals(ZoneId.of("Europe/Moscow"), captured.getZone());
    }
}