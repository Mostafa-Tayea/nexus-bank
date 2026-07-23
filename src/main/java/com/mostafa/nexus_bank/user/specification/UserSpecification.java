package com.mostafa.nexus_bank.user.specification;

import com.mostafa.nexus_bank.user.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> withFilters(String search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(search)) {
                String lowerSearch = "%" + search.toLowerCase() + "%";
                Predicate firstNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")), lowerSearch);
                Predicate lastNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lastName")), lowerSearch);
                Predicate emailPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), lowerSearch);
                Predicate phonePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("phone")), lowerSearch);
                Predicate nationalIdPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nationalId")), lowerSearch);

                predicates.add(criteriaBuilder.or(
                        firstNamePredicate,
                        lastNamePredicate,
                        emailPredicate,
                        phonePredicate,
                        nationalIdPredicate
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
