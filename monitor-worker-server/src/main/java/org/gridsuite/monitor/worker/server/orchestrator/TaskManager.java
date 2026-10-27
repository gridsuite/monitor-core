/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.orchestrator;

import jakarta.annotation.PreDestroy;
import org.gridsuite.monitor.worker.server.core.exceptions.ProcessCancelledException;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * @author Radouane KHOUADRI {@literal <redouane.khouadri_externe at rte-france.com>}
 */
@Component
public class TaskManager {
    private final Object lockRunAndCancel = new Object();
    private final ConcurrentMap<UUID, CompletableFuture<?>> tasks = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Long> canceledExecutions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

    public TaskManager() {
        cleanupExecutor.scheduleAtFixedRate(this::cleanupCanceledExecutions, 1, 1, TimeUnit.HOURS);
    }

    public <R> R runComputation(UUID id, Supplier<CompletableFuture<R>> computation) {
        CompletableFuture<R> future;
        synchronized (lockRunAndCancel) {
            if (checkIfCanceledAndRemove(id)) {
                throw new CancellationException("Execution canceled");
            }
            future = computation.get();
            tasks.put(id, future);
        }
        try {
            return future.get();
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new ProcessCancelledException("Execution interrupted", e);
        } catch (ExecutionException e) {
            throw new CompletionException(e.getCause());
        } finally {
            tasks.remove(id, future);
        }
    }

    public boolean cancelTask(UUID id) {
        synchronized (lockRunAndCancel) {
            canceledExecutions.put(id, System.currentTimeMillis());
            CompletableFuture<?> task = tasks.get(id);
            return task != null && task.cancel(true);
        }
    }

    public boolean checkIfCanceledAndRemove(UUID id) {
        return canceledExecutions.remove(id) != null;
    }

    private void cleanupCanceledExecutions() {
        long oneHourAgo = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1);
        canceledExecutions.entrySet().removeIf(entry -> entry.getValue() < oneHourAgo);
    }

    @PreDestroy
    public void shutdown() {
        cleanupExecutor.shutdownNow();
    }
}
