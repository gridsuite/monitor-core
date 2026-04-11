package org.gridsuite.monitor.server.services.processconfig;

import org.gridsuite.monitor.commons.types.processconfig.ProcessConfig;
import org.gridsuite.monitor.server.testdata.processconfig.ProcessConfigProvider;
import org.gridsuite.monitor.server.testdata.processconfig.ProcessConfigProviders;
import org.gridsuite.monitor.server.entities.processconfig.ProcessConfigEntity;
import org.gridsuite.monitor.server.mappers.processconfig.LoadFlowConfigMapper;
import org.gridsuite.monitor.server.mappers.processconfig.SecurityAnalysisConfigMapper;
import org.gridsuite.monitor.server.repositories.ProcessConfigRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessConfigServiceCrudTest {

    @Mock
    private ProcessConfigRepository processConfigRepository;

    @Spy
    private LoadFlowConfigMapper loadFlowMapper = Mappers.getMapper(LoadFlowConfigMapper.class);

    @Spy
    private SecurityAnalysisConfigMapper securityMapper = Mappers.getMapper(SecurityAnalysisConfigMapper.class);

    @InjectMocks
    private ProcessConfigService processConfigService;

    static Stream<ProcessConfigProvider<?, ?>> providerStream() {
        return ProcessConfigProviders.all().stream();
    }

    @ParameterizedTest
    @MethodSource("providerStream")
    void createProcessConfigTest(ProcessConfigProvider provider) {
        // --- GIVEN ---
        UUID expectedProcessConfigId = UUID.randomUUID();
        ProcessConfig dto = provider.createDto();
        when(processConfigRepository.save(any()))
                .thenAnswer(invocation -> {
                    ProcessConfigEntity entity = invocation.getArgument(0);
                    entity.setId(expectedProcessConfigId);
                    return entity;
                });

        // --- WHEN ---
        UUID result = processConfigService.createProcessConfig(dto);

        // --- THEN ---
        assertThat(result).isEqualTo(expectedProcessConfigId);

        // Capture saved entity
        ArgumentCaptor<ProcessConfigEntity> captor = ArgumentCaptor.forClass(ProcessConfigEntity.class);
        verify(processConfigRepository).save(captor.capture());

        ProcessConfigEntity savedEntity = captor.getValue();

        // ✅ Common assertions
        assertThat(savedEntity.getId()).isEqualTo(expectedProcessConfigId);
        assertThat(savedEntity.getProcessType()).isEqualTo(dto.processType());
        assertThat(savedEntity.getModificationUuids())
                .isEqualTo(dto.modificationUuids());

        // Subtype-specific assertions (delegated to provider)
        provider.assertDtoEntityEquivalent(dto, savedEntity);
    }
}
