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
* **AI Artifact (Chat Log):** [Gemini Conversation Link](https://gemini.google.com/share/b26819ddb3f6)
* **What Worked Well:** The LLM successfully implemented the complete CRUD lifecycle for the `Lead` entity, adhering to the Hexagonal Architecture. It correctly generated the necessary inbound/outbound ports, domain services (`LeadManagementService`), and JPA adapters. The unit tests for the domain layer (`LeadOrchestratorTest` and decorators) were correctly structured using Mockito to ensure pure isolation.
* **What Went Wrong & Refinements Needed:**
  1. **Test Context Overhead:** The prompt explicitly requested `@DataJpaTest` for infrastructure tests, but the AI used the heavier `@SpringBootTest` alongside `@Transactional` in `LeadJpaAdapterTest`. This requires manual refactoring to use the correct test slice and optimize execution time.
  2. **Incomplete Test Coverage:** Specific unit tests for the `LeadManagementService` and integration tests for `ComplianceCacheJpaAdapter` were omitted by the AI and must be added manually.
  3. **Legacy Configuration:** The AI implementation maintained legacy XML configuration (`persistence.xml`). This will be manually removed to centralize all persistence settings in `application.properties` following Spring Boot best practices.
  4. **Adapter Flexibility:** To improve architectural flexibility, manual refinements will be made to introduce `@ConditionalOnProperty`. This will allow dynamic switching between different database implementations or stubs directly through application properties.

### v0.3: Asynchronous Resilience, Schedulers & Infrastructure Flexibility
* **AI Artifact (Chat Log):** [Gemini Conversation Link](https://gemini.google.com/share/08dfadd104bc)
* **What Worked Well:** The LLM successfully implemented the JSON file-based repository adapter (`LeadJsonFileAdapter`) and seamlessly integrated it using Spring's `@ConditionalOnProperty`. The evolution of the domain model to support state and retries (`validationStatus`, `retryCount`, `nextRetryTime`) was done while strictly respecting the Hexagonal Architecture. The AI also correctly built the `LeadRetryScheduler` with `@Scheduled` to handle asynchronous exponential backoffs without polluting the domain layer with infrastructure concerns.
* **What Went Wrong & Refinements Needed:**
  1. **Idempotency Considerations:** Re-injecting a lead into the pipeline triggers all validations again. While the cache handles the Compliance Bureau, we risk re-executing the National Registry and Judicial checks. In a production environment, the domain model should be refined to track the status of each individual validation step to ensure true idempotency.
  2. **Incomplete Test Coverage for New Components:** The AI focused heavily on the implementation code but omitted the unit tests for the `LeadRetryScheduler` and the new `LeadJsonFileAdapter`. These tests must be authored manually to ensure the persistence and scheduling logic work as intended under different conditions.

## Pending Improvements
Based on the final state of the project, the following technical debt and improvements remain:
1. **Containerization:** Add Docker and Docker Compose configurations to package the application and run the simulated external services/databases consistently across environments.
2. **RESTful API Layer:** Replace or complement the CLI interface with a REST API adapter (Spring Web) to make the orchestration layer consumable by external CRMs or other internal microservices.
3. **Advanced Step-by-Step Idempotency:** Refactor the `Lead` model and database schema to store the individual results of each validation step (Registry, Judicial, Compliance). This will prevent redundant external calls when a lead is re-processed by the retry scheduler.