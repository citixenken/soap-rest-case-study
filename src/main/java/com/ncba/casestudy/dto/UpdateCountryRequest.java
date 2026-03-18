package com.ncba.casestudy.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCountryRequest {

    @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
    private String name;

    @Size(max = 150, message = "Capital city must not exceed 150 characters")
    private String capitalCity;

    @Size(max = 20, message = "Phone code must not exceed 20 characters")
    private String phoneCode;

    @Size(max = 10, message = "Continent code must not exceed 10 characters")
    private String continentCode;

    @Size(max = 10, message = "Currency ISO code must not exceed 10 characters")
    private String currencyIsoCode;

    private String countryFlag;
}
