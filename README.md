# Role-Based Content Management System

> **Provenance:** a clean reimplementation of a project I originally built as a Java course project; the original code was unavailable, so this was rebuilt from the original design. All 11 unit tests pass (verified with a JUnit stand-in, since Maven isn't installed on my machine). I have **not** launched and clicked through the Swing GUI, and persistence is in-memory only.

*Sep 2025 | Java course project*

A desktop GUI content management system in Java (Swing) built on an **MVC** architecture, with **role-based access control (RBAC)**, session-based authentication, CRUD operations, and server-side validation.

## Features
| | |
|---|---|
| **RBAC** | `ADMIN` and `USER` roles map to explicit permissions (`Role`/`Permission`). Users manage their own articles; admins manage everything and can promote/demote users. |
| **Authentication** | Passwords stored as salted PBKDF2-HMAC-SHA256 hashes. Login returns an unguessable session token with a sliding 30-minute expiry. Login errors never reveal whether a username exists. |
| **CRUD** | Create, read, update, delete articles. Every call re-checks the session and the caller's permission in the service layer, so hiding a button in the UI is never the only protection. |
| **Validation** | `Validator` enforces username/password rules and title/body limits on the service side and reports all problems at once. |
| **Safety rails** | The last remaining admin cannot be demoted. An admin who demotes themselves is signed out. |

## Architecture
```
view/        Swing screens (LoginView, MainView) - no business logic
controller/  AuthController, ArticleController - hold the session, delegate to services
service/     AuthService, ArticleService, Validator - rules, RBAC, validation
security/    PasswordHasher, SessionManager
repo/        UserRepository, ArticleRepository (+ in-memory implementations)
model/       User, Article, Role, Permission
```
Repositories are interfaces, so the in-memory implementations can be swapped for JDBC/files without touching the services.

## Run
Requires JDK 17+.
```bash
mvn -q package && java -jar target/role-based-cms-1.0.0.jar
# or without Maven:
mkdir -p out && javac -d out $(find src/main -name '*.java') && java -cp out cms.App
```
Demo accounts created on startup: `admin` / `Admin1234` (admin) and `alice` / `Alice1234` (user). You can also create accounts from the login screen (always regular users).

## Test
```bash
mvn test
```
