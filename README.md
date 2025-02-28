# SopiaTech Eats (24-25) - Team H

Welcome to the repository of Team H for the SopiaTech Eats project. This repository contains the source code, documentation, and other resources for our project.

## **Table of Contents**

1. [Team Members and Roles](#team-members-and-roles)
2. [Documentation](#documentation)
3. [Brief presentation of our project](#brief-presentation-of-our-project)
4. [Project Structure](#project-structure)
5. [How to run the application](#how-to-run-the-application)
6. [GitHub Actions](#github-actions)
7. [Contributing](#contributing)

## Team Members and Roles

- **Sagesse Adabadji** - **Product Owner**
- **Sara Taoufiq** - **Software Architect**
- **Jean Paul Assimpah** - **Quality Assurance Engineer**
- **Selom Adzaho** - **Quality Assurance Engineer**
- **Abenezer Yifru** - **Operations Specialist**


## Documentation

You can find further documentation for our project in the `doc/` directory.
[View our report here](https://github.com/PNS-Conception/STE-24-25--teamh/tree/main/doc)

## Brief presentation of our project

Our project is a system designed to manage food ordering on our school campus. It allows students to place orders at the campus restaurant while enabling the restaurant manager to handle the restaurant's capacity by updating schedules the restaurant manager can also implement discount strategies, modify items and their preparation time. This system ensures that order placement is feasible and efficient regarding the restaurant's capacities.

### User Stories

#### **[US] Implementation of Status Discount Strategy #74**

- **GitHub Issue ID**: [#74](https://github.com/PNS-Conception/STE-24-25--teamh/issues/74)
- **User Story**: As a restaurant manager, I want to apply a status-based discount for specific customer groups (e.g., CAMPUS_STUDENT, LOYAL_CUSTOMER) so that eligible customers can benefit from reduced pricing based on their status.
- **Feature File**: `statusDiscount.feature`


#### **[US] Restaurant Opening Hours Management**

- **GitHub Issue ID**: [#8](https://github.com/PNS-Conception/STE-24-25--teamh/issues/8)
- **User Story**: As a Restaurant Manager, I want to manage the restaurant's opening hours so that customers can view accurate information on when the restaurant is open.
- **Feature File**: `RestaurantScheduleManagement.feature`


#### **[US] Group Order Validation**

- **GitHub Issue ID**: [#77](https://github.com/PNS-Conception/STE-24-25--teamh/issues/77)
- **User Story**: As a Campus User, I want to validate our Group Order so that it moves forward in the delivery process.
- **Feature File**: `groupOrderValidation.feature`

## Project Structure

The project is structured as follows to ensure clear organization and easy maintenance:

- **`.github/`**: Contains GitHub-specific configurations, such as templates for issues and CI workflows.
- **`doc/`**: Technical documentation, project reports and other related documents.
- **`src/`**: Core application source code.
  - **`main/java/fr/unice/polytech/`**: Main application source code, organized by layers (clean architecture) functional packages.
    - **`application/`**: Application services and use cases.
    - **`domain/`**: Domain entities and business logic.
    - **`infrastructure/`**: Infrastructure and external dependencies.
    - **`server/`**: HttpHandlers (controllers) and API gateway.
  - **`test/java/fr/unice/polytech/`**: Steps definitions and JUnit tests for the application.
    - **`application/`**: Application services and use cases junit tests.
    - **`domain/`**: Domain entities and business logic junit tests.
    - **`infrastructure/`**: Infrastructure and external dependencies junit tests.
    - **`stepDefs`**: Steps definitions for Cucumber tests.
  - **`test/resources/features/`**: BDD feature files for Cucumber tests.


## How to run the application

### Requirements

- **Java** (version 21)
- **Maven** (version 3.9.9)
- (Optional) **Git** for cloning the repository

### Installation

1. **Clone the repository**:

```bash
git clone https://github.com/PNS-Conception/STE-24-25--teamh.git
```

2. **Navigate to the project directory**:

```bash
cd STE-24-25--teamh
```

3. **Running the tests**:

```bash
mvn clean test
```

This command will run the Cucumber and JUnit tests in the project.

### Building and Running the Application

// TODO: Configure `pom.xml` and set the main class to run for the project.

1. This command will build the project and create a JAR file in the `target` directory.

```bash
mvn clean install
```

2. **Run the JAR file**:

```bash
java -jar target/steats-1.0-SNAPSHOT-jar-with-dependencies.jar
```

(Or run the main class directly)

```bash
mvn exec:java -Dexec.mainClass="fr.unice.polytech.server.ApiGateway.ApiGateway"
```

## Contributing

### GitHub Actions

We have set up a GitHub Actions file under `workflows/maven.yml`. This action is triggered whenever code is pushed to the repository. In this initial version, it runs a JUnit5 test to ensure everything is functioning correctly.

### Issue Templates

Under `ISSUE_TEMPLATE`, we have created templates for user stories and bug reports. These templates can be customized as needed.
