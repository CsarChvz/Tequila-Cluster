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

import java.math.BigDecimal;
import java.util.UUID;

/** Tabla origen: agave_field. Predios donde se cultiva agave (FR-06, FR-37). */
@Entity
@Table(name = "agave_field")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgaveField {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "field_code", nullable = false, unique = true, length = 50)
    private String fieldCode;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorized_area_id", nullable = false)
    private AuthorizedProductionArea authorizedArea;

    @Column(name = "address_text")
    private String addressText;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "active", nullable = false)
    private Boolean active;
}
