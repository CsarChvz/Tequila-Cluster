package org.dev.tequilacluster.models.catalogs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dev.tequilacluster.models.shared.ProcessStage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Tabla origen: validation_rule. Reglas configurables de rango/tolerancia por etapa+parámetro (RB-207). */
@Entity
@Table(name = "validation_rule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_code", nullable = false)
    private ProcessStage processStage;

    @Column(name = "parameter_code", nullable = false, length = 60)
    private String parameterCode;

    @Column(name = "display_name", nullable = false, length = 140)
    private String displayName;

    @Column(name = "unit", length = 30)
    private String unit;

    @Column(name = "min_value", precision = 18, scale = 6)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 18, scale = 6)
    private BigDecimal maxValue;

    @Column(name = "allowed_deviation", precision = 18, scale = 6)
    private BigDecimal allowedDeviation;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;
}
