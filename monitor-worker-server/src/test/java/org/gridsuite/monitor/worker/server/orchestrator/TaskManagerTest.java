package org.gridsuite.monitor.worker.server.orchestrator;

import org.gridsuite.monitor.worker.server.core.exceptions.ProcessCancelledException;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskManagerTest {

    private final TaskManager taskManager = new TaskManager();

    @Test
    void cancelBeforeComputationPreventsItFromStarting() {
        UUID id = UUID.randomUUID();
        taskManager.registerExecution(id);
        taskManager.cancelTask(id);
        AtomicBoolean computationStarted = new AtomicBoolean();

        assertThatThrownBy(() -> taskManager.runComputation(id, () -> {
            computationStarted.set(true);
            return CompletableFuture.completedFuture(1);
        })).isInstanceOf(ProcessCancelledException.class);

        assertThat(computationStarted).isFalse();
    }

    @Test
    void cancelDuringComputationCancelsItsFuture() throws Exception {
        UUID id = UUID.randomUUID();
        taskManager.registerExecution(id);
        CountDownLatch supplierEntered = new CountDownLatch(1);
        CountDownLatch releaseSupplier = new CountDownLatch(1);
        CompletableFuture<Integer> computation = new CompletableFuture<>();

        CompletableFuture<Boolean> runner = CompletableFuture.supplyAsync(() -> {
            try {
                taskManager.runComputation(id, () -> {
                    supplierEntered.countDown();
                    try {
                        releaseSupplier.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new CompletionException(e);
                    }
                    return computation;
                });
                return false;
            } catch (CancellationException _) {
                return true;
            }
        });

        try {
            assertThat(supplierEntered.await(1, TimeUnit.SECONDS)).isTrue();
            releaseSupplier.countDown();
            assertThat(taskManager.cancelTask(id)).isTrue();
            assertThat(computation.isCancelled()).isTrue();
            assertThat(runner.get(1, TimeUnit.SECONDS)).isTrue();
        } finally {
            releaseSupplier.countDown();
        }
    }

    @Test
    void cancelUnknownExecutionReturnsFalse() {
        assertThat(taskManager.cancelTask(UUID.randomUUID())).isFalse();
    }
}
