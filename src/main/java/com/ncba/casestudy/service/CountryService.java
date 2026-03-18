package com.ncba.casestudy.service;

import com.ncba.casestudy.dto.*;
import com.ncba.casestudy.exception.CountryNotFoundException;
import com.ncba.casestudy.model.Country;
import com.ncba.casestudy.model.CountryLanguage;
import com.ncba.casestudy.repository.CountryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class CountryService {

    private final CountryRepository countryRepository;
    private final SoapClientService soapClientService;

    public CountryService(CountryRepository countryRepository,
                          SoapClientService soapClientService) {
        this.countryRepository = countryRepository;
        this.soapClientService = soapClientService;
    }

    /**
     * Orchestrates the full flow:
     * 1. Convert country name to sentence case.
     * 2. Fetch ISO code from SOAP.
     * 3. Fetch full country info from SOAP using ISO code.
     * 4. Persist (insert or update) the country.
     */
    public CountryResponse fetchAndPersistCountry(String rawName) {
        String sentenceCaseName = toSentenceCase(rawName);
        log.info("Processing country lookup for '{}'", sentenceCaseName);

        // Step 1 – get ISO code
        String isoCode = soapClientService.getCountryIsoCode(sentenceCaseName);
        log.info("ISO code for '{}' is '{}'", sentenceCaseName, isoCode);

        // Step 2 – get full info
        FullCountryInfoDto fullInfo = soapClientService.getFullCountryInfo(isoCode);
        log.info("Received full country info for ISO code '{}'", isoCode);

        // Step 3 – upsert
        Country country = countryRepository.findByIsoCode(isoCode)
                .orElse(new Country());

        applyFullInfo(country, fullInfo);

        Country saved = countryRepository.save(country);
        log.info("Persisted country '{}' (id={})", saved.getName(), saved.getId());

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CountryResponse> getAllCountries() {
        log.info("Fetching all countries");
        return countryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CountryResponse getCountryById(Long id) {
        log.info("Fetching country by id={}", id);
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new CountryNotFoundException("Country not found with id: " + id));
        return mapToResponse(country);
    }

    public CountryResponse updateCountry(Long id, UpdateCountryRequest request) {
        log.info("Updating country id={}", id);
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new CountryNotFoundException("Country not found with id: " + id));

        if (request.getName() != null)          country.setName(request.getName());
        if (request.getCapitalCity() != null)   country.setCapitalCity(request.getCapitalCity());
        if (request.getPhoneCode() != null)     country.setPhoneCode(request.getPhoneCode());
        if (request.getContinentCode() != null) country.setContinentCode(request.getContinentCode());
        if (request.getCurrencyIsoCode() != null) country.setCurrencyIsoCode(request.getCurrencyIsoCode());
        if (request.getCountryFlag() != null)   country.setCountryFlag(request.getCountryFlag());

        Country updated = countryRepository.save(country);
        log.info("Updated country id={}", updated.getId());
        return mapToResponse(updated);
    }

    public void deleteCountry(Long id) {
        log.info("Deleting country id={}", id);
        if (!countryRepository.existsById(id)) {
            throw new CountryNotFoundException("Country not found with id: " + id);
        }
        countryRepository.deleteById(id);
        log.info("Deleted country id={}", id);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void applyFullInfo(Country country, FullCountryInfoDto dto) {
        country.setIsoCode(dto.getIsoCode());
        country.setName(dto.getName());
        country.setCapitalCity(dto.getCapitalCity());
        country.setPhoneCode(dto.getPhoneCode());
        country.setContinentCode(dto.getContinentCode());
        country.setCurrencyIsoCode(dto.getCurrencyIsoCode());
        country.setCountryFlag(dto.getCountryFlag());

        country.getLanguages().clear();
        if (dto.getLanguages() != null) {
            for (LanguageDto langDto : dto.getLanguages()) {
                country.getLanguages().add(
                        CountryLanguage.builder()
                                .isoCode(langDto.getIsoCode())
                                .name(langDto.getName())
                                .country(country)
                                .build()
                );
            }
        }
    }

    private CountryResponse mapToResponse(Country country) {
        List<LanguageDto> languages = country.getLanguages() != null
                ? country.getLanguages().stream()
                        .map(l -> new LanguageDto(l.getIsoCode(), l.getName()))
                        .collect(Collectors.toList())
                : new ArrayList<>();

        return CountryResponse.builder()
                .id(country.getId())
                .isoCode(country.getIsoCode())
                .name(country.getName())
                .capitalCity(country.getCapitalCity())
                .phoneCode(country.getPhoneCode())
                .continentCode(country.getContinentCode())
                .currencyIsoCode(country.getCurrencyIsoCode())
                .countryFlag(country.getCountryFlag())
                .languages(languages)
                .createdAt(country.getCreatedAt())
                .updatedAt(country.getUpdatedAt())
                .build();
    }

    /**
     * Converts input to sentence case: first character upper-cased, rest lower-cased.
     * e.g. "KENYA" -> "Kenya", "kenya" -> "Kenya"
     */
    private String toSentenceCase(String input) {
        if (input == null || input.isBlank()) return input;
        String trimmed = input.trim().toLowerCase();
        return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
    }
}
