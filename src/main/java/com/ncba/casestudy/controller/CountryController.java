package com.ncba.casestudy.controller;

import com.ncba.casestudy.dto.*;
import com.ncba.casestudy.service.CountryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/countries")
@Slf4j
public class CountryController {

    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    /**
     * POST /api/v1/countries
     * Accepts a country name, calls SOAP services to resolve full info, and persists it.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CountryResponse>> fetchAndSaveCountry(
            @Valid @RequestBody CountryRequest request) {
        log.info("POST /api/v1/countries – name='{}'", request.getName());
        CountryResponse response = countryService.fetchAndPersistCountry(request.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Country information retrieved and saved successfully", response));
    }

    /**
     * GET /api/v1/countries
     * Returns all persisted country records.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CountryResponse>>> getAllCountries() {
        log.info("GET /api/v1/countries");
        List<CountryResponse> countries = countryService.getAllCountries();
        return ResponseEntity.ok(
                ApiResponse.success("Countries retrieved successfully", countries));
    }

    /**
     * GET /api/v1/countries/{id}
     * Returns a single country record by its database ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CountryResponse>> getCountryById(@PathVariable Long id) {
        log.info("GET /api/v1/countries/{}", id);
        CountryResponse country = countryService.getCountryById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Country retrieved successfully", country));
    }

    /**
     * PUT /api/v1/countries/{id}
     * Updates editable fields of an existing country record.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CountryResponse>> updateCountry(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCountryRequest request) {
        log.info("PUT /api/v1/countries/{}", id);
        CountryResponse updated = countryService.updateCountry(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Country updated successfully", updated));
    }

    /**
     * DELETE /api/v1/countries/{id}
     * Deletes a country record by its database ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCountry(@PathVariable Long id) {
        log.info("DELETE /api/v1/countries/{}", id);
        countryService.deleteCountry(id);
        return ResponseEntity.ok(
                ApiResponse.success("Country deleted successfully", null));
    }
}
