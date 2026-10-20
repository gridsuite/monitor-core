/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.monitor.worker.server.orchestrator;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * @author Radouane KHOUADRI {@literal <redouane.khouadri_externe at rte-france.com>}
 */
@Component
public class TaskManager {
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private final ConcurrentMap<UUID, Future<?>> tasks = new ConcurrentHashMap<>();
    private final Set<UUID> canceledExecutions = ConcurrentHashMap.newKeySet();

    Future<Void> submit(UUID id, Runnable work) {
        FutureTask<Void> future = new FutureTask<>(() -> {
            work.run();
            return null;
        }) {
            @Override
            protected void done() {
                tasks.remove(id);
            }
        };

        tasks.put(id, future);
        executor.execute(future);
        return future;
    }

    public boolean cancelTask(UUID id) {
        Future task = tasks.get(id);
        return task != null && task.cancel(true);
    }

    void removeTask(UUID id) {
        tasks.remove(id);
    }

    public boolean addCanceledExecution(UUID id) {
        return canceledExecutions.add(id);
    }

    public boolean checkIfCanceledAndRemove(UUID id) {
        return canceledExecutions.remove(id);
    }

    @PreDestroy
    void shutdown() {
        executor.shutdownNow();
    }
}
