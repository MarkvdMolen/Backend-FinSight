package com.finsight.TransactionService.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO voor het samenstellen van dynamische zoekcriteria
 * bij het zoeken/filteren van transacties.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchCriteriaDTO  {

    /**
     * Algemene zoektekst, bijvoorbeeld "supermarkt".
     * Wordt via LIKE in de opgegeven velden gezocht.
     */
    private String searchText;

    /**
     * Lijst van kolomnamen waarin gezocht wordt.
     * Voorbeelden: ["recipient","description","category"].
     */
    private List<String> searchFields;

    /**
     * Exact bedrag filter. Alleen transacties met dit bedrag
     * worden opgehaald als dit niet null is.
     */
    private BigDecimal exactAmount;

    // Indien gewenst kun je later uitbreiden met bv. min/max:
    // private BigDecimal minAmount;
    // private BigDecimal maxAmount;
}
