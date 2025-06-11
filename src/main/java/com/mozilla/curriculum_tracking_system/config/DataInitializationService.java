package com.mozilla.curriculum_tracking_system.config;

import com.mozilla.curriculum_tracking_system.constants.RoleConstants;
import com.mozilla.curriculum_tracking_system.model.academic.AcademicLevel;
import com.mozilla.curriculum_tracking_system.model.department.Department;
import com.mozilla.curriculum_tracking_system.model.roles.Role;
import com.mozilla.curriculum_tracking_system.model.school.School;
import com.mozilla.curriculum_tracking_system.model.user.User;
import com.mozilla.curriculum_tracking_system.repository.academic.AcademicLevelRepository;
import com.mozilla.curriculum_tracking_system.repository.department.DepartmentRepository;
import com.mozilla.curriculum_tracking_system.repository.roles.RoleRepository;
import com.mozilla.curriculum_tracking_system.repository.school.SchoolRepository;
import com.mozilla.curriculum_tracking_system.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AcademicLevelRepository academicLevelRepository;
    private final SchoolRepository schoolRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String defaultAdminUsername;

    @Value("${app.admin.email:admin@curriculum.system}")
    private String defaultAdminEmail;

    @Value("${app.admin.password:Admin@123}")
    private String defaultAdminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting system data initialization...");

        initializeRoles();
        initializeDefaultAdmin();
        initializeAcademicLevels();
        initializeSchoolsAndDepartments();

        log.info("System data initialization completed successfully!");
    }

    private void initializeRoles() {
        log.info("Initializing system roles...");

        createRoleIfNotExists(RoleConstants.ADMIN, "System Administrator with full access");
        createRoleIfNotExists(RoleConstants.VICE_CHANCELLOR, "Vice Chancellor of the institution");
        createRoleIfNotExists(RoleConstants.DEAN, "Dean of a faculty or school");
        createRoleIfNotExists(RoleConstants.HEAD_OF_DEPARTMENT, "Head of a department");
        createRoleIfNotExists(RoleConstants.ASSISTANT_ROLE, "Assistant roles to dean");

        log.info("Role initialization completed");
    }

    private void createRoleIfNotExists(String roleName, String description) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = Role.builder()
                    .name(roleName)
                    .description(description)
                    .build();
            roleRepository.save(role);
            log.info("Created role: {}", roleName);
        } else {
            log.debug("Role already exists: {}", roleName);
        }
    }

    private void initializeDefaultAdmin() {
        log.info("Checking for default admin user...");

        long adminCount = userRepository.countUsersWithRole(RoleConstants.ADMIN);

        if (adminCount == 0) {
            log.info("No admin user found. Creating default admin user...");

            Role adminRole = roleRepository.findByName(RoleConstants.ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            User adminUser = User.builder()
                    .username(defaultAdminUsername)
                    .email(defaultAdminEmail)
                    .password(passwordEncoder.encode(defaultAdminPassword))
                    .firstName("System")
                    .lastName("Administrator")
                    .phoneNumber("")
                    .isEnabled(true)
                    .isAccountNonExpired(true)
                    .isAccountNonLocked(true)
                    .isCredentialsNonExpired(true)
                    .roles(Set.of(adminRole))
                    .build();
            userRepository.save(adminUser);

            log.info("Default admin user created successfully");
            log.info("Username: {}", defaultAdminUsername);
            log.info("Email: {}", defaultAdminEmail);
            log.warn("Please change the default admin password after first login!");
        } else {
            log.info("Admin user(s) already exist. Skipping default admin creation.");
        }
    }

    private void initializeAcademicLevels() {
        log.info("Initializing academic levels...");

        List<String> academicLevels = Arrays.asList(
                "Undergraduate",
                "Masters",
                "PhD"
        );

        for (String levelName : academicLevels) {
            createAcademicLevelIfNotExists(levelName);
        }

        log.info("Academic levels initialization completed");
    }

    private void createAcademicLevelIfNotExists(String levelName) {
        if (academicLevelRepository.findByName(levelName).isEmpty()) {
            AcademicLevel academicLevel = AcademicLevel.builder()
                    .name(levelName)
                    .build();
            academicLevelRepository.save(academicLevel);
            log.info("Created academic level: {}", levelName);
        } else {
            log.debug("Academic level already exists: {}", levelName);
        }
    }

    private void initializeSchoolsAndDepartments() {
        log.info("Initializing schools and departments...");

        Map<SchoolData, List<String>> schoolsAndDepartments = new HashMap<>();

        schoolsAndDepartments.put(
                new SchoolData("School of Engineering and Technology", "SET", "dean.set@university.edu"),
                Arrays.asList(
                        "Computer Science and Information Technology",
                        "Electrical and Electronic Engineering",
                        "Mechanical Engineering",
                        "Civil Engineering",
                        "Chemical Engineering",
                        "Biomedical Engineering"
                )
        );

        schoolsAndDepartments.put(
                new SchoolData("School of Business and Economics", "SBE", "dean.sbe@university.edu"),
                Arrays.asList(
                        "Business Administration",
                        "Economics",
                        "Accounting and Finance",
                        "Marketing",
                        "Human Resource Management",
                        "Supply Chain Management"
                )
        );

        schoolsAndDepartments.put(
                new SchoolData("School of Health Sciences", "SHS", "dean.shs@university.edu"),
                Arrays.asList(
                        "Medicine",
                        "Nursing",
                        "Pharmacy",
                        "Public Health",
                        "Dentistry",
                        "Physiotherapy"
                )
        );

        schoolsAndDepartments.put(
                new SchoolData("School of Natural Sciences", "SNS", "dean.sns@university.edu"),
                Arrays.asList(
                        "Mathematics and Statistics",
                        "Physics",
                        "Chemistry",
                        "Biology",
                        "Environmental Science",
                        "Geology"
                )
        );

        schoolsAndDepartments.put(
                new SchoolData("School of Social Sciences and Humanities", "SSH", "dean.ssh@university.edu"),
                Arrays.asList(
                        "Psychology",
                        "Sociology",
                        "Political Science",
                        "History",
                        "Literature and Languages",
                        "Philosophy"
                )
        );

        schoolsAndDepartments.put(
                new SchoolData("School of Education", "SE", "dean.se@university.edu"),
                Arrays.asList(
                        "Early Childhood Education",
                        "Primary Education",
                        "Secondary Education",
                        "Special Education",
                        "Educational Leadership",
                        "Curriculum and Instruction"
                )
        );

        schoolsAndDepartments.put(
                new SchoolData("School of Agriculture and Environmental Sciences", "SAES", "dean.saes@university.edu"),
                Arrays.asList(
                        "Crop Science",
                        "Animal Science",
                        "Agricultural Economics",
                        "Food Science and Technology",
                        "Forestry",
                        "Agricultural Engineering"
                )
        );

        schoolsAndDepartments.put(
                new SchoolData("School of Law", "SL", "dean.sl@university.edu"),
                Arrays.asList(
                        "Private Law",
                        "Public Law",
                        "Commercial Law",
                        "International Law",
                        "Criminal Law",
                        "Constitutional Law"
                )
        );

        for (Map.Entry<SchoolData, List<String>> entry : schoolsAndDepartments.entrySet()) {
            School school = createSchoolIfNotExists(entry.getKey());
            createDepartmentsForSchool(school, entry.getValue());
        }

        log.info("Schools and departments initialization completed");
    }

    private School createSchoolIfNotExists(SchoolData schoolData) {
        if (!schoolRepository.existsByName(schoolData.name)) {
            School school = School.builder()
                    .name(schoolData.name)
                    .code(schoolData.code)
                    .email(schoolData.email)
                    .isActive(true)
                    .build();
            school = schoolRepository.save(school);
            log.info("Created school: {} ({})", schoolData.name, schoolData.code);
            return school;
        } else {
            log.debug("School already exists: {}", schoolData.name);
            return schoolRepository.findAll().stream()
                    .filter(s -> s.getName().equals(schoolData.name))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("School not found: " + schoolData.name));
        }
    }

    private void createDepartmentsForSchool(School school, List<String> departmentNames) {
        for (String departmentName : departmentNames) {
            createDepartmentIfNotExists(school, departmentName);
        }
    }

    private void createDepartmentIfNotExists(School school, String departmentName) {
        if (!departmentRepository.existsByNameAndSchoolId(departmentName, school.getId())) {
            String baseDepartmentCode = generateDepartmentCode(departmentName);
            String uniqueCode = ensureUniqueCodeForSchool(school, baseDepartmentCode);

            Department department = Department.builder()
                    .name(departmentName)
                    .code(uniqueCode)
                    .school(school)
                    .build();
            departmentRepository.save(department);
            log.info("Created department: {} ({}) in school: {}",
                    departmentName, uniqueCode, school.getName());
        } else {
            log.debug("Department already exists: {} in school: {}",
                    departmentName, school.getName());
        }
    }

    private String generateDepartmentCode(String departmentName) {
        String[] words = departmentName.split("\\s+");
        StringBuilder code = new StringBuilder();

        for (String word : words) {
            if (!word.equalsIgnoreCase("and") &&
                    !word.equalsIgnoreCase("of") &&
                    !word.equalsIgnoreCase("the")) {
                code.append(word.substring(0, 1).toUpperCase());
            }
        }

        if (code.length() > 6) {
            return code.substring(0, 6);
        }

        return code.toString();
    }

    private String ensureUniqueCodeForSchool(School school, String baseCode) {
        String candidateCode = baseCode;
        int counter = 1;

        while (departmentRepository.existsByCodeAndSchoolId(candidateCode, school.getId())) {
            candidateCode = baseCode + counter;
            counter++;

            if (candidateCode.length() > 6) {
                candidateCode = baseCode.substring(0, Math.min(baseCode.length(), 5)) + counter;
            }
        }

        return candidateCode;
    }

    private static class SchoolData {
        final String name;
        final String code;
        final String email;

        SchoolData(String name, String code, String email) {
            this.name = name;
            this.code = code;
            this.email = email;
        }
    }
}