package com.example.timerecorder.queue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class TimeQueueTest {

    @Test
    @Timeout(5)
    void add_and_take() throws InterruptedException {
        TimeQueue queue = new TimeQueue();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
        queue.add(now);
        assertEquals(1, queue.size());
        assertFalse(queue.isEmpty());
        ZonedDateTime taken = queue.take();
        assertEquals(now, taken);
    }

    @Test
    @Timeout(5)
    void take_blocks_when_empty_confirms_blocking_behavior() throws InterruptedException {
        TimeQueue queue = new TimeQueue();

        Thread takeThread = new Thread(() -> {
            try {
                queue.take();
                fail("Должно блокироваться");
            } catch (InterruptedException ignored) {
            }
        });

        takeThread.start();
        Thread.sleep(100);

        assertTrue(takeThread.isAlive());
    }

    @Test
    @Timeout(5)
    void size_and_isEmpty() throws InterruptedException {
        TimeQueue queue = new TimeQueue();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());

        queue.add(now);

        assertEquals(1, queue.size());
        assertFalse(queue.isEmpty());

        queue.take();

        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
    }

    @Test
    @Timeout(5)
    void multiple_adds_and_takes_FIFO_order() throws InterruptedException {
        TimeQueue queue = new TimeQueue();

        ZonedDateTime t1 = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
        ZonedDateTime t2 = t1.plusSeconds(1);
        ZonedDateTime t3 = t1.plusSeconds(2);

        queue.add(t1);
        queue.add(t2);
        queue.add(t3);

        assertEquals(t1, queue.take());
        assertEquals(t2, queue.take());
        assertEquals(t3, queue.take());
        assertTrue(queue.isEmpty());
    }

    @Test
    @Timeout(5)
    void blocking_put_on_full_queue() throws InterruptedException {
        TimeQueue queue = new TimeQueue() {
            private final LinkedBlockingQueue<ZonedDateTime> testQueue = new LinkedBlockingQueue<>(1);

            @Override public void add(ZonedDateTime zdt) { testQueue.offer(zdt); }
            @Override public ZonedDateTime take() throws InterruptedException { return testQueue.take(); }
            @Override public int size() { return testQueue.size(); }
            @Override public boolean isEmpty() { return testQueue.isEmpty(); }
            @Override public int remainingCapacity() { return testQueue.remainingCapacity(); }
        };

        queue.add(ZonedDateTime.now(ZoneId.of("Europe/Moscow")));
        assertEquals(0, queue.remainingCapacity());

        queue.add(ZonedDateTime.now(ZoneId.of("Europe/Moscow")));

        assertEquals(1, queue.size());
        queue.take();
        assertEquals(0, queue.size());
    }
}