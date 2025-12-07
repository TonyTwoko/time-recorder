package com.example.timerecorder.service;


import com.example.timerecorder.queue.TimeQueue;
import com.example.timerecorder.repository.TimeRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DbWriterServiceTest {

    @Mock TimeQueue mockQueue;
    @Mock TimeRecordRepository mockRepository;

    private DbWriterService service;

    @BeforeEach
    void setUp() {
        service = new DbWriterService(mockQueue, mockRepository);
    }

    @Test
    void start_createsAndStartsThread() throws InterruptedException {
        service.start();
        Thread.sleep(100);
        service.stop();
        Thread t = service.getWriterThread();
        t.join(5000);
        assertFalse(t.isAlive());
    }

    @Test
    void runWriter_exitsOnInterruptedException() throws InterruptedException {
        when(mockQueue.take()).thenThrow(new InterruptedException());
        service.start();
        Thread.sleep(200);
        verify(mockQueue, timeout(500).times(1)).take();
        verify(mockRepository, never()).insertZonedDateTime(any());
        service.stop();
    }

    @Test
    void runWriter_processesElementsFromQueue() throws InterruptedException {
        ZonedDateTime t1 = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
        ZonedDateTime t2 = t1.plusSeconds(1);

        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);

        when(mockQueue.take())
                .thenAnswer(i -> { latch1.countDown(); return t1; })
                .thenAnswer(i -> { latch2.countDown(); return t2; })
                .thenThrow(new InterruptedException());

        service.start();

        assertTrue(latch1.await(5, TimeUnit.SECONDS));
        assertTrue(latch2.await(5, TimeUnit.SECONDS));
        Thread.sleep(200);

        ArgumentCaptor<ZonedDateTime> captor = ArgumentCaptor.forClass(ZonedDateTime.class);

        verify(mockRepository, timeout(500).times(2)).insertZonedDateTime(captor.capture());

        List<ZonedDateTime> values = captor.getAllValues();
        assertEquals(t1, values.get(0));
        assertEquals(t2, values.get(1));

        service.stop();
    }

    @Test
    void writeToDb_retriesOnDataAccessException() throws InterruptedException {
        ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));

        DataAccessException ex = new DataAccessException("fail") {};

        CountDownLatch latch = new CountDownLatch(1);

        when(mockQueue.take())
                .thenAnswer(i -> { latch.countDown(); return zdt; })
                .thenThrow(new InterruptedException());

        doThrow(ex).doThrow(ex).doNothing().when(mockRepository).insertZonedDateTime(eq(zdt));

        service.start();

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        Thread.sleep(15000);

        verify(mockRepository, timeout(15000).times(3)).insertZonedDateTime(eq(zdt));

        service.stop();
    }

    @Test
    void stop_interruptsThread() throws InterruptedException {
        service.start();
        Thread.sleep(100);
        service.stop();
        assertTrue(service.getWriterThread().isInterrupted());
    }
}