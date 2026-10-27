package org.gridsuite.monitor.worker.server.orchestrator;

import org.gridsuite.monitor.worker.server.core.exceptions.ProcessCancelledException;
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

    @Test
    void runComputationShouldAllowSubsequentComputationInSameExecution() {
        UUID id = UUID.randomUUID();
        taskManager.registerExecution(id);
        String result = taskManager.runComputation(id, () -> CompletableFuture.completedFuture("result"));

        assertThat(result).isEqualTo("result");
        assertThat(taskManager.runComputation(id, () -> CompletableFuture.completedFuture("next"))).isEqualTo("next");
        assertThat(taskManager.cancelTask(id)).isFalse();
    }

    @Test
    void cancelTaskShouldCancelProviderFuture() throws Exception {
        UUID id = UUID.randomUUID();
        taskManager.registerExecution(id);
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
    void cancellationBeforeComputationShouldPreventItFromStarting() {
        UUID id = UUID.randomUUID();
        taskManager.registerExecution(id);
        Supplier<CompletableFuture<String>> computation = mock(Supplier.class);
        taskManager.cancelTask(id);

        assertThatThrownBy(() -> taskManager.runComputation(id, computation))
            .isInstanceOf(ProcessCancelledException.class);
        verifyNoInteractions(computation);
    }

    @Test
    void interruptedWaitShouldCancelProviderFutureAndRestoreInterruptFlag() {
        UUID id = UUID.randomUUID();
        taskManager.registerExecution(id);
        CompletableFuture<String> providerFuture = new CompletableFuture<>();
        Thread.currentThread().interrupt();
        try {
            assertThatThrownBy(() -> taskManager.runComputation(id, () -> providerFuture))
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
        taskManager.registerExecution(id);
        RuntimeException failure = new RuntimeException("Computation failed");

        assertThatThrownBy(() -> taskManager.runComputation(id, () -> CompletableFuture.failedFuture(failure)))
            .isInstanceOf(CompletionException.class)
            .hasCause(failure);
        assertThat(taskManager.cancelTask(id)).isFalse();
    }

    @Test
    void cancellationForUnknownExecutionShouldNotLeavePendingState() {
        UUID id = UUID.randomUUID();
        assertThat(taskManager.cancelTask(id)).isFalse();

        taskManager.registerExecution(id);

        assertThat(taskManager.runComputation(id, () -> CompletableFuture.completedFuture("result"))).isEqualTo("result");
    }

    @Test
    void cancellationBetweenComputationsShouldRemainUntilExecutionEnds() {
        UUID id = UUID.randomUUID();
        taskManager.registerExecution(id);
        taskManager.runComputation(id, () -> CompletableFuture.completedFuture("first"));
        taskManager.cancelTask(id);

        assertThatThrownBy(() -> taskManager.checkCancellation(id)).isInstanceOf(ProcessCancelledException.class);
        assertThatThrownBy(() -> taskManager.checkCancellation(id)).isInstanceOf(ProcessCancelledException.class);
        assertThatThrownBy(() -> taskManager.runComputation(id, () -> CompletableFuture.completedFuture("second")))
            .isInstanceOf(ProcessCancelledException.class);

        taskManager.unregisterExecution(id);
        assertThatThrownBy(() -> taskManager.checkCancellation(id)).isInstanceOf(IllegalStateException.class);
        taskManager.registerExecution(id);
        assertThat(taskManager.runComputation(id, () -> CompletableFuture.completedFuture("new execution"))).isEqualTo("new execution");
    }

    @Test
    void duplicateRegistrationShouldNotReplaceCancellationState() {
        UUID id = UUID.randomUUID();
        taskManager.registerExecution(id);
        taskManager.cancelTask(id);

        assertThatThrownBy(() -> taskManager.registerExecution(id)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> taskManager.checkCancellation(id)).isInstanceOf(ProcessCancelledException.class);
    }

    @Test
    void unregisteredExecutionShouldNotStartComputation() {
        Supplier<CompletableFuture<String>> computation = mock(Supplier.class);

        assertThatThrownBy(() -> taskManager.runComputation(UUID.randomUUID(), computation))
            .isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(computation);
    }
}
