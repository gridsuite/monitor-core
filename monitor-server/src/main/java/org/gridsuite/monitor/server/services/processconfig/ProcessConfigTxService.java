package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.commons.types.processconfig.ProcessConfigFieldComparison;
import org.gridsuite.monitor.commons.types.processexecution.ProcessType;
import org.gridsuite.monitor.server.dto.processconfig.MetadataInfos;
import org.gridsuite.monitor.server.dto.processconfig.PersistedProcessConfig;
import org.gridsuite.monitor.server.dto.processconfig.ProcessConfigComparison;
import org.gridsuite.monitor.server.entities.processconfig.AbstractProcessConfigEntity;
import org.gridsuite.monitor.server.error.MonitorServerException;
import org.gridsuite.monitor.server.mappers.processconfig.ProcessConfigMapper;
import org.gridsuite.monitor.server.repositories.processconfig.ProcessConfigRepository;
import org.gridsuite.monitor.server.services.processconfig.providers.ProcessConfigProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.gridsuite.monitor.server.error.MonitorServerBusinessErrorCode.DIFFERENT_PROCESS_CONFIG_TYPE;

@Service
public class ProcessConfigTxService {
    private final ProcessConfigRepository processConfigRepository;
    private final Map<ProcessType, ProcessConfigProvider<?, ?>> processConfigProviders;

    public ProcessConfigTxService(ProcessConfigRepository processConfigRepository, List<ProcessConfigProvider<?, ?>> listProviders) {
        this.processConfigRepository = processConfigRepository;
        this.processConfigProviders = listProviders.stream().collect(Collectors.toMap(
            ProcessConfigProvider::getProcessType, Function.identity(), (p1, p2) -> {
                throw new IllegalStateException(String.format("Duplicate process config providers for process type: %s", p1.getProcessType()));
            }
        ));
    }

    @Transactional
    public UUID createProcessConfig(ProcessConfig processConfig) {
        AbstractProcessConfigEntity entity = getProvider(processConfig.processType()).getMapper().toEntity(processConfig);
        return processConfigRepository.save(entity).getId();
    }

    @Transactional(readOnly = true)
    public Optional<PersistedProcessConfig> getProcessConfig(UUID processConfigUuid) {
        return processConfigRepository.findById(processConfigUuid)
            .map(this::toPersistedProcessConfig);
    }

    @Transactional(readOnly = true)
    public List<PersistedProcessConfig> getProcessConfigs(ProcessType processType) {
        return getProvider(processType).getRepository().findAll().stream()
            .map(this::toPersistedProcessConfig)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<MetadataInfos> getProcessConfigsMetadata(List<UUID> processConfigUuids) {
        return processConfigRepository.findAllById(processConfigUuids).stream()
            .map(entity -> new MetadataInfos(entity.getId(), entity.getProcessType()))
            .toList();
    }

    @Transactional
    public Optional<UUID> updateProcessConfig(UUID processConfigUuid, ProcessConfig processConfig) {
        return processConfigRepository.findById(processConfigUuid)
            .map(entity -> {
                if (entity.getProcessType() != processConfig.processType()) {
                    throw new IllegalArgumentException("Process config type mismatch : " + entity.getProcessType());
                }
                getProvider(processConfig.processType()).getMapper().updateEntityFromDto(processConfig, entity);
                return processConfigUuid;
            });
    }

    @Transactional
    public Optional<UUID> duplicateProcessConfig(UUID sourceProcessConfigUuid) {
        return processConfigRepository.findById(sourceProcessConfigUuid)
            .map(sourceEntity -> {
                ProcessConfigMapper<ProcessConfig, AbstractProcessConfigEntity> mapper = getProvider(sourceEntity.getProcessType()).getMapper();
                AbstractProcessConfigEntity entity = mapper.toEntity(mapper.toDto(sourceEntity));
                return processConfigRepository.save(entity).getId();
            });
    }

    @Transactional
    public Optional<UUID> deleteProcessConfig(UUID processConfigUuid) {
        if (processConfigRepository.existsById(processConfigUuid)) {
            processConfigRepository.deleteById(processConfigUuid);
            return Optional.of(processConfigUuid);
        } else {
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public Optional<ProcessConfigComparison> compareProcessConfigs(UUID uuid1, UUID uuid2) {
        Optional<AbstractProcessConfigEntity> processConfigEntity1 = processConfigRepository.findById(uuid1);
        Optional<AbstractProcessConfigEntity> processConfigEntity2 = processConfigRepository.findById(uuid2);

        if (processConfigEntity1.isEmpty() || processConfigEntity2.isEmpty()) {
            return Optional.empty();
        }

        if (processConfigEntity1.get().getProcessType() != processConfigEntity2.get().getProcessType()) {
            throw new MonitorServerException(DIFFERENT_PROCESS_CONFIG_TYPE, "Cannot compare different process config types",
                Map.of("processConfigEntity1Type", processConfigEntity1.get().getProcessType(), "processConfigEntity2Type", processConfigEntity2.get().getProcessType()));
        }

        ProcessConfig processConfig1 = toProcessConfig(processConfigEntity1.get());
        ProcessConfig processConfig2 = toProcessConfig(processConfigEntity2.get());

        List<ProcessConfigFieldComparison> differences = processConfig1.compareWith(processConfig2);
        boolean identical = differences.stream().allMatch(ProcessConfigFieldComparison::identical);

        return Optional.of(new ProcessConfigComparison(uuid1, uuid2, identical, differences));
    }

    private PersistedProcessConfig toPersistedProcessConfig(AbstractProcessConfigEntity entity) {
        return new PersistedProcessConfig(entity.getId(), toProcessConfig(entity));
    }

    private ProcessConfig toProcessConfig(AbstractProcessConfigEntity entity) {
        return getProvider(entity.getProcessType()).getMapper().toDto(entity);
    }

    @SuppressWarnings("unchecked")
    private <C extends ProcessConfig, E extends AbstractProcessConfigEntity> ProcessConfigProvider<C, E> getProvider(ProcessType processType) {
        ProcessConfigProvider<?, ?> provider = processConfigProviders.get(processType);
        if (provider == null) {
            throw new IllegalArgumentException("Unsupported process config type: " + processType);
        }
        return (ProcessConfigProvider<C, E>) provider;
    }
}
