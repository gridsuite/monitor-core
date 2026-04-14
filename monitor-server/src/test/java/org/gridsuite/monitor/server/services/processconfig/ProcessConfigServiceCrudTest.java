package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.server.dto.processconfig.PersistedProcessConfig;
import org.gridsuite.monitor.server.entities.processconfig.ProcessConfigEntity;
import org.gridsuite.monitor.server.mappers.processconfig.LoadFlowConfigMapper;
import org.gridsuite.monitor.server.mappers.processconfig.SecurityAnalysisConfigMapper;
import org.gridsuite.monitor.server.repositories.ProcessConfigRepository;
import org.gridsuite.monitor.server.testdata.processconfig.ProcessConfigTestDataProvider;
import org.gridsuite.monitor.server.testdata.processconfig.ProcessConfigTestDataProviders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessConfigServiceCrudTest {

    @Mock
    private ProcessConfigRepository processConfigRepository;

    @Spy
    private final SecurityAnalysisConfigMapper securityAnalysisConfigMapper = Mappers.getMapper(SecurityAnalysisConfigMapper.class);

    @Spy
    private final LoadFlowConfigMapper loadFlowConfigMapper = Mappers.getMapper(LoadFlowConfigMapper.class);

    @InjectMocks
    private ProcessConfigService processConfigService;

    static Stream<ProcessConfigTestDataProvider<?, ?>> providerStream() {
        return ProcessConfigTestDataProviders.all().stream();
    }

    @ParameterizedTest
    @MethodSource("providerStream")
    void createProcessConfigTest(ProcessConfigTestDataProvider<ProcessConfig, ProcessConfigEntity> provider) {
        // --- Prepare data and mocks --- //
        UUID expectedProcessConfigId = UUID.randomUUID();
        ProcessConfig dto = provider.createDto();
        when(processConfigRepository.save(any()))
                .thenAnswer(invocation -> {
                    ProcessConfigEntity entity = invocation.getArgument(0);
                    entity.setId(expectedProcessConfigId);
                    return entity;
                });

        // --- Call method under test --- //
        UUID result = processConfigService.createProcessConfig(dto);

        // --- Check results --- //
        assertThat(result).isEqualTo(expectedProcessConfigId);

        // Capture saved entity
        ArgumentCaptor<ProcessConfigEntity> captor = ArgumentCaptor.forClass(ProcessConfigEntity.class);
        verify(processConfigRepository).save(captor.capture());

        ProcessConfigEntity savedEntity = captor.getValue();

        // Common assertions
        assertThat(savedEntity.getId()).isEqualTo(expectedProcessConfigId);

        // Subtype-specific assertions (delegated to provider)
        provider.assertDtoEntityEquivalent(dto, savedEntity);
    }

    @ParameterizedTest
    @MethodSource("providerStream")
    void updateProcessConfigTest(ProcessConfigTestDataProvider<ProcessConfig, ProcessConfigEntity> provider) {
        // --- Prepare data and mocks --- //
        UUID processConfigId = UUID.randomUUID();
        ProcessConfigEntity processConfigEntity = provider.createEntity();

        ProcessConfig newProcessConfig = provider.createDto();

        when(processConfigRepository.findById(processConfigId)).thenReturn(Optional.of(processConfigEntity));

        // --- Call method under test --- //
        Optional<UUID> updatedProcessConfigId = processConfigService.updateProcessConfig(processConfigId, newProcessConfig);

        // --- Check results --- //
        assertThat(updatedProcessConfigId).contains(processConfigId);

        verify(processConfigRepository).findById(processConfigId);

        Optional<PersistedProcessConfig> processConfigUpdated = processConfigService.getProcessConfig(processConfigId);
        // Common assertions
        assertThat(processConfigUpdated).isPresent();

        // Subtype-specific assertions (delegated to provider)
        provider.assertDtoEquals(processConfigUpdated.get().processConfig(), newProcessConfig);
    }

    @ParameterizedTest
    @MethodSource("providerStream")
    void getProcessConfigTest(ProcessConfigTestDataProvider<ProcessConfig, ProcessConfigEntity> provider) {
        // --- Prepare data and mocks --- //
        UUID processConfigId = UUID.randomUUID();
        ProcessConfigEntity processConfigEntity = provider.createEntity();

        when(processConfigRepository.findById(processConfigId)).thenReturn(Optional.of(processConfigEntity));

        // --- Call method under test --- //
        Optional<PersistedProcessConfig> processConfig = processConfigService.getProcessConfig(processConfigId);

        // --- Check results --- //
        verify(processConfigRepository).findById(processConfigId);
        assertThat(processConfig).isPresent();

        // Subtype-specific assertions (delegated to provider)
        provider.assertDtoEntityEquivalent(processConfig.get().processConfig(), processConfigEntity);
    }

    @Test
    void deleteProcessConfigTest() {
        UUID processConfigId = UUID.randomUUID();

        when(processConfigRepository.existsById(processConfigId)).thenReturn(Boolean.TRUE);
        doNothing().when(processConfigRepository).deleteById(processConfigId);

        Optional<UUID> deletedProcessConfigId = processConfigService.deleteProcessConfig(processConfigId);
        assertThat(deletedProcessConfigId).contains(processConfigId);

        verify(processConfigRepository).existsById(processConfigId);
        verify(processConfigRepository).deleteById(processConfigId);
    }

}
