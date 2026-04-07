# Automated Lead Qualification Orchestrator

## Description
The Automated Lead Qualification Orchestrator is a robust backend system designed to evaluate and process sales leads before they are advanced to the "Prospect" stage. Built with Java 17 and Spring Boot, the application strictly adheres to Hexagonal Architecture and SOLID principles, ensuring domain logic is completely isolated from infrastructure concerns. It features parallel and sequential processing pipelines, custom resilience mechanisms, and an interactive CLI interface.

## Prerequisites
To build and run this application, you will need:
* Java 17
* Docker
* Git

## How to Run

### 1. Using Gradle Wrapper (Console)
Execute the application directly from your terminal using the provided Gradle wrapper. The `--console=plain` flag ensures a clean interactive CLI experience without background noise.
```bash
./gradlew bootRun --console=plain
```

### 2. Using IntelliJ IDEA
Open the project in IntelliJ IDEA. Locate the SalesApplication class containing the main method. Run this class directly. The interactive CLI will be available within the IDE's built-in run console.

### 3. Using Docker (Native)
Build the multi-stage Docker image and run it interactively to access the CLI.
```bash
docker build -t lead-orchestrator .
docker run -it lead-orchestrator
```

### 4. Using Docker Compose
Use Docker Compose to build and start the service. The run command is necessary to attach your terminal to the interactive CLI.
```bash
docker compose run orchestrator
```

## AI Integration, Versioning & Development Process

### v0: Technical Specification & Architectural Design
Spec-driven development was prioritized to define the system boundaries, interfaces, and concurrency model before writing implementation code. For detailed context, system requirements, architectural decisions, and testing strategy, please refer to the [SPECIFICATION.md](SPECIFICATION.md) file.

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

### v0.4: Test Coverage Expansion, Resilience Fixes & Quality Standards
* **AI Artifact (Chat Log):** [Gemini Conversation Link](https://gemini.google.com/share/68feb99dde22)
* **What Worked Well:** I successfully closed the testing gaps identified in v0.3 by introducing comprehensive unit tests for the infrastructure adapters (`LeadJsonFileAdapterTest`, `LeadJpaAdapterTest`) and schedulers (`LeadRetrySchedulerTest`). I also corrected an architectural mismatch in `ComplianceResilienceDecoratorTest`, ensuring the test accurately expects a `RuntimeException` when local retries are exhausted. This aligns the test suite with the intended resilience behavior in production.
* **What Went Wrong & Refinements Needed:** Although the test suite is more robust, I observed that the core domain model still processes the pipeline as an all-or-nothing operation. Currently, if the retry scheduler picks up a failed lead, it re-executes the entire pipeline. I need to address this lack of step-by-step idempotency to prevent redundant calls to external services that may have already succeeded.

### v0.5: Domain Validation, Custom Exceptions & Refactoring
* **AI Artifact (Chat Log):** [Gemini Conversation Link](https://gemini.google.com/share/35b6dbbb41df)
* **What Worked Well:**
  * **Rich Domain Model:** Implemented a self-validating `Lead` record that prevents invalid states (e.g., empty IDs, future birth dates, or malformed emails) at the constructor level.
  * **Custom Exception Handling:** Introduced `DomainValidationException` and `ResourceNotFoundException` to replace generic errors, providing better semantic meaning to failures.
  * **Semantic Refactoring:** Renamed decorators to `CachedComplianceBureau` and `RetryingComplianceBureau` to align with domain language rather than just design pattern names.
  * **CLI Resilience:** Updated the controller to gracefully catch and display domain errors to the user without crashing the application.
* **What Went Wrong & Refinements Needed:**
  * **Delayed Idempotency:** While the domain model was strengthened, the step-by-step idempotency (skipping already successful validations during retries) identified in v0.4 remains pending and is now the primary goal for the next architectural iteration.

### v1.0: Containerization & Integration Testing
* **AI Artifact (Chat Log):** [Gemini Conversation Link](https://gemini.google.com/share/dfa79b0f503c)
* **What Worked Well:**
  * **Docker Integration:** Successfully implemented containerization using a `Dockerfile` and `docker-compose.yml`. Configured `stdin_open: true` and `tty: true` to properly support the interactive CLI environment within the container.
  * **Integration Test Suite:** Introduced robust integration tests (`LeadOrchestratorIT`, `ComplianceConfigIT`, `LeadRetrySchedulerIT`) using `@SpringBootTest` and an in-memory H2 database. These tests accurately validate the Spring context wiring, asynchronous pipeline execution, and scheduled retry behaviors.
* **What Went Wrong & Refinements Needed:**
  * **Step-by-Step Idempotency:** The state machine idempotency planned in v0.5 remains pending. The orchestrator still lacks the granular ability to skip previously successful validation steps when a lead is re-injected by the retry scheduler.
  * **Quality Gates:** The integration of static analysis tools (e.g., Checkstyle, Spotless) and the enforcement of strict code coverage thresholds in the build process are still required for full production readiness.

## Pending Improvements
Based on the current state of the project, the following areas are identified for improvement to reach a production-ready standard:

1. **Advanced Step-by-Step Idempotency (State Machine):** Refactor the `Lead` domain model to track the individual status of each validation step (e.g., `registryStatus`, `judicialStatus`, `complianceStatus`). The `LeadOrchestrator` must be updated to skip previously approved validations when a lead is re-injected by the retry mechanism, optimizing resource usage and avoiding redundant API calls.

2. **Quality Gates, Code Coverage & Static Analysis:** Configure automated CI/CD pipelines (e.g., GitHub Actions) to enforce strict quality gates. This includes failing the build if test coverage drops below a defined threshold using **JaCoCo**, and integrating static analysis tools like `Checkstyle` or `Spotless` to enforce consistent code formatting and prevent code smells.

3. **Asynchronous Scalability & Resilience:** 
    * **Message Brokers:** Replace the current `LeadRetryScheduler` (based on a Spring `@Scheduled` cron job) with an event-driven architecture using a message broker (e.g., RabbitMQ, Apache Kafka, or AWS SQS). This will manage retries more efficiently through Dead Letter Queues (DLQ) and Delayed Exchanges.
     * **Circuit Breaker:** Implement the Circuit Breaker pattern (e.g., via Resilience4j) on the outbound adapters to prevent resource exhaustion when external simulated services experience prolonged downtime.