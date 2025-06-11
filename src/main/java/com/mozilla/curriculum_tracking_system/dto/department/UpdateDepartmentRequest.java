package com.mozilla.curriculum_tracking_system.dto.department;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDepartmentRequest {

    @Size(max = 100, message = "Department name must not exceed 100 characters")
    private String name;

    @Size(max = 10, message = "Department code must not exceed 10 characters")
    private String code;

    private Long headId;
}
