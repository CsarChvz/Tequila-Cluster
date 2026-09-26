package org.dev.tequilacluster.models.shared;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Tabla origen: process_stage. Catálogo de etapas del proceso productivo (NFR-15). */
@Entity
@Table(name = "process_stage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessStage {

    @Id
    @Column(name = "code", length = 30)
    private String code;

    @Column(name = "name", nullable = false, unique = true, length = 80)
    private String name;

    @Column(name = "sort_order", nullable = false, unique = true)
    private Short sortOrder;
}
