package com.ncba.casestudy.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "countries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iso_code", unique = true, nullable = false, length = 10)
    private String isoCode;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "capital_city", length = 150)
    private String capitalCity;

    @Column(name = "phone_code", length = 20)
    private String phoneCode;

    @Column(name = "continent_code", length = 10)
    private String continentCode;

    @Column(name = "currency_iso_code", length = 10)
    private String currencyIsoCode;

    @Column(name = "country_flag", length = 500)
    private String countryFlag;

    @OneToMany(
        mappedBy = "country",
        cascade = CascadeType.ALL,
        fetch = FetchType.EAGER,
        orphanRemoval = true
    )
    @Builder.Default
    private List<CountryLanguage> languages = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
