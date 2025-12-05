package com.example.timerecorder.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class TimeQueue {

    private static final Logger log = LoggerFactory.getLogger(TimeQueue.class);

    private final BlockingQueue<Instant> queue = new LinkedBlockingQueue<>();

    public void add(Instant instant) {
        try {
            queue.put(instant);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Прервано при добавлении в очередь", e);
        }
    }

    public Instant take() throws InterruptedException {
        return queue.take();
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    public int getSize() {
        return queue.size();
    }
}