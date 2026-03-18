package com.ncba.casestudy.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CountryResponse {

    private Long id;
    private String isoCode;
    private String name;
    private String capitalCity;
    private String phoneCode;
    private String continentCode;
    private String currencyIsoCode;
    private String countryFlag;
    private List<LanguageDto> languages;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
