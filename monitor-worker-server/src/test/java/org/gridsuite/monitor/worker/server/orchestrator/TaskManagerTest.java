package org.gridsuite.monitor.worker.server.orchestrator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TaskManagerTest {

    private final TaskManager taskManager = new TaskManager();

    @AfterEach
    void tearDown() {
        taskManager.shutdown();
    }

    @Test
    void submitShouldRunWorkAndRemoveCompletedTask() throws Exception {
        UUID id = UUID.randomUUID();
        CountDownLatch workRan = new CountDownLatch(1);

        var future = taskManager.submit(id, workRan::countDown);

        assertThat(workRan.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(future.get(1, TimeUnit.SECONDS)).isNull();
        assertThat(taskManager.cancelTask(id)).isFalse();
    }

    @Test
    void cancelTaskShouldInterruptRunningWork() throws Exception {
        UUID id = UUID.randomUUID();
        CountDownLatch workStarted = new CountDownLatch(1);
        CountDownLatch workInterrupted = new CountDownLatch(1);

        var future = taskManager.submit(id, () -> {
            workStarted.countDown();
            try {
                new CountDownLatch(1).await();
            } catch (InterruptedException _) {
                workInterrupted.countDown();
                Thread.currentThread().interrupt();
            }
        });

        assertThat(workStarted.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(taskManager.cancelTask(id)).isTrue();
        assertThat(future.isCancelled()).isTrue();
        assertThat(workInterrupted.await(1, TimeUnit.SECONDS)).isTrue();
        assertThat(taskManager.cancelTask(id)).isFalse();
    }

    @Test
    void cancelTaskShouldReturnFalseWhenTaskDoesNotExist() {
        assertThat(taskManager.cancelTask(UUID.randomUUID())).isFalse();
    }

    @Test
    void canceledExecutionMarkerShouldBeAddedAndConsumedOnce() {
        UUID id = UUID.randomUUID();

        assertThat(taskManager.addCanceledExecution(id)).isTrue();
        assertThat(taskManager.addCanceledExecution(id)).isFalse();

        assertThat(taskManager.checkIfCanceledAndRemove(id)).isTrue();
        assertThat(taskManager.checkIfCanceledAndRemove(id)).isFalse();
    }
}
