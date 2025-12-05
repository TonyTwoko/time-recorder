package com.example.timerecorder.service;

import com.example.timerecorder.queue.TimeQueue;
import com.example.timerecorder.repository.TimeRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DbWriterServiceTest {

    @Mock
    private TimeQueue mockQueue;

    @Mock
    private TimeRecordRepository mockRepository;

    private DbWriterService dbWriterService;
    private final Logger log = LoggerFactory.getLogger(DbWriterService.class);

    @BeforeEach
    void setUp() {
        dbWriterService = new DbWriterService(mockQueue, mockRepository);
    }

    @Test
    void start_createsAndStartsThread() throws InterruptedException {
        dbWriterService.start();
        Thread.sleep(100);
        dbWriterService.stop();
        Thread writerThread = dbWriterService.getWriterThread();
        writerThread.join(5000);
        assertTrue(!writerThread.isAlive(), "Поток DbWriterService должен завершиться после stop().");
    }

    @Test
    void runWriter_exitsOnInterruptedException() throws InterruptedException {
        when(mockQueue.take()).thenThrow(new InterruptedException("Simulated interruption for test"));
        dbWriterService.start();
        Thread.sleep(200);
        verify(mockQueue, timeout(500).times(1)).take();
        verify(mockRepository, never()).insertInstant(any(Instant.class));
        dbWriterService.stop();
    }

    @Test
    void runWriter_processesElementsFromQueue() throws InterruptedException {
        Instant instant1 = Instant.now();
        Instant instant2 = instant1.plusSeconds(1);
        CountDownLatch firstElementTaken = new CountDownLatch(1);
        CountDownLatch secondElementTaken = new CountDownLatch(1);
        when(mockQueue.take())
                .thenAnswer(invocation -> {
                    firstElementTaken.countDown();
                    return instant1;
                })
                .thenAnswer(invocation -> {
                    secondElementTaken.countDown();
                    return instant2;
                })
                .thenThrow(new InterruptedException("Simulated interruption for test"));
        dbWriterService.start();
        assertTrue(firstElementTaken.await(5, TimeUnit.SECONDS));
        assertTrue(secondElementTaken.await(5, TimeUnit.SECONDS));
        Thread.sleep(200);
        verify(mockRepository, timeout(500).times(2)).insertInstant(any(Instant.class));

        ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
        verify(mockRepository, timeout(500).times(2)).insertInstant(captor.capture());
        List<Instant> capturedInstants = captor.getAllValues();
        assertEquals(2, capturedInstants.size());
        assertEquals(instant1, capturedInstants.get(0));
        assertEquals(instant2, capturedInstants.get(1));

        dbWriterService.stop();
    }

    @Test
    void writeToDb_retriesOnDataAccessException() throws InterruptedException {
        Instant instant = Instant.now();
        DataAccessException exception = new DataAccessException("DB Connection Lost") {
        };
        CountDownLatch elementTaken = new CountDownLatch(1);
        when(mockQueue.take())
                .thenAnswer(invocation -> {
                    elementTaken.countDown();
                    return instant;
                })
                .thenThrow(new InterruptedException("Stop after first element"));
        doThrow(exception)
                .doThrow(exception)
                .doNothing()
                .when(mockRepository).insertInstant(eq(instant));
        dbWriterService.start();
        assertTrue(elementTaken.await(5, TimeUnit.SECONDS));
        Thread.sleep(15000);
        verify(mockRepository, timeout(15000).times(3)).insertInstant(eq(instant));
        dbWriterService.stop();
    }

    @Test
    void stop_interruptsThread() throws InterruptedException {
        dbWriterService.start();
        Thread.sleep(100);
        dbWriterService.stop();
        Thread writerThread = dbWriterService.getWriterThread();
        assertTrue(writerThread.isInterrupted(),
                "Внутренний поток DbWriterService должен быть прерван при вызове stop().");
    }
}