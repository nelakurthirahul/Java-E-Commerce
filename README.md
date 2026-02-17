# Demo Spring Boot JWT Auth API

This project is a Spring Boot REST API with:
- User registration and login
- JWT-based authentication
- Protected endpoint example
- H2 in-memory database
- Integration tests with MockMvc

## Tech Stack
- Java 17 (project target)
- Spring Boot 3.2.5
- Spring Security
- Spring Data JPA
- H2 Database
- JJWT 0.11.5
- Maven Wrapper (`mvnw.cmd` / `mvnw`)

## Project Structure
- `src/main/java/com/example/demo/DemoApplication.java`: app entry point
- `src/main/java/com/example/demo/controller/AuthController.java`: register/login APIs
- `src/main/java/com/example/demo/controller/ProfileController.java`: protected profile API
- `src/main/java/com/example/demo/service/UserService.java`: auth business logic
- `src/main/java/com/example/demo/config/SecurityConfig.java`: security configuration
- `src/main/java/com/example/demo/config/JwtAuthenticationFilter.java`: JWT request filter
- `src/main/java/com/example/demo/config/JwtUtil.java`: token create/validate utilities
- `src/main/java/com/example/demo/model/User.java`: JPA entity
- `src/main/java/com/example/demo/repository/UserRepository.java`: JPA repository
- `src/main/resources/application.properties`: DB and app config
- `src/test/java/com/example/demo/DemoApplicationTests.java`: integration tests

## Prerequisites
1. JDK installed (17 recommended).
2. Maven is not required globally because wrapper is included.

Check Java:
```powershell
java -version
```

## Build and Test
Run tests:
```powershell
.\mvnw.cmd clean test
```

Build jar:
```powershell
.\mvnw.cmd clean package -DskipTests
```

## Run the Application
Default port:
```powershell
.\mvnw.cmd spring-boot:run -DskipTests
```

If port `8080` is already used, run on `8081`:
```powershell
.\mvnw.cmd spring-boot:run -DskipTests "-Dspring-boot.run.arguments=--server.port=8081"
```

## API Endpoints
Base URL examples below use `http://localhost:8081`.

### 1) Register User
- Method: `POST`
- URL: `/auth/register`
- Body:
```json
{
  "username": "alice",
  "password": "secret123"
}
```
- Success: `201 Created`
- Response:
```json
{
  "id": 1,
  "username": "alice"
}
```
- Possible errors:
  - `409 Conflict` if username already exists

### 2) Login
- Method: `POST`
- URL: `/auth/login`
- Body:
```json
{
  "username": "alice",
  "password": "secret123"
}
```
- Success: `200 OK`
- Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```
- Possible errors:
  - `401 Unauthorized` for invalid credentials

### 3) Protected Profile Endpoint
- Method: `GET`
- URL: `/api/profile`
- Header: `Authorization: Bearer <token>`
- Success: `200 OK`
- Response:
```json
{
  "username": "alice"
}
```
- Without valid token:
  - `401 Unauthorized`

## PowerShell Quick Test Flow
```powershell
# Register
Invoke-RestMethod -Method POST -Uri "http://localhost:8081/auth/register" `
  -ContentType "application/json" `
  -Body '{"username":"alice","password":"secret123"}'

# Login
$login = Invoke-RestMethod -Method POST -Uri "http://localhost:8081/auth/login" `
  -ContentType "application/json" `
  -Body '{"username":"alice","password":"secret123"}'
$token = $login.token

# Access protected endpoint
Invoke-RestMethod -Method GET -Uri "http://localhost:8081/api/profile" `
  -Headers @{ Authorization = "Bearer $token" }
```

## H2 Database
- Console path: `/h2-console`
- Example URL: `http://localhost:8081/h2-console`
- JDBC URL: `jdbc:h2:mem:ecommerce`
- Username: `sa`
- Password: empty

## Current Security Behavior
- Stateless session policy (`SessionCreationPolicy.STATELESS`)
- `POST /auth/**` and `/h2-console/**` are public
- All other routes require JWT
- Form login and HTTP basic are disabled
- Unauthorized requests return `401`

## Troubleshooting
### Port 8080 already in use
Find process:
```powershell
netstat -ano | findstr :8080
```
Stop process by PID:
```powershell
taskkill /PID <PID> /F
```

### Class format / Java version mismatch after switching JDK
If you see errors like unsupported class file version, clean build:
```powershell
.\mvnw.cmd clean test
```

### App starts but requests fail unauthorized
- Ensure `Authorization` header is exactly:
  - `Bearer <token>`
- Ensure token was generated from `/auth/login` in the same app run.

## Notes
- JWT secret key is generated at app startup by `JwtUtil`, so tokens from previous runs are invalid after restart.
- This setup is good for demo/development. For production, externalize JWT secret and add user roles/authorities.
