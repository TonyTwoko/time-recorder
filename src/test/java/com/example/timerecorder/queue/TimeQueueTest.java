package com.example.timerecorder.queue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Instant;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class TimeQueueTest {

    @Test
    @Timeout(value = 5)
    void add_and_take() throws InterruptedException {
        TimeQueue queue = new TimeQueue();
        Instant now = Instant.now();
        queue.add(now);
        assertEquals(1, queue.size());
        assertFalse(queue.isEmpty());
        Instant taken = queue.take();
        assertEquals(now, taken);
    }

    @Test
    @Timeout(value = 5)
    void take_blocks_when_empty_confirms_blocking_behavior() throws InterruptedException {
        TimeQueue queue = new TimeQueue();
        Thread takeThread = new Thread(() -> {
            try {
                Instant taken = queue.take();
                fail("take() не должен был вернуть значение в этом тесте, " +
                        "так как очередь пуста и элемент не добавляется.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        takeThread.start();
        Thread.sleep(100);
        assertTrue(takeThread.isAlive(), "Поток, вызывающий take() на пустой очереди, " +
                "должен быть жив и ждать.");
    }

    @Test
    @Timeout(value = 5)
    void size_and_isEmpty() throws InterruptedException {
        TimeQueue queue = new TimeQueue();
        Instant now = Instant.now();
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
        queue.add(now);
        assertEquals(1, queue.size());
        assertFalse(queue.isEmpty());
        queue.take();
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    @Timeout(value = 5)
    void multiple_adds_and_takes_FIFO_order() throws InterruptedException {
        TimeQueue queue = new TimeQueue();
        Instant instant1 = Instant.now();
        Instant instant2 = instant1.plusSeconds(1);
        Instant instant3 = instant1.plusSeconds(2);

        queue.add(instant1);
        queue.add(instant2);
        queue.add(instant3);

        assertEquals(3, queue.size());
        assertFalse(queue.isEmpty());

        assertEquals(instant1, queue.take());
        assertEquals(instant2, queue.take());
        assertEquals(instant3, queue.take());

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    @Timeout(value = 5)
    void blocking_put_on_full_queue() throws InterruptedException {
        TimeQueue queue = new TimeQueue() {
            private final LinkedBlockingQueue<Instant> testQueue = new LinkedBlockingQueue<>(1);

            @Override
            public void add(Instant instant) {
                testQueue.offer(instant);
            }

            @Override
            public Instant take() throws InterruptedException {
                return testQueue.take();
            }

            @Override
            public int size() {
                return testQueue.size();
            }

            @Override
            public boolean isEmpty() {
                return testQueue.isEmpty();
            }

            @Override
            public int remainingCapacity() {
                return testQueue.remainingCapacity();
            }
        };

        queue.add(Instant.now());
        assertEquals(0, queue.remainingCapacity());
        queue.add(Instant.now());
        assertEquals(1, queue.size());
        queue.take();
        assertEquals(0, queue.size());
    }
}