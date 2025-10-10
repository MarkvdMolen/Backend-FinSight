package com.finsight.TransactionService.Spec;

import com.finsight.TransactionService.dto.SearchCriteriaDTO;
import com.finsight.TransactionService.Entity.Transaction;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Builder voor JPA Specifications op basis van TransactionSearchCriteria.
 * Hiermee kun je dynamisch WHERE‑clausules samenstellen.
 */
public class TransactionSpecification {

    /**
     * Combineert alle criteria in één Specification<Transaction>.
     *
     * @param criteria de DTO met zoekTekst, welke velden en exact bedrag
     * @return een Specification die je kunt doorgeven aan repository.findAll(...)
     */
    public static Specification<Transaction> byCriteria(SearchCriteriaDTO criteria) {
        return (Root<Transaction> root,
                CriteriaQuery<?> query,
                CriteriaBuilder cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // --- 1) Full‑text LIKE‑zoek op opgegeven kolommen ---
            String text = criteria.getSearchText();
            if (text != null && !text.isBlank()) {
                String pattern = "%" + text.toLowerCase() + "%";
                List<Predicate> orConditions = new ArrayList<>();

                // per kolom: maak cb.like(cb.lower(root.get(...)), pattern)
                if (criteria.getSearchFields().contains("recipient")) {
                    orConditions.add(
                            cb.like(cb.lower(root.get("recipient")), pattern)
                    );
                }
                if (criteria.getSearchFields().contains("description")) {
                    orConditions.add(
                            cb.like(cb.lower(root.get("description")), pattern)
                    );
                }
                if (criteria.getSearchFields().contains("category")) {
                    orConditions.add(
                            cb.like(cb.lower(root.get("category")), pattern)
                    );
                }

                // voeg OR van al deze velden samen
                if (!orConditions.isEmpty()) {
                    predicates.add(cb.or(orConditions.toArray(new Predicate[0])));
                }
            }

            // --- 2) Exact bedrag filter (optional) ---
            if (criteria.getExactAmount() != null) {
                predicates.add(
                        cb.equal(root.get("amount"), criteria.getExactAmount())
                );
            }

            // --- 3) Combineer alles met AND ---
            if (predicates.isEmpty()) {
                // geen filters: haal alles
                return cb.conjunction();
            } else {
                return cb.and(predicates.toArray(new Predicate[0]));
            }
        };
    }

    public static Specification<Transaction> dateBetween(LocalDate start, LocalDate end) {
        return (root, q, cb) -> cb.between(root.get("date"), start, end);
    }

    public static Specification<Transaction> excludeCategories(Collection<String> excluded) {
        return (root, q, cb) -> {
            if (excluded == null || excluded.isEmpty()) return cb.conjunction();
            return cb.not(root.get("category").in(excluded));
        };
    }

    /** amount > 0 */
    public static Specification<Transaction> incomeOnly() {
        return (root, q, cb) -> cb.greaterThan(root.get("amount"), BigDecimal.ZERO);
    }

    /** amount < 0 */
    public static Specification<Transaction> expenseOnly() {
        return (root, q, cb) -> cb.lessThan(root.get("amount"), BigDecimal.ZERO);
    }
}