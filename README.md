# Secure Storage System (Server) for SecureVault
The Secure Storage System is the server-side component of SecureVault, a
secure file storage solution developed for the COMP3334 project. It
provides a REST API for user management, file storage, access control, log
auditing, and multi-factor authentication (MFA) for password resets. Built
with Spring Boot, it ensures data confidentiality, integrity, and
accountability, protecting against passive server adversaries and
unauthorized users.
## Features
The Secure Storage System offers the following server-side functionalities:
- Handles user registration, authentication, and password resets via REST
endpoints like `/api/user/register` and `/api/user/submit-token-password`.
- Stores encrypted files, salts, and IVs, ensuring client-side encryption
keeps data opaque to the server.
- Enforces access control through ownership and sharing policies, using JPA
queries for validation.
- Logs operations (e.g., login, upload, sharing) with tamper-evident
HmacSHA256 signatures, accessible to admins.
- Supports MFA for password resets by generating and verifying 6-digit OTPs
sent via email.
- Mitigates security threats like SQL injection through parameterized JPA
queries.
## Project Architecture
The Secure Storage System is a Spring Boot application with the following
components:
- **Controllers**: `UserController.java` manages user-related endpoints,
including MFA workflows.
- **Services**: `UserService.java`, `FileService.java`,
`PwdResetTokenService.java`, and `AuditLogService.java` handle business
logic for users, files, MFA, and logging.
- **Repositories**: JPA-based `UserRepository`, `FileRepository`,
`PwdResetTokenRepository`, and `AuditLogRepository` manage database
interactions.
- **Cryptography**: HmacSHA256 for hashing (passwords, OTPs, signatures),
SecureRandom for randomness, and AES support for client-side encryption
metadata.
The server communicates with the client via REST APIs over HTTP (assumed
TLS in production) and uses MySQL for persistent storage.
## Prerequisites
To run the Secure Storage System, ensure you have:
- Java 17 or later ([download](https://www.java.com/en/download/))
- Maven 3.8.x ([download](https://maven.apache.org/download.cgi))
- MySQL ([download](https://dev.mysql.com/downloads/installer/))
- An SMTP server (e.g., Gmail) for MFA email delivery
- Git (optional, for cloning)
## Setup Instructions
To set up the Secure Storage System, first install Java, Maven, and MySQL
as outlined in the prerequisites. Create a MySQL database named
`storageSystem` and ensure the MySQL service is running. Download the
repository into your desired directory
## Configure environment settings
In the Secure Storage System’s src/main/resources directory, create a file
named `env.properties` to store environment-specific variables. Add the
following entries, replacing placeholders with your MySQL credentials and
admin settings:
```
DB_USER=your_mysql_username
DB_PASSWORD=your_mysql_password
ADMIN_USERNAME=admin
ADMIN_EMAIL=admin@gmail.com
ADMIN_PASSWORD=admin@123
```
## Running the server
Run the server in bash using maven with the following command:

```
mvn clean compile exec:java
```

