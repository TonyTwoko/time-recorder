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

import java.time.ZonedDateTime;

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
            ZonedDateTime time;
            try {
                time = queue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.debug("Поток '{}' прерван при ожидании элемента, выход.",
                        Thread.currentThread().getName());
                break;
            }

            if (time != null) {
                writeToDb(time);
            }
        }
        log.debug("Цикл runWriter завершен.");
    }

    private void writeToDb(ZonedDateTime time) {
        int attempt = 0;
        while (running) {
            attempt++;
            try {
                repository.insertZonedDateTime(time);
                log.info("Успешно записано в БД: {} (попытка #{}, осталось в очереди: {})",
                        time, attempt, queue.size());
                return;
            } catch (DataAccessException e) {
                log.warn("Ошибка записи {} (попытка #{}, очередь: {}). Повтор через 5 сек...",
                        time, attempt, queue.size(), e);

                if (!running) {
                    log.debug("Остановка во время ретрая: {}", time);
                    return;
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.debug("Прервано во время ожидания ретрая: {}", time);
                    return;
                }
            }
        }
    }
}