package com.example.timerecorder.scheduler;

import com.example.timerecorder.queue.TimeQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class TimeTicker {

    private final TimeQueue queue;
    private final Clock clock;

    public TimeTicker(TimeQueue queue, @Autowired(required = false) Clock clock) {
        this.queue = queue;
        this.clock = (clock != null) ? clock : Clock.systemDefaultZone();
    }

    @Scheduled(fixedRate = 1000)
    public void tick() {
        try {
            queue.add(clock.instant());
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }
}