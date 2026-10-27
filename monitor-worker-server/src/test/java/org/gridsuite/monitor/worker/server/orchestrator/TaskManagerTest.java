package org.gridsuite.monitor.worker.server.orchestrator;

import org.gridsuite.monitor.worker.server.core.exceptions.ProcessCancelledException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TaskManagerTest {

    private final TaskManager taskManager = new TaskManager();

    @AfterEach
    void tearDown() {
        taskManager.shutdown();
    }

    @Test
    void runComputationShouldReturnResultAndRemoveCompletedTask() {
        UUID id = UUID.randomUUID();
        String result = taskManager.runComputation(id, () -> CompletableFuture.completedFuture("result"));

        assertThat(result).isEqualTo("result");
        assertThat(taskManager.cancelTask(id)).isFalse();
    }

    @Test
    void cancelTaskShouldCancelProviderFuture() throws Exception {
        UUID id = UUID.randomUUID();
        CountDownLatch computationStarted = new CountDownLatch(1);
        CompletableFuture<String> providerFuture = new CompletableFuture<>();

        try (var executor = Executors.newSingleThreadExecutor()) {
            var work = executor.submit(() -> assertThatThrownBy(() ->
                taskManager.runComputation(id, () -> {
                    computationStarted.countDown();
                    return providerFuture;
                })).isInstanceOf(CancellationException.class));
            try {
                assertThat(computationStarted.await(5, TimeUnit.SECONDS)).isTrue();
                assertThat(taskManager.cancelTask(id)).isTrue();
                assertThat(providerFuture.isCancelled()).isTrue();
                work.get(5, TimeUnit.SECONDS);
                assertThat(taskManager.cancelTask(id)).isFalse();
            } finally {
                providerFuture.cancel(true);
            }
        }
    }

    @Test
    void cancellationBeforeRegistrationShouldPreventComputationFromStarting() {
        UUID id = UUID.randomUUID();
        Supplier<CompletableFuture<String>> computation = mock(Supplier.class);
        taskManager.cancelTask(id);

        assertThatThrownBy(() -> taskManager.runComputation(id, computation))
            .isInstanceOf(CancellationException.class);
        verifyNoInteractions(computation);
    }

    @Test
    void interruptedWaitShouldCancelProviderFutureAndRestoreInterruptFlag() {
        CompletableFuture<String> providerFuture = new CompletableFuture<>();
        Thread.currentThread().interrupt();
        try {
            assertThatThrownBy(() -> taskManager.runComputation(UUID.randomUUID(), () -> providerFuture))
                .isInstanceOf(ProcessCancelledException.class);
            assertThat(providerFuture.isCancelled()).isTrue();
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void failedComputationShouldPropagateFailureAndRemoveTask() {
        UUID id = UUID.randomUUID();
        RuntimeException failure = new RuntimeException("Computation failed");

        assertThatThrownBy(() -> taskManager.runComputation(id, () -> CompletableFuture.failedFuture(failure)))
            .isInstanceOf(CompletionException.class)
            .hasCause(failure);
        assertThat(taskManager.cancelTask(id)).isFalse();
    }

    @Test
    void cancelTaskShouldReturnFalseWhenTaskDoesNotExist() {
        assertThat(taskManager.cancelTask(UUID.randomUUID())).isFalse();
    }

    @Test
    void canceledExecutionMarkerShouldBeAddedAndConsumedOnce() {
        UUID id = UUID.randomUUID();

        taskManager.cancelTask(id);
        taskManager.cancelTask(id);

        assertThat(taskManager.checkIfCanceledAndRemove(id)).isTrue();
        assertThat(taskManager.checkIfCanceledAndRemove(id)).isFalse();
    }
}
