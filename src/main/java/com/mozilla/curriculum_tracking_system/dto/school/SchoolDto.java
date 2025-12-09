package com.mozilla.curriculum_tracking_system.dto.school;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolDto {

    private Long id;
    private String name;
    private String code;
    private Long deanId;

}