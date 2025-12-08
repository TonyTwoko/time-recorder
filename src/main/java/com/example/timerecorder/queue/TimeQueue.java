package com.example.timerecorder.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.ZonedDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class TimeQueue {

    private static final Logger log = LoggerFactory.getLogger(TimeQueue.class);

    @Value("${app.queue.capacity:10000}")
    private int capacity;

    private BlockingQueue<ZonedDateTime> queue;

    @PostConstruct
    public void init() {
        if (capacity <= 0) {
            throw new IllegalArgumentException("app.queue.capacity должен быть > 0");
        }
        queue = new LinkedBlockingQueue<>(capacity);
        log.info("TimeQueue инициализирована с capacity = {}", capacity);
    }

    public void add(ZonedDateTime value) {
        try {
            queue.put(value);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Прервано при добавлении в очередь", e);
        }
    }

    public ZonedDateTime take() throws InterruptedException {
        return queue.take();
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int remainingCapacity() {
        return queue.remainingCapacity();
    }

    public ZonedDateTime peek() {
        return queue.peek();
    }
}