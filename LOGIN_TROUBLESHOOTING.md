# Login Troubleshooting Guide

## What I See From Your Response Headers:

✅ **Backend is working correctly!**
- Status: `200 OK` 
- Token present in header: `Authorization: Bearer eyJ...`
- CORS properly configured: `access-control-expose-headers: Authorization, Content-Type`

## The Issue: Frontend Not Reading Token from Headers

Your frontend code needs to extract the token from **response headers**, not the response body.

---

## 🔍 DEBUGGING STEPS

### Step 1: Open Browser Console & Check Network
1. Open DevTools: **F12**
2. Go to **Network** tab
3. Login
4. Click on the login request
5. Check **Response Headers** tab

**You should see:**
```
authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Step 2: Check if Token is Stored
Open console and run:
```javascript
localStorage.getItem('authToken')
```

**Should output:** The JWT token starting with `eyJ...`

**If it outputs `null`:** Frontend is not reading the header correctly ❌

### Step 3: Decode & Verify Token
Run in console:
```javascript
function decodeToken(token) {
  const parts = token.split('.');
  if (parts.length !== 3) return null;
  const payload = atob(parts[1]);
  return JSON.parse(payload);
}

const token = localStorage.getItem('authToken');
console.log(decodeToken(token));
```

**Expected output:**
```javascript
{
  sub: "tarun@gmail.com",
  userId: 1,
  role: "CUSTOMER",
  iat: 1785695692,
  exp: 1785782092
}
```

---

## 🛠️ QUICK FIX - Copy This Code

### If Using Fetch API:

```javascript
// ❌ WRONG (token not in response body anymore)
const response = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
});
const data = await response.json();
const token = data.token;  // ❌ This will be undefined!

// ✅ CORRECT (token in response headers)
const response = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
});

// Get token from headers
const token = response.headers.get('Authorization').replace('Bearer ', '');
localStorage.setItem('authToken', token);

// Get other data from body
const data = await response.json();
localStorage.setItem('userId', data.userId);
localStorage.setItem('userEmail', data.email);
localStorage.setItem('userName', data.name);
```

### If Using Axios:

```javascript
// ❌ WRONG
const { data } = await axios.post('http://localhost:8080/api/auth/login', { email, password });
const token = data.token;  // ❌ Undefined

// ✅ CORRECT
const response = await axios.post('http://localhost:8080/api/auth/login', { email, password });
const token = response.headers['authorization'].replace('Bearer ', '');
localStorage.setItem('authToken', token);
```

---

## 📋 Response Format Reference

### What You're Getting Now:

**Response Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
```

**Response Body:**
```json
{
  "message": "Login Successful",
  "userId": 1,
  "email": "tarun@gmail.com",
  "name": "Tarun Kumar"
}
```

**NOT** in response body anymore:
```json
{
  "token": "..."  // ❌ This is now hidden with @JsonIgnore
}
```

---

## ✅ Complete Login Implementation

Choose your framework:

### React (with Hooks)
```jsx
import { useState } from 'react';

function LoginPage() {
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: 'tarun@gmail.com',
          password: 'password123'
        })
      });

      if (!response.ok) throw new Error('Login failed');

      // Extract token from headers
      const token = response.headers.get('Authorization').replace('Bearer ', '');
      const data = await response.json();

      // Store in localStorage
      localStorage.setItem('authToken', token);
      localStorage.setItem('userId', data.userId);
      localStorage.setItem('userEmail', data.email);
      localStorage.setItem('userName', data.name);

      console.log('✅ Login successful!');
      window.location.href = '/dashboard';
    } catch (error) {
      console.error('❌ Error:', error);
      alert('Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleLogin}>
      <input type="email" placeholder="Email" required />
      <input type="password" placeholder="Password" required />
      <button disabled={loading}>{loading ? 'Loading...' : 'Login'}</button>
    </form>
  );
}

export default LoginPage;
```

### Vue 3
```vue
<template>
  <form @submit.prevent="handleLogin">
    <input v-model="email" type="email" placeholder="Email" required />
    <input v-model="password" type="password" placeholder="Password" required />
    <button :disabled="loading">{{ loading ? 'Loading...' : 'Login' }}</button>
  </form>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const email = ref('');
const password = ref('');
const loading = ref(false);

async function handleLogin() {
  loading.value = true;
  try {
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: email.value, password: password.value })
    });

    if (!response.ok) throw new Error('Login failed');

    const token = response.headers.get('Authorization').replace('Bearer ', '');
    const data = await response.json();

    localStorage.setItem('authToken', token);
    localStorage.setItem('userId', data.userId);
    localStorage.setItem('userEmail', data.email);
    localStorage.setItem('userName', data.name);

    router.push('/dashboard');
  } catch (error) {
    console.error('Login error:', error);
  } finally {
    loading.value = false;
  }
}
</script>
```

### Angular
```typescript
import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  template: `
    <form (ngSubmit)="onLogin()">
      <input type="email" [(ngModel)]="email" name="email" required />
      <input type="password" [(ngModel)]="password" name="password" required />
      <button [disabled]="loading">{{ loading ? 'Loading...' : 'Login' }}</button>
    </form>
  `
})
export class LoginComponent {
  email = '';
  password = '';
  loading = false;

  constructor(private http: HttpClient, private router: Router) {}

  onLogin() {
    this.loading = true;
    this.http.post<any>('http://localhost:8080/api/auth/login', {
      email: this.email,
      password: this.password
    }).subscribe({
      next: (data) => {
        // Note: In Angular HttpClient, response headers are accessible differently
        // You may need to use { observe: 'response' } to get full response
        localStorage.setItem('userId', data.userId);
        localStorage.setItem('userEmail', data.email);
        localStorage.setItem('userName', data.name);
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        console.error('Login error:', error);
        alert('Login failed');
      },
      complete: () => this.loading = false
    });
  }
}

// Better version with observe: 'response'
onLogin() {
  this.loading = true;
  this.http.post<any>('http://localhost:8080/api/auth/login',
    { email: this.email, password: this.password },
    { observe: 'response' }
  ).subscribe({
    next: (response) => {
      const token = response.headers.get('Authorization')?.replace('Bearer ', '');
      if (token) localStorage.setItem('authToken', token);
      
      const data = response.body;
      localStorage.setItem('userId', data.userId);
      localStorage.setItem('userEmail', data.email);
      localStorage.setItem('userName', data.name);
      
      this.router.navigate(['/dashboard']);
    },
    error: () => alert('Login failed'),
    complete: () => this.loading = false
  });
}
```

---

## 🧪 Test with cURL

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"tarun@gmail.com","password":"password123"}' \
  -v
```

**Look for in output:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## ❌ Common Mistakes

### Mistake 1: Looking for token in response body
```javascript
const data = await response.json();
const token = data.token;  // ❌ Will be undefined with @JsonIgnore
```

### Mistake 2: Not replacing "Bearer " prefix
```javascript
const token = response.headers.get('Authorization');  // Returns "Bearer eyJ..."
const tokenOnly = token.replace('Bearer ', '');  // Now just "eyJ..."
```

### Mistake 3: Not storing in localStorage
```javascript
// ❌ Wrong - token only in memory, lost on refresh
let token = response.headers.get('Authorization');

// ✅ Correct - persistent across page refreshes
localStorage.setItem('authToken', token);
```

### Mistake 4: Forgetting to handle CORS
Your backend has CORS configured, but make sure the header is exposed.
Verify `access-control-expose-headers` includes `Authorization`:
```
access-control-expose-headers: Authorization, Content-Type
```

---

## ✅ Verification Checklist

- [ ] Backend returns 200 OK on login
- [ ] Authorization header present in response
- [ ] Frontend extracts token from header
- [ ] Token stored in localStorage
- [ ] Can retrieve token from console: `localStorage.getItem('authToken')`
- [ ] Decoded token has correct claims (userId, role, email)
- [ ] Token sent in subsequent API calls: `Authorization: Bearer {token}`
- [ ] Can access protected endpoints
- [ ] 401 errors trigger logout & redirect

---

## Still Not Working?

1. **Check browser console (F12)** for errors
2. **Check Network tab** - see actual response headers
3. **Run the debug commands** from Step 2 above
4. **Check localStorage** for stored token
5. **Verify CORS** - your backend config includes Authorization header

If you still have issues, share:
- Your exact frontend code
- What error you see in browser console
- What you see in Network > Response Headers
