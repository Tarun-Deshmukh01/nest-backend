// ============================================
// REACT - Auth Service & Hooks
// ============================================

// authService.js
export class AuthService {
  static API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

  static async login(email, password) {
    const response = await fetch(`${this.API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Login failed');
    }

    const data = await response.json();

    // Store user data
    localStorage.setItem('authToken', data.token);
    localStorage.setItem('userId', data.userId);
    localStorage.setItem('userEmail', data.email);
    localStorage.setItem('userName', data.name);

    return data;
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
// REACT - useAuth Hook
// ============================================

// useAuth.js
import { useState, useCallback, useEffect } from 'react';
import { AuthService } from './authService';

export function useAuth() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const userInfo = AuthService.getUserInfo();
    if (userInfo.token) {
      setUser(userInfo);
    }
    setLoading(false);
  }, []);

  const login = useCallback(async (email, password) => {
    setLoading(true);
    setError(null);
    try {
      const data = await AuthService.login(email, password);
      setUser(data);
      return data;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const register = useCallback(async (registerData) => {
    setLoading(true);
    setError(null);
    try {
      const data = await AuthService.register(registerData);
      return data;
    } catch (err) {
      setError(err.message);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  const logout = useCallback(async () => {
    setLoading(true);
    try {
      await AuthService.logout();
      setUser(null);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    user,
    loading,
    error,
    login,
    register,
    logout,
    isAuthenticated: AuthService.isAuthenticated()
  };
}

// ============================================
// REACT - Protected Route Component
// ============================================

// ProtectedRoute.jsx
import React from 'react';
import { Navigate } from 'react-router-dom';
import { AuthService } from './authService';

function ProtectedRoute({ children }) {
  if (!AuthService.isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

export default ProtectedRoute;

// ============================================
// REACT - Login Component Example
// ============================================

// LoginPage.jsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

function LoginPage() {
  const navigate = useNavigate();
  const { login, loading, error } = useAuth();
  const [formData, setFormData] = useState({ email: '', password: '' });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await login(formData.email, formData.password);
      navigate('/dashboard');
    } catch (err) {
      console.error('Login failed:', err);
    }
  };

  return (
    <div className="login-container">
      <h2>Login</h2>
      {error && <div className="error-message">{error}</div>}
      <form onSubmit={handleSubmit}>
        <input
          type="email"
          name="email"
          placeholder="Email"
          value={formData.email}
          onChange={handleChange}
          required
        />
        <input
          type="password"
          name="password"
          placeholder="Password"
          value={formData.password}
          onChange={handleChange}
          required
        />
        <button type="submit" disabled={loading}>
          {loading ? 'Loading...' : 'Login'}
        </button>
      </form>
    </div>
  );
}

export default LoginPage;

// ============================================
// AXIOS - Interceptor Setup
// ============================================

// axiosInstance.js
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

// Response interceptor - Handle 401 errors
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      AuthService.logout();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;

// ============================================
// FETCH API - Wrapper Function
// ============================================

// api.js
import { AuthService } from './authService';

export async function apiCall(endpoint, options = {}) {
  const url = `${process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api'}${endpoint}`;
  
  const config = {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
      'Authorization': `Bearer ${AuthService.getToken()}`
    }
  };

  const response = await fetch(url, config);

  if (response.status === 401) {
    AuthService.logout();
    window.location.href = '/login';
    return;
  }

  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || 'API request failed');
  }

  return response.json();
}

// Usage: 
// const data = await apiCall('/users/profile', { method: 'GET' });

// ============================================
// REACT CONTEXT - Auth Provider
// ============================================

// AuthContext.js
import React, { createContext, useContext, useEffect, useState } from 'react';
import { AuthService } from './authService';

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const userInfo = AuthService.getUserInfo();
    if (userInfo.token) {
      setUser(userInfo);
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    const data = await AuthService.login(email, password);
    setUser(data);
    return data;
  };

  const logout = async () => {
    await AuthService.logout();
    setUser(null);
  };

  const value = {
    user,
    loading,
    login,
    logout,
    isAuthenticated: !!user?.token
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuthContext() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuthContext must be used within AuthProvider');
  }
  return context;
}

// ============================================
// VUE.JS - Auth Service
// ============================================

// authService.js (Vue)
export const authService = {
  API_BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',

  async login(email, password) {
    const response = await fetch(`${this.API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });

    if (!response.ok) throw new Error('Login failed');

    const data = await response.json();
    localStorage.setItem('authToken', data.token);
    localStorage.setItem('userId', data.userId);
    localStorage.setItem('userEmail', data.email);
    localStorage.setItem('userName', data.name);
    return data;
  },

  logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userName');
  },

  getToken() {
    return localStorage.getItem('authToken');
  },

  isAuthenticated() {
    return !!localStorage.getItem('authToken');
  }
};

// ============================================
// ANGULAR - Auth Service
// ============================================

// auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  private userSubject = new BehaviorSubject(null);
  public user$ = this.userSubject.asObservable();

  constructor(private http: HttpClient) {
    const userInfo = this.getUserInfo();
    if (userInfo?.token) {
      this.userSubject.next(userInfo);
    }
  }

  login(email: string, password: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, { email, password })
      .pipe(
        tap((data: any) => {
          localStorage.setItem('authToken', data.token);
          localStorage.setItem('userId', data.userId);
          localStorage.setItem('userEmail', data.email);
          localStorage.setItem('userName', data.name);
          this.userSubject.next(data);
        })
      );
  }

  logout() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userName');
    this.userSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem('authToken');
  }

  isAuthenticated(): boolean {
    return !!localStorage.getItem('authToken');
  }

  getUserInfo() {
    return {
      userId: localStorage.getItem('userId'),
      email: localStorage.getItem('userEmail'),
      name: localStorage.getItem('userName'),
      token: this.getToken()
    };
  }
}

// auth.interceptor.ts
import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    
    if (token) {
      req = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(req);
  }
}

// ============================================
// FLUTTER - Dart Auth Service
// ============================================

// auth_service.dart
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';

class AuthService {
  static const String apiUrl = 'http://localhost:8080/api';

  static Future<Map<String, dynamic>> login(String email, String password) async {
    final response = await http.post(
      Uri.parse('$apiUrl/auth/login'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'email': email, 'password': password}),
    );

    if (response.statusCode != 200) {
      throw Exception('Login failed');
    }

    final data = jsonDecode(response.body);
    
    // Store token
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('authToken', data['token']);
    await prefs.setString('userId', data['userId'].toString());
    await prefs.setString('userEmail', data['email']);
    await prefs.setString('userName', data['name']);

    return data;
  }

  static Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('authToken');
  }

  static Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('authToken');
    await prefs.remove('userId');
    await prefs.remove('userEmail');
    await prefs.remove('userName');
  }

  static Future<bool> isAuthenticated() async {
    final token = await getToken();
    return token != null;
  }
}

// ============================================
// NEXTJS - Auth Service & Middleware
// ============================================

// lib/authService.ts (Next.js)
export class AuthService {
  private static API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api';

  static async login(email: string, password: string) {
    const response = await fetch(`${this.API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });

    if (!response.ok) throw new Error('Login failed');

    const data = await response.json();
    
    // Store in cookies (can be accessed by middleware)
    document.cookie = `authToken=${data.token}; path=/`;
    localStorage.setItem('userId', data.userId);
    localStorage.setItem('userEmail', data.email);

    return data;
  }

  static logout() {
    document.cookie = 'authToken=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC;';
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
  }
}

// middleware.ts (Next.js)
import { NextRequest, NextResponse } from 'next/server';

export function middleware(request: NextRequest) {
  const token = request.cookies.get('authToken')?.value;

  if (!token && request.nextUrl.pathname.startsWith('/dashboard')) {
    return NextResponse.redirect(new URL('/login', request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ['/dashboard/:path*', '/profile/:path*']
};
