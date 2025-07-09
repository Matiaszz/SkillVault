# SkillVault

SkillVault is a technical skills certifications and validations platform built with Spring Boot, supporting user authentication, skill management, and profile picture storage using Azure Blob Storage.

> **Status:** Project under active development.

## Project Structure

```
rootDir/
├── Docker/
│   └── backend/
│       ├── Dockerfile
│       └── wait-connection.sh
├── docker-compose.yaml
├── SkillVault/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/skillvault/backend/
│   │   │   │   ├── Controllers/
│   │   │   │   ├── Domain/
│   │   │   │   ├── Domain/Enums/
│   │   │   │   ├── dtos/Requests/
│   │   │   │   ├── dtos/Responses/
│   │   │   │   ├── Events/
│   │   │   │   ├── Exceptions/
│   │   │   │   ├── Repositories/
│   │   │   │   ├── Security/
│   │   │   │   ├── Services/
│   │   │   │   ├── Utils/
│   │   │   │   └── Validations/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application.example.properties
│   ├── test/
│   │   └── java/com/skillvault/backend/
│   ├── target/
│   │   └── backend-0.0.1-SNAPSHOT.jar
│   ├── .mvn/
│   ├── .idea/
│   ├── .gitignore
│   ├── .gitattributes
│   ├── LICENSE
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── pom.xml
│   ├── README.md
│   ├── skillvault.http
│   └── compose-example.yaml
```

## Features

- **User Registration & Authentication**: Register as User, Evaluator, or Admin. JWT-based authentication with secure cookies.
- **Skill Management**: Users can add, list, and delete their skills.
- **Profile Management**: Users can update their profile and upload a profile picture (stored in Azure Blob Storage).
- **Role-based Access Control**: Endpoints are protected based on user roles.
- **Validation & Error Handling**: DTO validation and global exception handling.
- **Docker Support**: Ready-to-use Dockerfiles and docker-compose for local development and deployment.

## Configuration

- **application.properties**: Main Spring Boot configuration.
- **application.example.properties**: Example configuration with placeholders for secrets and Azure credentials.
- **compose-example.yaml**: Example Docker Compose file for local development.

## Running Locally

### Prerequisites

- Java 21+
- Maven
- Docker & Docker Compose

### Up docker MySQL service
1. Copy `compose-example.yaml` to the project root as `docker-compose.yaml`.
2. Edit environment variables in the compose file and `application.properties` as needed.

```sh
docker-compose up mysql
```

### Build the Project

```sh
./mvnw clean package
```

### Run with Docker Compose

Build and start the containers:

```sh
docker-compose up --build
```

### Manual Run

You can also run the backend directly:

```sh
./mvnw spring-boot:run
```

## API Overview

### Authentication

- `POST /api/auth/user/register` — Register a new user (auto-login)
- `POST /api/auth/user/login` — Login and receive JWT in a secure cookie
- `POST /api/auth/logout` — Logout (expires cookie)
- `POST /api/auth/evaluator/register` — Register evaluator (admin only)
- `POST /api/auth/admin/register` — Register admin (admin only)

### User

- `GET /api/user` — Get current user profile
- `PATCH /api/user` — Update user profile
- `POST /api/user/uploadProfileImg` — Upload profile image

### Skill

- `POST /api/skill` — Add a new skill
- `GET /api/skill/my` — List current user's skills
- `DELETE /api/skill/{skillId}` — Delete a skill

## Validation

- DTOs use Jakarta Bean Validation annotations.
- Custom password validation ensures strong passwords.
- Additional manual validation in [`DTOValidator`](src/main/java/com/skillvault/backend/Validations/DTO/DTOValidator.java).

## Error Handling

- Centralized in [`GlobalExceptionHandler`](src/main/java/com/skillvault/backend/Exceptions/GlobalExceptionHandler.java).
- Returns structured error responses for validation and server errors.

## Security

- JWT tokens are used for authentication, stored in HTTP-only, secure cookies.
- Role-based access control is enforced in [`SecurityConfig`](src/main/java/com/skillvault/backend/Security/SecurityConfig.java).
- CORS is configured for frontend integration.

## Azure Blob Storage

- Profile images are uploaded to Azure Blob Storage using credentials from configuration.
- See [`AzureService`](src/main/java/com/skillvault/backend/Services/AzureService.java) for implementation.

## Docker

- Backend is containerized using [`Dockerfile`](../Docker/backend/Dockerfile).
- Waits for MySQL to be available before starting the app using [`wait-connection.sh`](../Docker/backend/wait-connection.sh).
- The [`docker-compose.yaml`](../docker-compose.yaml) file orchestrates the backend and MySQL containers.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

---

## Testing the API

If you want to test the API quickly, use the following temporary public URL:

https://0n4n5rfs-8080.brs.devtunnels.ms/

*Note:* If this URL is disabled or unavailable, please contact me.

For detailed information, refer to the source code and inline comments throughout the project.
