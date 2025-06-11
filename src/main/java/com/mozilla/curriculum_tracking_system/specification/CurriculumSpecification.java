package com.mozilla.curriculum_tracking_system.specification;

import com.mozilla.curriculum_tracking_system.dto.curriculum.CurriculumSearchCriteria;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
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
}
