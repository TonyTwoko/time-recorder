package com.example.timerecorder.service;

import com.example.timerecorder.queue.TimeQueue;
import com.example.timerecorder.repository.TimeRecordRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class DbWriterService {

    private static final Logger log = LoggerFactory.getLogger(DbWriterService.class);

    private final TimeQueue queue;
    private final TimeRecordRepository repository;
    @Getter
    private final Thread writerThread;
    private volatile boolean running = true;

    public DbWriterService(TimeQueue queue, TimeRecordRepository repository) {
        this.queue = queue;
        this.repository = repository;
        this.writerThread = new Thread(this::runWriter, "db-writer-thread");
        this.writerThread.setDaemon(true);
    }

    @PostConstruct
    public void start() {
        writerThread.start();
        log.info("DbWriterService запущен. Поток: {}", writerThread.getName());
    }

    @PreDestroy
    public void stop() {
        log.info("Остановка DbWriterService... (в очереди осталось: {} элементов)", queue.size());
        running = false;
        writerThread.interrupt();
        try {
            writerThread.join(10_000);
            log.info("DbWriterService успешно остановлен.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Прерывание во время ожидания завершения DbWriterService.", e);
        }
    }

    private void runWriter() {
        log.debug("Цикл runWriter запущен в потоке: {}", Thread.currentThread().getName());
        while (running) {
            Instant instant;
            try {
                instant = queue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.debug("Поток DbWriterService '{}' прерван во время ожидания элемента, выход из цикла runWriter.",
                        Thread.currentThread().getName());
                break;
            }
            if (instant != null) {
                writeToDb(instant);
            }
        }
        log.debug("Цикл runWriter завершен в потоке: {}", Thread.currentThread().getName());
    }

    private void writeToDb(Instant instant) {
        int attempt = 0;
        while (running) {
            attempt++;
            try {
                repository.insertInstant(instant);
                log.info("Успешно записано в БД: {} (попытка #{}, в очереди осталось: {})",
                        instant, attempt, queue.size());
                return;
            } catch (DataAccessException e) {
                log.warn("Ошибка записи в БД: {} (попытка #{}, в очереди: {} элементов). Повтор через 5 сек...",
                        instant, attempt, queue.size(), e);
                if (!running) {
                    log.debug("Остановка во время ретрая для {}", instant);
                    return;
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.debug("Прервано во время ожидания ретрая для {}", instant);
                    return;
                }
            }
        }
    }
}