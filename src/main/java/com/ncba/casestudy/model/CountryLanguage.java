package com.ncba.casestudy.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "country_languages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryLanguage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iso_code", length = 10)
    private String isoCode;

    @Column(name = "name", length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;
}
