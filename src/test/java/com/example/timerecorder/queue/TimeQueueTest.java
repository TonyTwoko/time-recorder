package com.example.timerecorder.queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.awaitility.Awaitility.await;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class TimeQueueTest {

    private TimeQueue queue;

    @BeforeEach
    void setUp() {
        queue = new TimeQueue();

        // Подготовка приватного поля capacity
        ReflectionTestUtils.setField(queue, "capacity", 1000);

        // Инициализация очереди
        queue.init();

        // Подмена приватного queue на тестовый экземпляр
        LinkedBlockingQueue<ZonedDateTime> testQueue =
                new LinkedBlockingQueue<>(1000);
        ReflectionTestUtils.setField(queue, "queue", testQueue);
    }

    @Test
    void add_and_take() throws InterruptedException {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));

        queue.add(now);

        assertEquals(1, queue.size());
        assertEquals(now, queue.take());
        assertTrue(queue.isEmpty());
    }

    @Test
    void fifo_order() throws InterruptedException {
        ZonedDateTime t1 = ZonedDateTime.of(2025, 1, 1, 10, 0, 0, 0,
                ZoneId.of("Europe/Moscow"));
        ZonedDateTime t2 = t1.plusSeconds(1);
        ZonedDateTime t3 = t1.plusSeconds(2);

        queue.add(t1);
        queue.add(t2);
        queue.add(t3);

        assertEquals(t1, queue.take());
        assertEquals(t2, queue.take());
        assertEquals(t3, queue.take());
    }

    @Test
    void put_blocks_when_queue_is_full_awaitility() throws Exception {
        // маленькая очередь
        LinkedBlockingQueue<ZonedDateTime> smallQueue =
                new LinkedBlockingQueue<>(1);

        ReflectionTestUtils.setField(queue, "queue", smallQueue);

        // заполняем очередь
        queue.add(ZonedDateTime.now());

        Thread t = new Thread(() -> {
            queue.add(ZonedDateTime.now()); // должен заблокироваться
        });
        t.start();

        // ждём, пока поток заблокируется
        await().atMost(200, MILLISECONDS).until(t::isAlive);

        // освобождаем место
        queue.take();

        // ждём завершения потока
        t.join(500);
        assertFalse(t.isAlive(), "После освобождения места поток должен завершиться");
    }

    @Test
    void remainingCapacity_works_correctly() throws InterruptedException {
        assertEquals(1000, queue.remainingCapacity());

        queue.add(ZonedDateTime.now());
        assertEquals(999, queue.remainingCapacity());

        queue.take();
        assertEquals(1000, queue.remainingCapacity());
    }

    @Test
    void works_with_small_capacity() {
        LinkedBlockingQueue<ZonedDateTime> smallQueue =
                new LinkedBlockingQueue<>(3);

        ReflectionTestUtils.setField(queue, "queue", smallQueue);

        queue.add(ZonedDateTime.now());
        queue.add(ZonedDateTime.now());
        queue.add(ZonedDateTime.now());

        assertEquals(3, queue.size());
        assertEquals(0, queue.remainingCapacity());
    }
}
