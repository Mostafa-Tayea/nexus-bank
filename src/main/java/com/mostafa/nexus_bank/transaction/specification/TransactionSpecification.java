package com.mostafa.nexus_bank.transaction.specification;

import com.mostafa.nexus_bank.common.enums.TransactionStatus;
import com.mostafa.nexus_bank.common.enums.TransactionType;
import com.mostafa.nexus_bank.transaction.entity.Transaction;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionSpecification {

    public static Specification<Transaction> withFilters(
            String search,
            TransactionType transactionType,
            TransactionStatus status,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            UUID accountId
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(search)) {
                String lowerSearch = "%" + search.toLowerCase() + "%";
                Predicate refPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("referenceNumber")), lowerSearch);
                Predicate descPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), lowerSearch);
                predicates.add(criteriaBuilder.or(refPredicate, descPredicate));
            }

            if (transactionType != null) {
                predicates.add(criteriaBuilder.equal(root.get("transactionType"), transactionType));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (minAmount != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), minAmount));
            }

            if (maxAmount != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("amount"), maxAmount));
            }

            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            }

            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDate));
            }

            if (accountId != null) {
                Predicate senderPredicate = criteriaBuilder.equal(root.get("senderAccount").get("id"), accountId);
                Predicate receiverPredicate = criteriaBuilder.equal(root.get("receiverAccount").get("id"), accountId);
                predicates.add(criteriaBuilder.or(senderPredicate, receiverPredicate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
