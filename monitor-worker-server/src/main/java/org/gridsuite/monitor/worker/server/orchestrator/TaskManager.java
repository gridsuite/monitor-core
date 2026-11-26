/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.orchestrator;

import org.gridsuite.monitor.worker.server.core.exceptions.ProcessCancelledException;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

/**
 * @author Radouane KHOUADRI {@literal <redouane.khouadri_externe at rte-france.com>}
 */
@Component
public class TaskManager {
    private final ConcurrentMap<UUID, ExecutionState> executions = new ConcurrentHashMap<>();

    public void registerExecution(UUID id) {
        if (executions.putIfAbsent(id, new ExecutionState()) != null) {
            throw new IllegalStateException("Execution " + id + " is already registered");
        }
    }

    public void unregisterExecution(UUID id) {
        executions.remove(id);
    }

    public void checkCancellation(UUID id) {
        getExecution(id).checkCancellation();
    }

    public <R> R runComputation(UUID id, Supplier<CompletableFuture<R>> computation) {
        ExecutionState execution = getExecution(id);
        CompletableFuture<R> future;
        synchronized (execution) {
            execution.checkCancellation();
            if (execution.future != null) {
                throw new IllegalStateException("Execution " + id + " already has a running computation");
            }
            future = computation.get();
            execution.future = future;
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
            synchronized (execution) {
                execution.future = null;
            }
        }
    }

    public boolean cancelTask(UUID id) {
        ExecutionState execution = executions.get(id);
        if (execution == null) {
            return false;
        }
        synchronized (execution) {
            execution.canceled = true;
            return execution.future != null && execution.future.cancel(true);
        }
    }

    private ExecutionState getExecution(UUID id) {
        ExecutionState execution = executions.get(id);
        if (execution == null) {
            throw new IllegalStateException("Execution " + id + " is not registered");
        }
        return execution;
    }

    private static final class ExecutionState {
        private boolean canceled;
        private CompletableFuture<?> future;

        private synchronized void checkCancellation() {
            if (canceled) {
                throw new ProcessCancelledException("Execution canceled");
            }
        }
    }
}
