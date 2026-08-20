I am building a Flipkart-like e-commerce backend called "Nest Backend" using:

- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT authentication
- Maven
- Swagger/OpenAPI

I want to implement production-ready Role-Based Access Control (RBAC).

Roles:

1. USER
2. VENDOR
3. ADMIN

IMPORTANT:
The backend must be the actual security boundary.

Never rely on the React frontend for authorization.

A user must not be able to access another role's APIs simply by manually calling the endpoint through Postman, curl, browser devtools, etc.

First inspect my existing backend structure and authentication implementation before making changes.

Do not unnecessarily rewrite working authentication code.

Architecture should follow a clean structure similar to:

src/main/java/com/tarun/nest/
│
├── config/
│   ├── SecurityConfig.java
│   └── SwaggerConfig.java
│
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── VendorController.java
│   └── AdminController.java
│
├── dto/
│   ├── auth/
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   ├── RegisterRequest.java
│   │   └── RegisterResponse.java
│   │
│   └── user/
│
├── entity/
│   ├── User.java
│   ├── Role.java
│   └── ...
│
├── repository/
│   └── UserRepository.java
│
├── security/
│   ├── JwtAuthenticationFilter.java
│   └── ...
│
├── service/
│   ├── AuthService.java
│   ├── UserService.java
│   ├── VendorService.java
│   └── AdminService.java
│
├── service/impl/
│   ├── AuthServiceImpl.java
│   ├── UserServiceImpl.java
│   ├── VendorServiceImpl.java
│   └── AdminServiceImpl.java
│
└── util/
    └── JwtUtil.java

Create a Role enum:

USER
VENDOR
ADMIN

Add the role field to User:

@Enumerated(EnumType.STRING)
private Role role;

The database should store roles as strings:

USER
VENDOR
ADMIN

Registration requirements:

Public registration must create USER accounts by default.

A normal registration request must NOT allow the client to choose:

ADMIN
VENDOR

For example, the backend must reject or ignore:

{
  "email": "test@test.com",
  "password": "password",
  "role": "ADMIN"
}

A normal user must never be able to register themselves as ADMIN.

Vendor accounts should be created through an ADMIN-controlled flow.

Implement a vendor request/approval concept if appropriate:

USER requests to become VENDOR
        ↓
ADMIN reviews request
        ↓
ADMIN approves
        ↓
User role becomes VENDOR

Alternatively, if the existing project already has vendor approval logic, integrate with it instead of duplicating it.

JWT:

After successful login, generate a JWT containing:

- user ID
- email
- role

Example claims:

{
  "sub": "1",
  "email": "user@example.com",
  "role": "USER"
}

The login response should contain:

{
  "token": "...",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "role": "USER"
  }
}

Create/update JwtUtil to:
- Generate JWT
- Extract username/email
- Extract role
- Validate JWT
- Check expiration

Use a secure signing key suitable for the selected JWT algorithm.

Do not hardcode secrets directly in Java source code.

Use application properties/environment variables for JWT configuration.

Spring Security:

Configure Spring Security so that:

PUBLIC:
- /api/auth/**
- Swagger/OpenAPI endpoints

USER:
- /api/user/**
- /api/orders/**
- /api/cart/**

VENDOR:
- /api/vendor/**

ADMIN:
- /api/admin/**

For endpoints requiring multiple roles, use appropriate authorization.

Example:

ADMIN + VENDOR:
- product management where appropriate

ADMIN only:
- user management
- vendor approval
- system configuration

VENDOR:
- manage their own products
- manage their inventory
- view their own seller orders
- view their own earnings

USER:
- cart
- wishlist
- place orders
- view their own orders
- profile

VERY IMPORTANT:

Role authorization is not enough for ownership.

For example:

Vendor A must not be able to update Vendor B's product.

The backend must verify ownership:

authenticatedVendorId == product.vendorId

Similarly:

User A must not be able to access User B's orders.

The backend must verify:

authenticatedUserId == order.userId

Implement this securely.

Use Spring Security authorities correctly.

Make sure JWT role is converted into a Spring Security authority such as:

ROLE_USER
ROLE_VENDOR
ROLE_ADMIN

Use:

hasRole("USER")
hasRole("VENDOR")
hasRole("ADMIN")

or an equivalent secure configuration.

Create reusable authorization logic where appropriate.

Add proper exception handling for:

401 Unauthorized
403 Forbidden
404 Not Found
400 Bad Request

Return clean API responses instead of exposing stack traces.

Example:

{
  "status": 403,
  "message": "You do not have permission to access this resource"
}

Swagger:

Configure Swagger/OpenAPI so I can authenticate using the JWT.

Swagger should provide an Authorization mechanism such as:

Bearer <JWT>

After logging in, I should be able to authorize Swagger and test protected endpoints.

Security requirements:

- Passwords must use BCryptPasswordEncoder.
- Never return password in API responses.
- Never store plain-text passwords.
- Never trust role values supplied by the frontend.
- Never trust user IDs supplied by the frontend for ownership checks.
- Extract authenticated user identity from the JWT/SecurityContext.
- Do not expose sensitive JWT secrets.
- Use environment variables for production secrets.

Database:

Update the User table/schema appropriately.

Make sure existing users are handled safely if the role column is newly introduced.

Use a sensible default role for existing users, preferably USER unless the current data requires another approach.

Testing:

Create tests for:

1. USER can access USER endpoints.
2. USER cannot access ADMIN endpoints.
3. USER cannot access VENDOR endpoints.
4. VENDOR can access VENDOR endpoints.
5. VENDOR cannot access ADMIN endpoints.
6. ADMIN can access ADMIN endpoints.
7. Vendor A cannot modify Vendor B's product.
8. User A cannot access User B's order.
9. Invalid JWT returns 401.
10. Missing JWT returns 401.
11. Insufficient role returns 403.
12. Normal registration always creates USER.
13. User cannot self-register as ADMIN.
14. User cannot self-register as VENDOR.

Before implementing:

1. Inspect my existing package structure.
2. Inspect User entity.
3. Inspect Role implementation if it already exists.
4. Inspect RegisterRequest/RegisterResponse.
5. Inspect LoginRequest/LoginResponse.
6. Inspect JwtUtil.
7. Inspect JwtAuthenticationFilter.
8. Inspect SecurityConfig.
9. Inspect AuthController.
10. Inspect AuthService/AuthServiceImpl.

Then tell me exactly what needs to be changed.

Do not create duplicate classes if equivalent classes already exist.

Keep package names consistent with their actual filesystem location.

Implement the changes incrementally.

After each major change, explain:
- What changed
- Why it changed
- Which files were modified
- How I can test it using Swagger/Postman

The final result should be a clean, scalable, production-ready RBAC architecture suitable for a Flipkart-like e-commerce application.