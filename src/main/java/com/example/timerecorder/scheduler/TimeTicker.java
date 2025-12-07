package com.example.timerecorder.scheduler;

import com.example.timerecorder.queue.TimeQueue;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class TimeTicker {

    private final TimeQueue queue;
    private final Clock clock;

    public TimeTicker(TimeQueue queue) {
        this.queue = queue;
        this.clock = Clock.system(ZoneId.of("Europe/Moscow"));
    }

    @Scheduled(fixedRate = 1000)
    public void tick() {
        ZonedDateTime now = ZonedDateTime.now(clock);
        queue.add(now);
    }
}