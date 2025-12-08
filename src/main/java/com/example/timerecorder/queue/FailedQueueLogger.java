package com.example.timerecorder.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class FailedQueueLogger {

    private static final Logger log = LoggerFactory.getLogger(FailedQueueLogger.class);
    private static final Path LOG_FILE = Path.of("logs/failed-queue.log");

    private final TimeQueue queue;
    private Thread loggerThread;
    private volatile boolean running = true;

    // Сохраняем уже залогированные элементы
    private final Set<ZonedDateTime> loggedElements = new HashSet<>();

    public FailedQueueLogger(TimeQueue queue) {
        this.queue = queue;
    }

    @PostConstruct
    public void start() {
        try {
            Files.createDirectories(LOG_FILE.getParent());
        } catch (IOException e) {
            log.error("Не удалось создать папку для логов", e);
        }

        loggerThread = new Thread(this::runLogger, "failed-queue-logger");
        loggerThread.setDaemon(true);
        loggerThread.start();
        log.info("FailedQueueLogger запущен.");
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (loggerThread != null) {
            loggerThread.interrupt();
            try {
                loggerThread.join(5000);
                log.info("FailedQueueLogger остановлен.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Прерывание при остановке FailedQueueLogger", e);
            }
        }
    }

    private void runLogger() {
        while (running) {
            try {
                // Копируем текущие элементы очереди
                Set<ZonedDateTime> currentSnapshot = new HashSet<>();
                queue.getAllElements().forEach(currentSnapshot::add);

                // Убираем уже залогированные
                currentSnapshot.removeAll(loggedElements);

                if (!currentSnapshot.isEmpty()) {
                    writeToFile(currentSnapshot);
                    loggedElements.addAll(currentSnapshot);
                }

                TimeUnit.MILLISECONDS.sleep(500); // интервал проверки
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void writeToFile(Set<ZonedDateTime> elements) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE.toFile(), true))) {
            for (ZonedDateTime time : elements) {
                writer.write(time.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            log.error("Не удалось записать элемент очереди в файл журнала", e);
        }
    }
}
