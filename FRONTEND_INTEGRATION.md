# Frontend Integration Guide - JWT Token Authentication

## 1. LOGIN API REQUEST & RESPONSE

### Request
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

### Response (Success - 200 OK)
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwidXNlcklkIjoxMjMsInJvbGUiOiJDVVNUT01FUiIsImlhdCI6MTcyMjYyNDAwMCwiZXhwIjoxNzIyNzEwNDAwfQ.signature",
  "message": "Login Successful",
  "userId": 123,
  "email": "user@example.com",
  "name": "John Doe"
}
```

---

## 2. STORING THE TOKEN

### Option A: Using LocalStorage (Persistent)
```javascript
// After successful login
const loginResponse = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    email: email,
    password: password
  })
});

const data = await loginResponse.json();

// Store token in localStorage
localStorage.setItem('authToken', data.token);
localStorage.setItem('userId', data.userId);
localStorage.setItem('userRole', data.role); // Note: May need to decode token or add role to response
localStorage.setItem('userEmail', data.email);
localStorage.setItem('userName', data.name);

// Redirect to dashboard
window.location.href = '/dashboard';
```

### Option B: Using SessionStorage (Session-only)
```javascript
// Store token in sessionStorage (cleared when browser closes)
sessionStorage.setItem('authToken', data.token);
sessionStorage.setItem('userId', data.userId);
sessionStorage.setItem('userEmail', data.email);
```

---

## 3. RETRIEVING & USING THE TOKEN

### Get Token
```javascript
const token = localStorage.getItem('authToken');
```

### Send Token with API Requests
```javascript
// Example: Making authenticated API call
const response = await fetch('http://localhost:8080/api/users/profile', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
```

---

## 4. COMPLETE LOGIN FLOW EXAMPLE (JavaScript/React)

### JavaScript Vanilla
```javascript
class AuthService {
  static async login(email, password) {
    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ email, password })
      });

      if (!response.ok) {
        throw new Error('Login failed');
      }

      const data = await response.json();

      // Store token and user info
      localStorage.setItem('authToken', data.token);
      localStorage.setItem('userId', data.userId);
      localStorage.setItem('userEmail', data.email);
      localStorage.setItem('userName', data.name);

      return data;
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  }

  static getToken() {
    return localStorage.getItem('authToken');
  }

  static isAuthenticated() {
    return !!localStorage.getItem('authToken');
  }

  static logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userName');
  }

  static getAuthHeaders() {
    const token = this.getToken();
    return {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    };
  }
}

// Usage
async function handleLogin() {
  try {
    const data = await AuthService.login('user@example.com', 'password123');
    console.log('Login successful:', data);
    // Redirect to dashboard
    window.location.href = '/dashboard';
  } catch (error) {
    console.error('Login failed:', error);
    alert('Invalid email or password');
  }
}
```

### React Example
```jsx
import React, { useState } from 'react';

function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ email, password })
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Login failed');
      }

      const data = await response.json();

      // Store token
      localStorage.setItem('authToken', data.token);
      localStorage.setItem('userId', data.userId);
      localStorage.setItem('userEmail', data.email);
      localStorage.setItem('userName', data.name);

      // Redirect or update state
      window.location.href = '/dashboard';
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleLogin}>
      <input
        type="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="Email"
        required
      />
      <input
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder="Password"
        required
      />
      <button type="submit" disabled={loading}>
        {loading ? 'Logging in...' : 'Login'}
      </button>
      {error && <p style={{ color: 'red' }}>{error}</p>}
    </form>
  );
}

export default LoginPage;
```

---

## 5. INTERCEPTING ALL API REQUESTS (Axios Example)

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api'
});

// Add token to all requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle unauthorized responses
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid
      localStorage.removeItem('authToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

---

## 6. LOGOUT FLOW

```javascript
async function handleLogout() {
  try {
    // Call backend logout endpoint
    const token = localStorage.getItem('authToken');
    await fetch('http://localhost:8080/api/auth/logout', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
  } catch (error) {
    console.error('Logout error:', error);
  } finally {
    // Clear local storage
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userName');

    // Redirect to login
    window.location.href = '/login';
  }
}
```

---

## 7. PROTECTING ROUTES (React Router Example)

```jsx
import React from 'react';
import { Navigate } from 'react-router-dom';

function PrivateRoute({ children }) {
  const token = localStorage.getItem('authToken');

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  return children;
}

export default PrivateRoute;

// Usage in your routing:
// <PrivateRoute>
//   <Dashboard />
// </PrivateRoute>
```

---

## 8. TOKEN STRUCTURE (JWT Payload)

When you decode the token, you'll see:
```json
{
  "sub": "user@example.com",      // Subject (email)
  "userId": 123,                   // User ID
  "role": "CUSTOMER",              // User Role (from token claim)
  "iat": 1722624000,               // Issued At
  "exp": 1722710400                // Expiration (24 hours later)
}
```

To decode in frontend:
```javascript
function decodeToken(token) {
  const base64Url = token.split('.')[1];
  const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
  const jsonPayload = decodeURIComponent(
    atob(base64).split('').map((c) => {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join('')
  );
  return JSON.parse(jsonPayload);
}

const tokenPayload = decodeToken(localStorage.getItem('authToken'));
console.log('User Role:', tokenPayload.role);
console.log('User ID:', tokenPayload.userId);
```

---

## 9. ENVIRONMENT CONFIGURATION

### .env file
```
REACT_APP_API_BASE_URL=http://localhost:8080/api
REACT_APP_TOKEN_EXPIRY_WARNING=300000  // Warn 5 mins before expiry
```

### Usage
```javascript
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

fetch(`${API_BASE_URL}/auth/login`, {
  // ...
});
```

---

## 10. BEST PRACTICES ✅

✅ **DO:**
- Store token in localStorage or sessionStorage
- Send token in Authorization header: `Bearer {token}`
- Handle 401 responses by redirecting to login
- Clear token on logout
- Use HTTPS in production
- Implement token refresh mechanism if needed
- Set appropriate CORS in backend

❌ **DON'T:**
- Store token in cookies without HttpOnly flag
- Send token as query parameter
- Store sensitive data in localStorage (only token)
- Forget to add Authorization header
- Keep expired tokens

---

## 11. CORS CONFIGURATION REMINDER

Your backend already has CORS enabled. Make sure your frontend URL matches:

In your `CorsConfig.java`:
```java
// Add your frontend URL
corsRegistry.addMapping("/api/**")
    .allowedOrigins("http://localhost:3000") // React dev server
    .allowedMethods("GET", "POST", "PUT", "DELETE")
    .allowedHeaders("*")
    .allowCredentials(true);
```

---

## 12. SAMPLE COMPLETE LOGIN COMPONENT

```jsx
import React, { useState } from 'react';

function LoginComponent() {
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || 'Login failed');
      }

      // Store credentials
      localStorage.setItem('authToken', data.token);
      localStorage.setItem('userId', data.userId);
      localStorage.setItem('userEmail', data.email);
      localStorage.setItem('userName', data.name);

      // Redirect
      window.location.href = '/dashboard';
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <h2>Login</h2>
      <form onSubmit={handleSubmit}>
        <div>
          <label>Email:</label>
          <input
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label>Password:</label>
          <input
            type="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            required
          />
        </div>
        <button type="submit" disabled={loading}>
          {loading ? 'Logging in...' : 'Login'}
        </button>
        {error && <p className="error">{error}</p>}
      </form>
    </div>
  );
}

export default LoginComponent;
```

---

## Quick Start Checklist

- [ ] User logs in with email & password
- [ ] Frontend receives JWT token in response
- [ ] Token stored in localStorage
- [ ] Token sent in Authorization header for protected endpoints
- [ ] 401 errors trigger logout & redirect to login
- [ ] Logout clears localStorage
- [ ] Protected routes check for token existence

---

**Token Expiration:** 24 hours (86400000ms)
**Token Type:** JWT with HS256 signature
**Backend API Base:** http://localhost:8080/api
