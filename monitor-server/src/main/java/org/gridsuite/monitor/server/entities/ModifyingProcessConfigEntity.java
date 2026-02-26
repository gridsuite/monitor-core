package org.gridsuite.monitor.server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;


/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@Entity
@Table(name = "modifying_process_config")
@DiscriminatorValue("MODIFYING_PROCESS_CONFIG")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ModifyingProcessConfigEntity extends ProcessConfigEntity {
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "modifying_process_config_modifications",
                     joinColumns = @JoinColumn(name = "modifying_process_config_id"),
                     foreignKey = @ForeignKey(name = "AbstractModifyingProcessConfigEntity_modificationUuids_fk1"))
    @Column(name = "modification_uuid")
    @OrderColumn(name = "pos_modifications")
    private List<UUID> modificationUuids;
}
