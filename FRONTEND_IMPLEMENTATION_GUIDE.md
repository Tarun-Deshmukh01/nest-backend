# Frontend Implementation Checklist & API Usage Patterns

## 🚀 QUICK START (Choose Your Framework)

### React
```bash
npm install react-router-dom axios
```

### Vue 3
```bash
npm install vue-router axios
```

### Angular
```bash
ng new nest-frontend
npm install @angular/common/http
```

### Flutter
```bash
flutter pub add http shared_preferences
```

### Next.js
```bash
npm install next axios
```

---

## 📋 IMPLEMENTATION CHECKLIST

### Step 1: Setup Auth Service ✅
- [ ] Create auth service file with login/logout methods
- [ ] Store token in localStorage/sessionStorage
- [ ] Create getToken() method
- [ ] Create isAuthenticated() method
- [ ] Create getUserInfo() method

### Step 2: Create Login Form ✅
- [ ] Build login component/page
- [ ] Add email & password inputs
- [ ] Add form validation
- [ ] Add error handling
- [ ] Add loading state

### Step 3: Add Authentication to Requests ✅
- [ ] Setup HTTP interceptor/middleware
- [ ] Add Authorization header: `Bearer {token}`
- [ ] Handle 401 Unauthorized responses
- [ ] Redirect to login on 401

### Step 4: Protect Routes ✅
- [ ] Create PrivateRoute/ProtectedRoute component
- [ ] Check token before rendering page
- [ ] Redirect to login if no token
- [ ] Add loading state during auth check

### Step 5: Implement Logout ✅
- [ ] Create logout button in navbar/menu
- [ ] Call logout endpoint (optional)
- [ ] Clear localStorage
- [ ] Redirect to login page
- [ ] Clear any cached user data

### Step 6: Error Handling ✅
- [ ] Handle invalid credentials
- [ ] Handle network errors
- [ ] Handle token expiration
- [ ] Show user-friendly error messages
- [ ] Log errors for debugging

### Step 7: Testing ✅
- [ ] Test login with valid credentials
- [ ] Test login with invalid credentials
- [ ] Test protected route access
- [ ] Test logout functionality
- [ ] Test token persistence on refresh
- [ ] Test 401 redirect

---

## 🔌 API ENDPOINTS

### 1. Login
```
POST /api/auth/login
Content-Type: application/json

Request:
{
  "email": "user@example.com",
  "password": "password123"
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "message": "Login Successful",
  "userId": 123,
  "email": "user@example.com",
  "name": "John Doe"
}

Response (401 Unauthorized):
{
  "message": "Invalid email or password"
}
```

### 2. Register
```
POST /api/auth/register
Content-Type: application/json

Request:
{
  "name": "John Doe",
  "email": "john@example.com",
  "mobileNumber": "+1234567890",
  "password": "password123",
  "confirmPassword": "password123",
  "role": "CUSTOMER"  // or "VENDOR"
}

Response (201 Created):
{
  "id": 123,
  "name": "John Doe",
  "email": "john@example.com",
  "mobileNumber": "+1234567890",
  "role": "CUSTOMER",
  "message": "Registration successful."
}
```

### 3. Logout
```
POST /api/auth/logout
Authorization: Bearer {token}

Response (200 OK):
{
  "success": true,
  "message": "Logout successful."
}
```

---

## 💾 LOCAL STORAGE KEYS

After login, store these in localStorage:

```javascript
{
  "authToken": "eyJhbGciOiJIUzI1NiJ9...",  // JWT token
  "userId": "123",                          // User ID
  "userEmail": "user@example.com",          // User email
  "userName": "John Doe"                    // User name
}
```

Retrieve them:
```javascript
const token = localStorage.getItem('authToken');
const userId = localStorage.getItem('userId');
const email = localStorage.getItem('userEmail');
const name = localStorage.getItem('userName');
```

---

## 🔐 AUTHENTICATION FLOW DIAGRAM

```
┌─────────────────────────────────────────────────────────────┐
│                    USER REGISTRATION                        │
├─────────────────────────────────────────────────────────────┤
│ 1. User fills registration form                              │
│ 2. Frontend validates input                                  │
│ 3. Send POST /api/auth/register                              │
│ 4. Backend validates & creates user                          │
│ 5. Return success response                                   │
│ 6. Redirect to login page                                    │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│                      USER LOGIN                              │
├─────────────────────────────────────────────────────────────┤
│ 1. User enters email & password                              │
│ 2. Frontend sends POST /api/auth/login                       │
│ 3. Backend validates credentials                             │
│ 4. Backend generates JWT token                               │
│ 5. Return token & user info                                  │
│ 6. Frontend stores token in localStorage                     │
│ 7. Redirect to dashboard                                     │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│                   AUTHENTICATED REQUESTS                     │
├─────────────────────────────────────────────────────────────┤
│ 1. Frontend retrieves token from localStorage                │
│ 2. Add to request: Authorization: Bearer {token}             │
│ 3. Send request to protected endpoint                        │
│ 4. Backend validates token                                   │
│ 5. If valid: Process request                                 │
│ 6. If invalid/expired: Return 401 Unauthorized               │
│ 7. If 401: Clear token & redirect to login                   │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│                      USER LOGOUT                             │
├─────────────────────────────────────────────────────────────┤
│ 1. User clicks logout button                                 │
│ 2. Frontend calls POST /api/auth/logout                      │
│ 3. Backend processes logout (optional)                       │
│ 4. Frontend clears localStorage                              │
│ 5. Frontend clears any state/context                         │
│ 6. Redirect to login page                                    │
└─────────────────────────────────────────────────────────────┘
```

---

## 🛠️ COMMON PATTERNS

### Pattern 1: Guard Protected Routes
```javascript
// Before rendering dashboard
if (!localStorage.getItem('authToken')) {
  navigate('/login');
}
```

### Pattern 2: Add Token to Headers
```javascript
const headers = {
  'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
  'Content-Type': 'application/json'
};
```

### Pattern 3: Refresh Token Check
```javascript
function isTokenExpired(token) {
  const payload = JSON.parse(atob(token.split('.')[1]));
  return Date.now() >= payload.exp * 1000;
}

if (isTokenExpired(token)) {
  localStorage.removeItem('authToken');
  navigate('/login');
}
```

### Pattern 4: Global Error Handler
```javascript
if (response.status === 401) {
  localStorage.clear();
  window.location.href = '/login';
}
```

---

## 📊 TOKEN EXPIRATION

**Current Configuration:**
- Expiration Time: 24 hours (86400000 milliseconds)
- Issued At: When user logs in
- Expires At: 24 hours after login

**Token Renewal Strategy (Optional):**
1. Check token expiration before each request
2. If expiring within 5 minutes, request new token
3. Update token in localStorage
4. Continue with request

---

## 🧪 TEST CASES

### Test Case 1: Valid Login
```javascript
// Input
Email: test@example.com
Password: password123

// Expected Output
✅ Token received
✅ Stored in localStorage
✅ Redirected to dashboard
```

### Test Case 2: Invalid Credentials
```javascript
// Input
Email: test@example.com
Password: wrongpassword

// Expected Output
❌ Error message displayed
❌ Token NOT stored
❌ Remained on login page
```

### Test Case 3: Protected Route Access
```javascript
// With valid token
✅ Access dashboard
✅ Load user data
✅ Display protected content

// Without token
❌ Redirect to login
❌ Cannot access dashboard
```

### Test Case 4: Logout
```javascript
// Before logout
✅ Token in localStorage
✅ User info in localStorage

// After logout
❌ Token removed
❌ User info removed
✅ Redirected to login
```

---

## ⚠️ COMMON MISTAKES TO AVOID

### ❌ Mistake 1: Forgetting Authorization Header
```javascript
// WRONG
fetch('http://localhost:8080/api/users/profile', {
  method: 'GET'
});

// CORRECT
fetch('http://localhost:8080/api/users/profile', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

### ❌ Mistake 2: Storing Token in Cookie Without HttpOnly
```javascript
// RISKY - XSS vulnerable
document.cookie = `token=${token}`;

// BETTER - Let backend set HttpOnly cookie
```

### ❌ Mistake 3: Not Handling 401 Response
```javascript
// WRONG - Ignores unauthorized
fetch(url).then(response => response.json());

// CORRECT - Handles 401
if (response.status === 401) {
  localStorage.removeItem('authToken');
  navigate('/login');
}
```

### ❌ Mistake 4: Checking Auth Only on Page Load
```javascript
// WRONG - Only checks once
useEffect(() => {
  if (!token) navigate('/login');
}, []);

// BETTER - Create auth guard on every navigation
```

### ❌ Mistake 5: Storing Sensitive Data in localStorage
```javascript
// WRONG - Never store password
localStorage.setItem('password', password);

// CORRECT - Only store token
localStorage.setItem('authToken', token);
```

---

## 🔗 CORS CONFIGURATION

Your backend CORS is already configured. If you get CORS errors:

```java
// In CorsConfig.java - Verify this is set:
corsRegistry.addMapping("/api/**")
    .allowedOrigins("http://localhost:3000")  // Your frontend URL
    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    .allowedHeaders("*")
    .allowCredentials(true);
```

---

## 📱 MOBILE (FLUTTER) SPECIFIC

### Store Token in Flutter
```dart
import 'package:shared_preferences/shared_preferences.dart';

final prefs = await SharedPreferences.getInstance();
await prefs.setString('authToken', token);
```

### Retrieve Token
```dart
final token = prefs.getString('authToken');
```

### Add to HTTP Request
```dart
final response = await http.get(
  Uri.parse(url),
  headers: {
    'Authorization': 'Bearer $token',
    'Content-Type': 'application/json',
  },
);
```

---

## 🌐 ENVIRONMENT SETUP

### React (.env)
```env
REACT_APP_API_BASE_URL=http://localhost:8080/api
REACT_APP_TOKEN_EXPIRY_WARNING=300000
```

### Vue (.env.local)
```env
VITE_API_BASE_URL=http://localhost:8080/api
```

### Angular (environment.ts)
```typescript
export const environment = {
  apiUrl: 'http://localhost:8080/api'
};
```

### Next.js (.env.local)
```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api
```

---

## 🚨 DEBUGGING TIPS

### Check Token in Console
```javascript
// View token
console.log(localStorage.getItem('authToken'));

// Decode token (without validation)
function decodeToken(token) {
  return JSON.parse(atob(token.split('.')[1]));
}
console.log(decodeToken(token));
```

### Check Network Requests
1. Open Developer Tools (F12)
2. Go to Network tab
3. Look for auth/login request
4. Check Response tab for token
5. Check Request headers for Authorization

### Debug 401 Errors
```javascript
// Check if token exists
console.log('Token:', localStorage.getItem('authToken'));

// Check if token is valid
const decoded = decodeToken(token);
console.log('Expires:', new Date(decoded.exp * 1000));

// Check if Authorization header is sent
// (View in Network tab)
```

---

## ✅ DEPLOYMENT CHECKLIST

- [ ] Update API_BASE_URL to production backend
- [ ] Use HTTPS in production
- [ ] Set secure cookie flags if using cookies
- [ ] Implement CSRF protection if needed
- [ ] Add request timeout handling
- [ ] Implement retry logic for failed requests
- [ ] Add loading indicators for UX
- [ ] Test all auth flows in production
- [ ] Monitor and log auth errors
- [ ] Set up error tracking (e.g., Sentry)
- [ ] Implement refresh token mechanism (if needed)
- [ ] Test across different browsers
- [ ] Test on mobile devices
- [ ] Verify CORS settings in production

---

## 📞 TROUBLESHOOTING

### Issue: "Network Error" on login
**Solution:** Check if backend is running on http://localhost:8080

### Issue: CORS Error
**Solution:** Verify CorsConfig in backend matches your frontend URL

### Issue: Token not persisting on refresh
**Solution:** Use localStorage.getItem() in useEffect to restore token

### Issue: 401 errors on protected routes
**Solution:** Ensure Authorization header is being sent with correct format

### Issue: Infinite redirect loop
**Solution:** Check that ProtectedRoute doesn't redirect authenticated users

---

Created: August 2, 2026
Backend Version: Spring Boot with JWT
Frontend: Universal Integration Guide
