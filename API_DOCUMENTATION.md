# Nest E-Commerce Backend - API Documentation

## ✅ Status
- **Application Status**: RUNNING ✅
- **Database**: Connected (PostgreSQL) ✅
- **Swagger UI**: Available ✅
- **APIs**: Login & Register Ready ✅

---

## 📍 Access Points

### Swagger UI
**URL**: `http://localhost:8080/swagger-ui.html`
**API Docs**: `http://localhost:8080/v3/api-docs`

---

## 🔐 Authentication APIs

### 1. **REGISTER API**
**Endpoint**: `POST http://localhost:8080/api/auth/register`

**Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "Password@123",
  "confirmPassword": "Password@123",
  "securityCode": "1234",
  "firstName": "John",
  "lastName": "Doe",
  "role": "CUSTOMER"
}
```

**Response (201 Created)**:
```json
{
  "message": "Registration successful",
  "success": true,
  "userId": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "CUSTOMER"
}
```

**Validation Rules**:
| Field | Validation |
|-------|-----------|
| username | Required, Unique |
| email | Valid email format, Unique |
| password | Min 8 chars, letters, numbers, special chars |
| confirmPassword | Must match password |
| securityCode | Must be exactly "1234" |
| role | CUSTOMER or VENDOR |
| firstName | Required |
| lastName | Required |

---

### 2. **LOGIN API**
**Endpoint**: `POST http://localhost:8080/api/auth/login`

**Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "email": "john@example.com",
  "password": "Password@123"
}
```

**Response (200 OK)**:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huQGV4YW1wbGUuY29tIiwiaWF0IjoxNjg2ODQwODAwLCJleHAiOjE2ODY5MjcyMDB9.signature...",
  "message": "Login successful",
  "userId": 1,
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Error Response (401 Unauthorized)**:
```json
{
  "token": null,
  "message": "Invalid password",
  "userId": null,
  "email": null,
  "firstName": null,
  "lastName": null
}
```

---

## 🗄️ Database Schema

### users table
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(255) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  security_code VARCHAR(255) NOT NULL,
  role ENUM('CUSTOMER', 'VENDOR') NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE
);
```

---

## 🔑 User Roles
- **CUSTOMER**: Regular user for shopping
- **VENDOR**: Merchant/seller account

---

## 🛡️ Security Features
- ✅ Password encoding using BCrypt
- ✅ JWT Token generation (24-hour expiration)
- ✅ CORS enabled
- ✅ Security code validation (fixed to "1234")
- ✅ Email and username uniqueness constraints
- ✅ Account active/inactive status

---

## 🧪 Testing with cURL

### Test Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jane_smith",
    "email": "jane@example.com",
    "password": "SecurePass@123",
    "confirmPassword": "SecurePass@123",
    "securityCode": "1234",
    "firstName": "Jane",
    "lastName": "Smith",
    "role": "VENDOR"
  }'
```

### Test Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane@example.com",
    "password": "SecurePass@123"
  }'
```

---

## 📁 Files Created

```
config/
├── SecurityConfig.java      - Spring Security configuration
└── SwaggerConfig.java       - OpenAPI/Swagger UI configuration

controller/
└── AuthController.java      - REST endpoints for auth

dto/
├── LoginRequest.java        - Login request model
├── LoginResponse.java       - Login response model
├── RegisterRequest.java     - Registration request model
└── RegisterResponse.java    - Registration response model

entity/
└── User.java               - User JPA entity

enums/
└── UserRole.java           - User role enumeration

repository/
└── UserRepository.java     - Database access layer

service/
└── AuthService.java        - Business logic

util/
└── JwtUtil.java            - JWT token utilities
```

---

## 🚀 Next Steps

1. ✅ Database created and connected
2. ✅ Login API working
3. ✅ Register API working
4. ✅ Swagger UI configured
5. Ready for additional features:
   - Product management APIs
   - Order management APIs
   - User profile endpoints
   - Payment integration
   - Product reviews and ratings

---

## ⚙️ Configuration (application.properties)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/nest
spring.datasource.username=tarunkumar
spring.datasource.password=1234
spring.jpa.hibernate.ddl-auto=update
jwt.secret=MySecretKeyForJWTTokenGenerationAndValidationPurpose123456789
jwt.expiration=86400000
```

---

## 🔧 Troubleshooting

**Issue**: "Username already exists"
- **Solution**: Use a different username

**Issue**: "Email already exists"
- **Solution**: Use a different email

**Issue**: "Invalid security code"
- **Solution**: Security code must be "1234"

**Issue**: "Passwords do not match"
- **Solution**: Confirm password must match password field

---

**Built with**: Spring Boot 3.5.3 | Spring Security | PostgreSQL | JWT | Swagger/OpenAPI 3.0

