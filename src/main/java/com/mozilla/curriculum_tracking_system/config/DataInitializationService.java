package com.mozilla.curriculum_tracking_system.config;

import com.mozilla.curriculum_tracking_system.constants.RoleConstants;
import com.mozilla.curriculum_tracking_system.enums.CurriculumStatus;
import com.mozilla.curriculum_tracking_system.model.academic.AcademicLevel;
import com.mozilla.curriculum_tracking_system.model.curriculum.Curriculum;
import com.mozilla.curriculum_tracking_system.model.department.Department;
import com.mozilla.curriculum_tracking_system.model.roles.Role;
import com.mozilla.curriculum_tracking_system.model.school.School;
import com.mozilla.curriculum_tracking_system.model.user.User;
import com.mozilla.curriculum_tracking_system.repository.academic.AcademicLevelRepository;
import com.mozilla.curriculum_tracking_system.repository.curriculum.CurriculumRepository;
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

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService implements CommandLineRunner {

    // Default password for all test users
    private static final String DEFAULT_TEST_PASSWORD = "TestUser@123";
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final AcademicLevelRepository academicLevelRepository;
    private final SchoolRepository schoolRepository;
    private final DepartmentRepository departmentRepository;
    private final CurriculumRepository curriculumRepository;
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

//        initializeRoles();
//        initializeDefaultAdmin();
//        initializeAcademicLevels();
//        initializeSchoolsAndDepartments();
//        initializeRoleBasedUsers();
//        initializeCurriculums();

        log.info("System data initialization completed successfully!");
    }

    private void initializeRoles() {
        log.info("Initializing system roles...");

        createRoleIfNotExists(RoleConstants.ADMIN, "System Administrator with full access to some resources");
        createRoleIfNotExists(RoleConstants.QA, "Senior System Administrator with full access");
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
                    .phoneNumber("+254700000001")
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

    private void initializeRoleBasedUsers() {
        log.info("Initializing role-based test users...");

        // Create QA users (Senior administrators)
        createQAUsers();

        // Create additional admin users
        createAdditionalAdminUsers();

        // Create deans for each school
        createDeanUsers();

        // Create HODs for each department
        createHODUsers();

        // Create assistant users
        createAssistantUsers();

        log.info("Role-based user initialization completed");
    }

    private void createQAUsers() {
        log.info("Creating QA users...");

        Role qaRole = roleRepository.findByName(RoleConstants.QA)
                .orElseThrow(() -> new RuntimeException("QA role not found"));

        List<UserTemplate> qaUsers = Arrays.asList(
                new UserTemplate("qa_director", "qa.director@university.edu", "Dr. Sarah", "Mitchell", "+254700000010"),
                new UserTemplate("qa_manager", "qa.manager@university.edu", "Prof. James", "Wilson", "+254700000011"),
                new UserTemplate("qa_officer", "qa.officer@university.edu", "Dr. Mary", "Johnson", "+254700000012")
        );

        for (UserTemplate template : qaUsers) {
            createUserIfNotExists(template, qaRole, "QA");
        }

        log.info("Created {} QA users", qaUsers.size());
    }

    private void createAdditionalAdminUsers() {
        log.info("Creating additional admin users...");

        Role adminRole = roleRepository.findByName(RoleConstants.ADMIN)
                .orElseThrow(() -> new RuntimeException("Admin role not found"));

        List<UserTemplate> adminUsers = Arrays.asList(
                new UserTemplate("system_admin", "system.admin@university.edu", "Dr. Robert", "Davis", "+254700000020"),
                new UserTemplate("tech_admin", "tech.admin@university.edu", "Ms. Linda", "Brown", "+254700000021")
        );

        for (UserTemplate template : adminUsers) {
            createUserIfNotExists(template, adminRole, "ADMIN");
        }

        log.info("Created {} additional admin users", adminUsers.size());
    }

    private void createDeanUsers() {
        log.info("Creating dean users for each school...");

        Role deanRole = roleRepository.findByName(RoleConstants.DEAN)
                .orElseThrow(() -> new RuntimeException("Dean role not found"));

        List<School> schools = schoolRepository.findAll();

        Map<String, UserTemplate> deanTemplates = getDeanTemplates();

        for (School school : schools) {
            String schoolKey = school.getCode();
            UserTemplate template = deanTemplates.get(schoolKey);

            if (template != null) {
                User dean = createUserIfNotExists(template, deanRole, "DEAN");
                if (dean != null) {
                    // Update school with dean ID
                    school.setDeanId(dean.getId());
                    schoolRepository.save(school);
                    log.info("Assigned dean {} to school: {}", dean.getUsername(), school.getName());
                }
            } else {
                // Generate a default dean for schools not in the template
                String deanUsername = "dean_" + school.getCode().toLowerCase();
                String deanEmail = "dean." + school.getCode().toLowerCase() + "@university.edu";
                UserTemplate defaultTemplate = new UserTemplate(
                        deanUsername,
                        deanEmail,
                        "Dean",
                        school.getCode(),
                        "+254700" + String.format("%06d", 100 + school.getId())
                );

                User dean = createUserIfNotExists(defaultTemplate, deanRole, "DEAN");
                if (dean != null) {
                    school.setDeanId(dean.getId());
                    schoolRepository.save(school);
                    log.info("Assigned default dean {} to school: {}", dean.getUsername(), school.getName());
                }
            }
        }

        log.info("Created dean users for {} schools", schools.size());
    }

    private void createHODUsers() {
        log.info("Creating HOD users for each department...");

        Role hodRole = roleRepository.findByName(RoleConstants.HEAD_OF_DEPARTMENT)
                .orElseThrow(() -> new RuntimeException("HOD role not found"));

        List<Department> departments = departmentRepository.findAll();
        int phoneCounter = 200;

        for (Department department : departments) {
            String hodUsername = generateHODUsername(department.getName());
            String hodEmail = generateHODEmail(department.getName(), department.getSchool().getCode());
            String hodFirstName = "Dr. " + generateFirstName(department.getName());
            String hodLastName = generateLastName(department.getName());
            String phoneNumber = "+254700" + String.format("%06d", phoneCounter++);

            UserTemplate template = new UserTemplate(hodUsername, hodEmail, hodFirstName, hodLastName, phoneNumber);
            User hod = createUserIfNotExists(template, hodRole, "HOD");

            if (hod != null) {
                // Update department with HOD ID
                department.setHeadId(hod.getId());
                departmentRepository.save(department);
                log.info("Assigned HOD {} to department: {}", hod.getUsername(), department.getName());
            }
        }

        log.info("Created HOD users for {} departments", departments.size());
    }

    private void createAssistantUsers() {
        log.info("Creating assistant users...");

        Role assistantRole = roleRepository.findByName(RoleConstants.ASSISTANT_ROLE)
                .orElseThrow(() -> new RuntimeException("Assistant role not found"));

        List<UserTemplate> assistantUsers = Arrays.asList(
                new UserTemplate("assistant_academic", "assistant.academic@university.edu", "Ms. Grace", "Wanjiku", "+254700000300"),
                new UserTemplate("assistant_admin", "assistant.admin@university.edu", "Mr. David", "Kiprotich", "+254700000301"),
                new UserTemplate("assistant_qa", "assistant.qa@university.edu", "Ms. Faith", "Achieng", "+254700000302"),
                new UserTemplate("assistant_registry", "assistant.registry@university.edu", "Mr. John", "Mwangi", "+254700000303")
        );

        for (UserTemplate template : assistantUsers) {
            createUserIfNotExists(template, assistantRole, "ASSISTANT");
        }

        log.info("Created {} assistant users", assistantUsers.size());
    }

    private User createUserIfNotExists(UserTemplate template, Role role, String roleType) {
        if (!userRepository.existsByUsername(template.username) && !userRepository.existsByEmail(template.email)) {
            User user = User.builder()
                    .username(template.username)
                    .email(template.email)
                    .password(passwordEncoder.encode(DEFAULT_TEST_PASSWORD))
                    .firstName(template.firstName)
                    .lastName(template.lastName)
                    .phoneNumber(template.phoneNumber)
                    .isEnabled(true)
                    .isAccountNonExpired(true)
                    .isAccountNonLocked(true)
                    .isCredentialsNonExpired(true)
                    .roles(Set.of(role))
                    .build();

            User savedUser = userRepository.save(user);
            log.info("Created {} user: {} ({})", roleType, template.username, template.email);
            return savedUser;
        } else {
            log.debug("{} user already exists: {}", roleType, template.username);
            return userRepository.findByUsername(template.username).orElse(null);
        }
    }

    private Map<String, UserTemplate> getDeanTemplates() {
        Map<String, UserTemplate> templates = new HashMap<>();

        templates.put("SET", new UserTemplate("dean_set", "dean.set@university.edu",
                "Prof. Michael", "Njoroge", "+254700000050"));
        templates.put("SBE", new UserTemplate("dean_sbe", "dean.sbe@university.edu",
                "Dr. Patricia", "Wambui", "+254700000051"));
        templates.put("SHS", new UserTemplate("dean_shs", "dean.shs@university.edu",
                "Prof. Anthony", "Kipchoge", "+254700000052"));
        templates.put("SNS", new UserTemplate("dean_sns", "dean.sns@university.edu",
                "Dr. Elizabeth", "Nyong", "+254700000053"));
        templates.put("SSH", new UserTemplate("dean_ssh", "dean.ssh@university.edu",
                "Prof. Samuel", "Mutua", "+254700000054"));
        templates.put("SE", new UserTemplate("dean_se", "dean.se@university.edu",
                "Dr. Catherine", "Wairimu", "+254700000055"));
        templates.put("SAES", new UserTemplate("dean_saes", "dean.saes@university.edu",
                "Prof. Joseph", "Kariuki", "+254700000056"));
        templates.put("SL", new UserTemplate("dean_sl", "dean.sl@university.edu",
                "Dr. Margaret", "Adhiambo", "+254700000057"));

        return templates;
    }

    private String generateHODUsername(String departmentName) {
        String cleanName = departmentName.toLowerCase()
                .replaceAll("[^a-z\\s]", "")
                .replaceAll("\\s+", "_")
                .replaceAll("and", "");

        // Take first 3 letters of significant words
        String[] words = cleanName.split("_");
        StringBuilder username = new StringBuilder("hod_");

        for (String word : words) {
            if (word.length() >= 3 && !word.equals("and") && !word.equals("the") && !word.equals("of")) {
                username.append(word.substring(0, Math.min(3, word.length())));
            }
        }

        return username.toString().length() > 20 ? username.substring(0, 20) : username.toString();
    }

    private String generateHODEmail(String departmentName, String schoolCode) {
        String cleanName = departmentName.toLowerCase()
                .replaceAll("[^a-z\\s]", "")
                .replaceAll("\\s+", ".")
                .replaceAll("and", "");

        String[] words = cleanName.split("\\.");
        StringBuilder emailPrefix = new StringBuilder("hod.");

        for (String word : words) {
            if (word.length() >= 3 && !word.equals("and") && !word.equals("the") && !word.equals("of")) {
                emailPrefix.append(word.substring(0, Math.min(4, word.length()))).append(".");
            }
        }

        return emailPrefix.toString().replaceAll("\\.$", "") + "@" + schoolCode.toLowerCase() + ".university.edu";
    }

    private String generateFirstName(String departmentName) {
        List<String> firstNames = Arrays.asList(
                "Peter", "Mary", "John", "Grace", "David", "Faith", "Samuel", "Catherine",
                "Michael", "Patricia", "Anthony", "Elizabeth", "Joseph", "Margaret", "Daniel", "Susan"
        );

        int hash = Math.abs(departmentName.hashCode());
        return firstNames.get(hash % firstNames.size());
    }

    private String generateLastName(String departmentName) {
        List<String> lastNames = Arrays.asList(
                "Njoroge", "Wambui", "Kipchoge", "Nyong", "Mutua", "Wairimu", "Kariuki", "Adhiambo",
                "Mwangi", "Achieng", "Kiprotich", "Wanjiku", "Mbugua", "Chebet", "Ochieng", "Nduta"
        );

        int hash = Math.abs((departmentName + "lastname").hashCode());
        return lastNames.get(hash % lastNames.size());
    }

    // Rest of the existing methods remain the same...
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

    private void initializeCurriculums() {
        log.info("Initializing curriculums...");

        List<School> schools = schoolRepository.findAll();
        List<AcademicLevel> academicLevels = academicLevelRepository.findAll();

        if (schools.isEmpty() || academicLevels.isEmpty()) {
            log.warn("No schools or academic levels found. Skipping curriculum initialization.");
            return;
        }

        int totalCurriculumsCreated = 0;

        for (School school : schools) {
            List<Department> departments = departmentRepository.findBySchoolId(school.getId(),
                    org.springframework.data.domain.PageRequest.of(0, Integer.MAX_VALUE)).getContent();

            for (Department department : departments) {
                for (AcademicLevel academicLevel : academicLevels) {
                    List<CurriculumTemplate> templates = getCurriculumTemplatesForDepartment(
                            department.getName(), academicLevel.getName());

                    for (CurriculumTemplate template : templates) {
                        if (createCurriculumIfNotExists(school, department, academicLevel, template)) {
                            totalCurriculumsCreated++;
                        }
                    }
                }
            }
        }

        log.info("Curriculum initialization completed. Created {} new curriculums", totalCurriculumsCreated);
    }

    private boolean createCurriculumIfNotExists(School school, Department department,
                                                AcademicLevel academicLevel, CurriculumTemplate template) {
        Optional<Curriculum> existingCurriculum = curriculumRepository
                .findByNameAndDepartmentIdAndAcademicLevelId(template.name, department.getId(), academicLevel.getId());

        if (existingCurriculum.isEmpty()) {
            String curriculumCode = generateCurriculumCode(department.getCode(), academicLevel.getName(), template.suffix);
            curriculumCode = ensureUniqueCurriculumCode(curriculumCode);

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime effectiveDate = now.minusMonths(6); // 6 months ago
            LocalDateTime expiryDate = now.plusYears(template.durationYears + 1);

            Curriculum curriculum = Curriculum.builder()
                    .name(template.name)
                    .code(curriculumCode)
                    .durationSemesters(template.durationSemesters)
                    .status(template.status)
                    .effectiveDate(effectiveDate)
                    .expiryDate(expiryDate)
                    .isActive(true)
                    .school(school)
                    .department(department)
                    .academicLevel(academicLevel)
                    .build();

            if (template.status == CurriculumStatus.APPROVED) {
                curriculum.setApprovedAt(effectiveDate.plusDays(7));
            }

            curriculumRepository.save(curriculum);
            log.info("Created curriculum: {} ({}) for {} - {} - {}",
                    template.name, curriculumCode, school.getName(),
                    department.getName(), academicLevel.getName());
            return true;
        } else {
            log.debug("Curriculum already exists: {} for {} - {} - {}",
                    template.name, school.getName(), department.getName(), academicLevel.getName());
            return false;
        }
    }

    private List<CurriculumTemplate> getCurriculumTemplatesForDepartment(String departmentName, String academicLevel) {

        String baseName = "Bachelor";
        int baseDuration = 8;
        int baseYears = 4;

        if ("Masters".equals(academicLevel)) {
            baseName = "Master";
            baseDuration = 4;
            baseYears = 2;
        } else if ("PhD".equals(academicLevel)) {
            baseName = "Doctor of Philosophy";
            baseDuration = 6;
            baseYears = 3;
        }

        return new ArrayList<>(createDepartmentSpecificCurriculums(departmentName, baseName, baseDuration, baseYears));
    }

    private List<CurriculumTemplate> createDepartmentSpecificCurriculums(String departmentName,
                                                                         String baseName, int baseDuration, int baseYears) {
        List<CurriculumTemplate> templates = new ArrayList<>();

        List<String> degreeVariations = getDegreeVariations(departmentName, baseName);
        CurriculumStatus[] statuses = {CurriculumStatus.APPROVED, CurriculumStatus.UNDER_REVIEW,
                CurriculumStatus.PENDING, CurriculumStatus.APPROVED};

        int statusIndex = 0;
        for (String variation : degreeVariations) {
            CurriculumStatus status = statuses[statusIndex % statuses.length];

            templates.add(new CurriculumTemplate(
                    variation,
                    baseDuration,
                    baseYears,
                    status,
                    "V1"
            ));

            if (statusIndex % 3 == 0) {
                templates.add(new CurriculumTemplate(
                        variation + " (Revised)",
                        baseDuration,
                        baseYears,
                        CurriculumStatus.UNDER_REVIEW,
                        "V2"
                ));
            }

            statusIndex++;
        }

        return templates;
    }

    private List<String> getDegreeVariations(String departmentName, String baseName) {
        Map<String, List<String>> departmentVariations = new HashMap<>();

        departmentVariations.put("Computer Science and Information Technology", Arrays.asList(
                baseName + " of Science in Computer Science",
                baseName + " of Science in Information Technology",
                baseName + " of Science in Software Engineering",
                baseName + " of Science in Cybersecurity"
        ));

        departmentVariations.put("Electrical and Electronic Engineering", Arrays.asList(
                baseName + " of Engineering in Electrical Engineering",
                baseName + " of Engineering in Electronic Engineering",
                baseName + " of Engineering in Telecommunications",
                baseName + " of Engineering in Power Systems"
        ));

        departmentVariations.put("Mechanical Engineering", Arrays.asList(
                baseName + " of Engineering in Mechanical Engineering",
                baseName + " of Engineering in Automotive Engineering",
                baseName + " of Engineering in Manufacturing Engineering"
        ));

        departmentVariations.put("Civil Engineering", Arrays.asList(
                baseName + " of Engineering in Civil Engineering",
                baseName + " of Engineering in Structural Engineering",
                baseName + " of Engineering in Environmental Engineering"
        ));

        // Business departments
        departmentVariations.put("Business Administration", Arrays.asList(
                baseName + " of Business Administration",
                baseName + " of Business Administration in Management",
                baseName + " of Business Administration in Entrepreneurship"
        ));

        departmentVariations.put("Economics", Arrays.asList(
                baseName + " of Arts in Economics",
                baseName + " of Science in Economics",
                baseName + " of Arts in Development Economics"
        ));

        departmentVariations.put("Medicine", Arrays.asList(
                baseName + " of Medicine and Surgery",
                baseName + " of Medicine",
                baseName + " of Science in Medical Sciences"
        ));

        departmentVariations.put("Nursing", Arrays.asList(
                baseName + " of Science in Nursing",
                baseName + " of Science in Community Health Nursing",
                baseName + " of Science in Critical Care Nursing"
        ));

        addMoreDepartmentVariations(departmentVariations, baseName);

        return departmentVariations.getOrDefault(departmentName,
                Arrays.asList(baseName + " of Science in " + departmentName,
                        baseName + " of Arts in " + departmentName));
    }

    private void addMoreDepartmentVariations(Map<String, List<String>> departmentVariations, String baseName) {
        departmentVariations.put("Mathematics and Statistics", Arrays.asList(
                baseName + " of Science in Mathematics",
                baseName + " of Science in Statistics",
                baseName + " of Science in Applied Mathematics"
        ));

        departmentVariations.put("Physics", Arrays.asList(
                baseName + " of Science in Physics",
                baseName + " of Science in Applied Physics",
                baseName + " of Science in Theoretical Physics"
        ));

        departmentVariations.put("Primary Education", Arrays.asList(
                baseName + " of Education in Primary Education",
                baseName + " of Education in Early Childhood Education"
        ));

        departmentVariations.put("Private Law", Arrays.asList(
                baseName + " of Laws",
                baseName + " of Laws in Private Law"
        ));

        departmentVariations.put("Crop Science", Arrays.asList(
                baseName + " of Science in Crop Science",
                baseName + " of Science in Agronomy"
        ));
    }

    private String generateCurriculumCode(String departmentCode, String academicLevel, String suffix) {
        String levelCode = academicLevel.substring(0, Math.min(2, academicLevel.length())).toUpperCase();
        return departmentCode + "-" + levelCode + "-" + suffix;
    }

    private String ensureUniqueCurriculumCode(String baseCode) {
        String candidateCode = baseCode;
        int counter = 1;

        while (curriculumRepository.findByCode(candidateCode).isPresent()) {
            candidateCode = baseCode + "-" + counter;
            counter++;
        }

        return candidateCode;
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

    // Record classes for data templates
    private record SchoolData(String name, String code, String email) {
    }

    private record CurriculumTemplate(String name, int durationSemesters, int durationYears, CurriculumStatus status,
                                      String suffix) {
    }

    private record UserTemplate(String username, String email, String firstName, String lastName, String phoneNumber) {
    }
}