package com.jobspring.auth.repository.spec;

import com.jobspring.auth.account.Account;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecs {

    private UserSpecs() {
    }

    public static Specification<Account> fuzzySearch(String raw) {
        return (root, query, cb) -> {
            String escaped = escapeLike(raw.toLowerCase());
            String pattern = "%" + escaped + "%";

            var emailLike = cb.like(cb.lower(root.get("email")), pattern, '\\');
            var nameLike = cb.like(cb.lower(root.get("fullName")), pattern, '\\');


            Predicate idPredicate;
            try {
                long idVal = Long.parseLong(raw);
                var idEq = cb.equal(root.get("id"), idVal);

                var idLike = cb.like(root.get("id").as(String.class), pattern, '\\');
                idPredicate = cb.or(idEq, idLike);
            } catch (NumberFormatException e) {

                idPredicate = cb.like(root.get("id").as(String.class), pattern, '\\');
            }

            return cb.or(emailLike, nameLike, idPredicate);
        };
    }


    private static String escapeLike(String s) {
        return s
                .replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("%", "\\%");
    }
}
