// ============================================
// UPDATED Frontend Code - Token from Headers
// ============================================

// authService.js (UPDATED)
export class AuthService {
  static API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

  static async login(email, password) {
    try {
      const response = await fetch(`${this.API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ email, password })
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'Login failed');
      }

      // ⭐ KEY CHANGE: Get token from response headers
      const authHeader = response.headers.get('Authorization');
      if (!authHeader) {
        throw new Error('No token received from server');
      }

      const token = authHeader.replace('Bearer ', '');
      const data = await response.json();

      // Store token and user info
      localStorage.setItem('authToken', token);
      localStorage.setItem('userId', data.userId);
      localStorage.setItem('userEmail', data.email);
      localStorage.setItem('userName', data.name);

      // Return data with token for debugging
      return {
        ...data,
        token: token // Add token to returned object for convenience
      };
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  }

  static async register(registerData) {
    const response = await fetch(`${this.API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(registerData)
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Registration failed');
    }

    return await response.json();
  }

  static async logout() {
    try {
      await fetch(`${this.API_BASE_URL}/auth/logout`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${this.getToken()}`,
          'Content-Type': 'application/json'
        }
      });
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      localStorage.removeItem('authToken');
      localStorage.removeItem('userId');
      localStorage.removeItem('userEmail');
      localStorage.removeItem('userName');
    }
  }

  static getToken() {
    return localStorage.getItem('authToken');
  }

  static isAuthenticated() {
    return !!localStorage.getItem('authToken');
  }

  static getUserInfo() {
    return {
      userId: localStorage.getItem('userId'),
      email: localStorage.getItem('userEmail'),
      name: localStorage.getItem('userName'),
      token: this.getToken()
    };
  }

  static getAuthHeaders() {
    return {
      'Authorization': `Bearer ${this.getToken()}`,
      'Content-Type': 'application/json'
    };
  }
}

// ============================================
// REACT - Updated Login Component
// ============================================

// LoginPage.jsx (UPDATED)
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

function LoginPage() {
  const navigate = useNavigate();
  const { login, loading, error } = useAuth();
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [debugInfo, setDebugInfo] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const data = await login(formData.email, formData.password);
      
      // Debug: Log received data
      console.log('Login successful:', data);
      console.log('Token stored:', localStorage.getItem('authToken'));
      
      setDebugInfo('✅ Login successful! Redirecting...');
      
      // Redirect after short delay to show success message
      setTimeout(() => {
        navigate('/dashboard');
      }, 1000);
    } catch (err) {
      console.error('Login failed:', err);
      setDebugInfo(`❌ Login failed: ${err.message}`);
    }
  };

  return (
    <div className="login-container">
      <h2>Login</h2>
      {error && <div className="error-message">{error}</div>}
      {debugInfo && <div className="debug-info">{debugInfo}</div>}
      
      <form onSubmit={handleSubmit}>
        <div>
          <label>Email:</label>
          <input
            type="email"
            name="email"
            placeholder="Email"
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
            placeholder="Password"
            value={formData.password}
            onChange={handleChange}
            required
          />
        </div>
        <button type="submit" disabled={loading}>
          {loading ? 'Logging in...' : 'Login'}
        </button>
      </form>

      {/* Debug Info */}
      <div style={{ marginTop: '20px', fontSize: '12px', color: '#666' }}>
        <p>Debug Info:</p>
        <p>Token in storage: {localStorage.getItem('authToken') ? '✅ Yes' : '❌ No'}</p>
        <p>User email: {localStorage.getItem('userEmail') || 'Not set'}</p>
        <p>User ID: {localStorage.getItem('userId') || 'Not set'}</p>
      </div>
    </div>
  );
}

export default LoginPage;

// ============================================
// AXIOS - Updated Interceptor
// ============================================

// axiosInstance.js (UPDATED)
import axios from 'axios';
import { AuthService } from './authService';

const axiosInstance = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api'
});

// Request interceptor - Add token to all requests
axiosInstance.interceptors.request.use(
  (config) => {
    const token = AuthService.getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor - Handle responses and 401 errors
axiosInstance.interceptors.response.use(
  (response) => {
    // If login response, extract token from headers
    if (response.config.url.includes('auth/login')) {
      const authHeader = response.headers['authorization'];
      if (authHeader) {
        const token = authHeader.replace('Bearer ', '');
        localStorage.setItem('authToken', token);
        console.log('Token extracted from login response header');
      }
    }
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      console.log('Unauthorized (401) - Logging out');
      AuthService.logout();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;

// ============================================
// VANILLA JAVASCRIPT - Complete Implementation
// ============================================

// script.js
const API_BASE_URL = 'http://localhost:8080/api';

async function handleLogin() {
  const email = document.getElementById('email').value;
  const password = document.getElementById('password').value;

  try {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ email, password })
    });

    // Log full response for debugging
    console.log('Response Status:', response.status);
    console.log('Response Headers:', response.headers);
    console.log('Authorization Header:', response.headers.get('Authorization'));

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Login failed');
    }

    // ⭐ Extract token from response headers
    const authHeader = response.headers.get('Authorization');
    if (!authHeader) {
      throw new Error('No authorization header in response');
    }

    const token = authHeader.replace('Bearer ', '');
    const data = await response.json();

    // Store in localStorage
    localStorage.setItem('authToken', token);
    localStorage.setItem('userId', data.userId);
    localStorage.setItem('userEmail', data.email);
    localStorage.setItem('userName', data.name);

    console.log('✅ Login successful!');
    console.log('Token:', token);
    console.log('User:', data);

    // Redirect to dashboard
    window.location.href = '/dashboard';
  } catch (error) {
    console.error('❌ Login error:', error);
    alert('Login failed: ' + error.message);
  }
}

// ============================================
// FETCH WRAPPER WITH HEADER TOKEN EXTRACTION
// ============================================

async function apiLoginCall(endpoint, credentials) {
  const url = `${API_BASE_URL}${endpoint}`;
  
  const response = await fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(credentials)
  });

  // Extract token from response header for login endpoint
  if (endpoint === '/auth/login') {
    const authHeader = response.headers.get('Authorization');
    if (authHeader) {
      const token = authHeader.replace('Bearer ', '');
      localStorage.setItem('authToken', token);
    }
  }

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'Request failed');
  }

  return response.json();
}

// Usage:
// const data = await apiLoginCall('/auth/login', { email, password });

// ============================================
// DEBUG: How to Check Token in Browser
// ============================================

// Open browser console (F12) and run these commands:

// Check localStorage
// localStorage.getItem('authToken')

// Decode token to see its content
// function decodeToken(token) {
//   const parts = token.split('.');
//   if (parts.length !== 3) return null;
//   const decoded = atob(parts[1]);
//   return JSON.parse(decoded);
// }
// console.log(decodeToken(localStorage.getItem('authToken')))

// Check all stored auth data
// console.log({
//   token: localStorage.getItem('authToken'),
//   userId: localStorage.getItem('userId'),
//   email: localStorage.getItem('userEmail'),
//   name: localStorage.getItem('userName')
// })

// ============================================
// TESTING: cURL Command to Test Login
// ============================================

// curl -X POST http://localhost:8080/api/auth/login \
//   -H "Content-Type: application/json" \
//   -d '{"email":"tarun@gmail.com","password":"password"}' \
//   -v

// Look for: Authorization: Bearer eyJ... in response headers
