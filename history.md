# Changelog

All notable changes to this project will be documented in this file.

## [4.1.0] - 2026-07-26

### Added
- GitHub Actions CI workflow (`.github/workflows/ci.yml`) for clean build on push/PR
- 3 end-to-end Spring Batch tests (`BatchJobE2ETest.java`)
  - Multi-record job completion test
  - Single-record job completion test
  - Non-existent input file failure test
- Test application context (`TestApplication.java`) excluding integration XML
- JUnit Platform configuration in Gradle 9.x

### Changed
| Component | Before | After |
|---|---|---|
| Gradle | 8.14 | 9.6.1 |
| Java (target) | 21 | 25 |
| Lombok | 1.18.30 | 1.18.42 |

### Fixed
- `sourceCompatibility`/`targetCompatibility` moved to `java {}` block (Gradle 9.x)
- Removed deprecated `DependencyReportTask` (Gradle 9.x)
- Lombok compatibility with Java 25 class files

---

## [4.0.7] - 2026-07-26

### Changed
| Component | Before | After |
|---|---|---|
| Spring Boot | 3.5.16 | 4.0.7 |
| Java (target) | 21 | 25 |
| Spring Batch packages | `org.springframework.batch.core.*` | Relocated sub-packages |

### Fixed
- Spring Batch 6.x package relocations (`job`, `step`, `listener`, `infrastructure.item`)
- Removed `@EnableBatchProcessing` (auto-configured in Boot 3+)
- Replaced `compile` → `implementation`, `maven` → `maven-publish`
- Added explicit `spring-boot-starter-batch` and `jackson-databind` dependencies
- Removed unused `gradle-testsets-plugin`
- Lombok `compileOnly` + `annotationProcessor` (Gradle 6+)
- Added explicit `io.spring.dependency-management` plugin

---

## [3.5.16] - 2026-07-26

### Changed
| Component | Before | After |
|---|---|---|
| Spring Boot | 3.4.1 | 3.5.16 |
| Spring Batch | 5.x | 6.0.4 |

---

## [3.4.1] - 2026-07-26

### Changed
| Component | Before | After |
|---|---|---|
| Spring Boot | 3.2.5 | 3.4.1 |
| Java (target) | 17 | 21 |

---

## [3.2.5] - 2026-07-26

### Changed
| Component | Before | After |
|---|---|---|
| Spring Boot | 3.0.13 | 3.2.5 |
| Spring Cloud | 2022.0.4 | 2023.0.1 |

---

## [3.0.13] - 2026-07-26

### Changed
| Component | Before | After |
|---|---|---|
| Spring Boot | 2.7.18 | 3.0.13 |
| Spring Cloud | 2021.0.8 | 2022.0.4 |
| Java (target) | 13 | 17 |

---

## [2.7.18] - 2026-07-26

### Changed
| Component | Before | After |
|---|---|---|
| Spring Boot | 2.0.9 | 2.7.18 |
| Spring Cloud | Finchley | 2021.0.8 |

---

## [2.0.9] - 2026-07-26

### Changed
| Component | Before | After |
|---|---|---|
| Spring Boot | 1.5.10 | 2.0.9 |
| Spring Batch | 3.x | 4.x |

### Fixed
- `bootRepackage` → `bootJar` (Boot 2.x)
- `JobBuilderFactory`/`StepBuilderFactory` → direct `JobBuilder`/`StepBuilder`

---

## [1.5.10] - 2018-05-29

### Added
- Initial project structure
- Spring Boot 1.5.10 with Spring Batch 3.0.9
- Gradle 4.4 with Java 1.8
- Spring Integration XML context
- Custom item writer with HTTP calls
- Job listener with report generation
- Jenkins pipeline configuration

### Dependencies
| Component | Version |
|---|---|
| Spring Boot | 1.5.10.RELEASE |
| Spring Cloud | Dalston.RELEASE |
| Gradle | 4.4 |
| Java | 1.8 |
| Spring Batch | 3.0.9 |
| Lombok | 1.16.8 |
