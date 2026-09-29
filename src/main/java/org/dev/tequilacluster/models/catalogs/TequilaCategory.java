package org.dev.tequilacluster.models.catalogs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/** Tabla origen: tequila_category. Clasificación comercial y maduración mínima (RB-205, RB-206). */
@Entity
@Table(name = "tequila_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TequilaCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 40)
    private String code;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "minimum_maturation_days", nullable = false)
    private Integer minimumMaturationDays;

    @Column(name = "active", nullable = false)
    private Boolean active;
}
