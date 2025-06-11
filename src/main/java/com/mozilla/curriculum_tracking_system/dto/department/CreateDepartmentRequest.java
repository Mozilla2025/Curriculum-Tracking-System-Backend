package com.mozilla.curriculum_tracking_system.dto.department;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDepartmentRequest {

    @NotBlank(message = "Department name is required")
    @Size(max = 100, message = "Department name must not exceed 100 characters")
    private String name;

    @Size(max = 10, message = "Department code must not exceed 10 characters")
    private String code;

    @NotNull(message = "School ID is required")
    private Long schoolId;

    private Long headId;
}
