# AI-Leveraged Backend Challenge: JVM Edition

*Note: For detailed context, system requirements, architectural decisions, and testing strategy, please refer to the [SPECIFICATION.md](SPECIFICATION.md) file.*

## 1. AI Integration, Versioning & Development Process
As required, this section documents the AI workflow, chat logs, and refinements.

### v0: Technical Specification & Architectural Design
Spec-driven development was prioritized to define the system boundaries, interfaces, and concurrency model before writing implementation code.

### v0.1: Initial AI Generation and Refinement
* **AI Artifact (Chat Log):** [Gemini Conversation Link](https://gemini.google.com/share/b08b291631a3)
* **What Worked Well:** The LLM successfully established the boilerplate for Hexagonal Architecture and correctly applied the requested Design Patterns (Strategy, Orchestrator, Decorator). It also implemented the `CompletableFuture` logic accurately.
* **What Went Wrong & Refinements Needed:**
    1.  **Framework Omission:** The AI generated pure Java instantiations. The output had to be manually refined to leverage Spring Boot (Bean injection, Spring Data JPA) to ensure enterprise-grade standards.
    2.  **Dependency Obsolescence:** The AI hallucinated older library specifications, using the deprecated `javax` persistence namespace instead of the modern `jakarta` namespace required for newer Spring Boot versions. This required manual migration.
    3.  **Rigid Interfaces:** The generated CLI was static and functioned like a hardcoded test case. It was refactored into a dynamic, interactive CLI.
    4.  **Scope Limitation:** The output focused strictly on the validation pipeline, completely omitting standard CRUD operations for the leads.

### v0.2: CRUD Operations, Testing & Configuration Refinement
* **AI Artifact (Chat Log):** [Gemini Conversation Link](https://gemini.google.com/share/b26819ddb3f6) (Note: Raw prompt logs are also stored in the `ai-artifacts/` directory to strictly comply with the submission requirements).
* **What Worked Well:** The LLM successfully implemented the complete CRUD lifecycle for the `Lead` entity, adhering to the Hexagonal Architecture. It correctly generated the necessary inbound/outbound ports, domain services (`LeadManagementService`), and JPA adapters. The unit tests for the domain layer (`LeadOrchestratorTest` and decorators) were correctly structured using Mockito to ensure pure isolation.
* **What Went Wrong & Refinements Needed:**
  1. **Test Context Overhead:** The prompt explicitly requested `@DataJpaTest` for infrastructure tests, but the AI used the heavier `@SpringBootTest` alongside `@Transactional` in `LeadJpaAdapterTest`. This requires manual refactoring to use the correct test slice and optimize execution time.
  2. **Incomplete Test Coverage:** Specific unit tests for the `LeadManagementService` and integration tests for `ComplianceCacheJpaAdapter` were omitted by the AI and must be added manually.
  3. **Legacy Configuration:** The AI implementation maintained legacy XML configuration (`persistence.xml`). This will be manually removed to centralize all persistence settings in `application.properties` following Spring Boot best practices.
  4. **Adapter Flexibility:** To improve architectural flexibility, manual refinements will be made to introduce `@ConditionalOnProperty`. This will allow dynamic switching between different database implementations or stubs directly through application properties.

## Pending Improvements
Based on the final state of the project, the following technical debt and improvements remain:
1. **Containerization:** Add Docker and Docker Compose configurations to package the application and run the simulated external services/databases consistently across environments.
2. **Retry Mechanism & Advanced Resilience:** Implement a robust retry mechanism (e.g., using a library like Resilience4j) for the external API calls. Currently, network failures are handled with a basic fallback to manual review, but adding automatic retries with exponential backoff will significantly improve the system's reliability against transient network issues.
3. 3. **Lead Status Visibility:** Update the database queries and the CLI read operations to store, fetch, and display the current validation status of each lead. This will provide complete observability of the pipeline results directly from the directory view.