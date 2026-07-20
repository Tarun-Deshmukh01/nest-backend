# Database Schema Migration Guide

## Problem
The User entity was changed from `firstName`/`lastName` to a single `name` field, but the database still has the old columns:
- ❌ Missing: `name` column
- ✅ Existing: `first_name` and `last_name` columns

## Solution

### Option 1: Let Hibernate Auto-Create the Column (Recommended)

Since `spring.jpa.hibernate.ddl-auto=update` is enabled, Hibernate will automatically add the missing `name` column on app restart.

**Steps:**
1. Restart your Spring Boot application
2. Hibernate will add the `name` column automatically
3. For existing user records, run the SQL below to migrate data

### Option 2: Manual SQL Migration

If you have existing users in the database, run this SQL to migrate data:

```sql
-- Add the new name column if it doesn't exist
ALTER TABLE users ADD COLUMN IF NOT EXISTS name VARCHAR(255);

-- Migrate existing data from first_name + last_name to name
UPDATE users 
SET name = CONCAT(first_name, ' ', last_name) 
WHERE name IS NULL AND first_name IS NOT NULL;

-- Verify the migration
SELECT id, username, email, first_name, last_name, name FROM users;
```

### Option 3: Fresh Start (If No Important Data)

If you're still in development and have no important data:

```sql
-- Drop and recreate the users table
DROP TABLE IF EXISTS users CASCADE;

-- Let Hibernate recreate it on app restart
-- Spring Boot will restart and auto-create with the correct schema
```

## Next Steps

1. **Restart your Spring Boot application:**
   ```bash
   cd /Users/tarunkumar/Documents/springboot/nest-backend
   ./mvnw spring-boot:run
   ```

2. **Test the login/register API again:**
   ```bash
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{
       "name": "John Doe",
       "email": "john.doe@example.com",
       "password": "SecurePass123!",
       "confirmPassword": "SecurePass123!",
       "securityCode": "1234",
       "role": "CUSTOMER"
     }'
   ```

3. **Verify in database:**
   ```bash
   psql -U tarunkumar -d nest -c "SELECT id, username, email, name FROM users LIMIT 5;"
   ```

## Verification

After restart, the `users` table should look like:

| id | username | email | name | password | security_code | role | active |
|----|----------|-------|------|----------|---------------|------|--------|
| 1 | tarun123456_123456 | tarun123456@gmail.com | Tarun Kumar | (hashed) | 1234 | CUSTOMER | true |

If you see the error again after restart, run the SQL migration commands above.
