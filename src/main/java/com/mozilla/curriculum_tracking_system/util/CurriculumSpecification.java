package com.mozilla.curriculum_tracking_system.util;

import com.mozilla.curriculum_tracking_system.dto.curriculum.CurriculumSearchCriteria;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CurriculumSpecification {

    public static Specification<Curriculum> withCriteria(CurriculumSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(criteria.getName())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + criteria.getName().toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(criteria.getCode())) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("code")),
                        "%" + criteria.getCode().toLowerCase() + "%"
                ));
            }

            if (criteria.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
            }

            if (criteria.getSchoolId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("school").get("id"), criteria.getSchoolId()));
            }

            if (criteria.getDepartmentId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("department").get("id"), criteria.getDepartmentId()));
            }

            if (criteria.getAcademicLevelId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("academicLevel").get("id"), criteria.getAcademicLevelId()));
            }

            if (criteria.getCreatedBy() != null) {
                predicates.add(criteriaBuilder.equal(root.get("createdBy"), criteria.getCreatedBy()));
            }

            if (criteria.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), criteria.getIsActive()));
            }

            root.fetch("school");
            root.fetch("department");
            root.fetch("academicLevel");

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Component
    public static class JwtUtil {

        @Value("${jwt.secret}")
        private String jwtSecret;

        @Value("${jwt.expiration}")
        private int jwtExprirationMs;

        @Value("${jwt.refresh-exprirationMs}")
        private int jwtRefreshExprirations;

        private SecretKey getSigningKey(){
            return Keys.hmacShaKeyFor(jwtSecret.getBytes());
        }

        public String generateJwtToken(Authentication authentication){
            UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

            return String.valueOf(Jwts.builder()
                    .subject(userPrincipal.getUsername())
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + 60 * 60 *30)));

        }
    }
}
