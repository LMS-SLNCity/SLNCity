# 🗄️ PostgreSQL Database Setup Guide

## 📋 Overview

This guide provides comprehensive instructions for setting up PostgreSQL database for the Lab Operations Management System. The application uses PostgreSQL with JSON/JSONB support for flexible data storage.

## 🎯 Quick Start

### Prerequisites
- PostgreSQL 12+ installed
- Administrative access to PostgreSQL server
- Java 17+ for running the application

### Basic Setup Commands
```bash
# 1. Create database
psql -U postgres -c "CREATE DATABASE lab_operations;"

# 2. Create user
psql -U postgres -c "CREATE USER lab_user WITH PASSWORD 'your_secure_password';"

# 3. Grant permissions
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE lab_operations TO lab_user;"

# 4. Test connection
psql -U lab_user -d lab_operations -c "SELECT current_database();"
```

## 🔧 Detailed Installation

### 1. PostgreSQL Installation

#### macOS (Homebrew)
```bash
# Install PostgreSQL
brew install postgresql@15

# Start PostgreSQL service
brew services start postgresql@15

# Connect as superuser
psql postgres
```

#### Ubuntu/Debian
```bash
# Update package list
sudo apt update

# Install PostgreSQL
sudo apt install postgresql postgresql-contrib

# Start PostgreSQL service
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Connect as postgres user
sudo -u postgres psql
```

#### Windows
1. Download PostgreSQL installer from [postgresql.org](https://www.postgresql.org/download/windows/)
2. Run installer and follow setup wizard
3. Remember the password for `postgres` user
4. Use pgAdmin or command line to connect

### 2. Database Configuration

#### Create Database and User
```sql
-- Connect as postgres superuser
psql -U postgres

-- Create the database
CREATE DATABASE lab_operations
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Create application user
CREATE USER lab_user WITH
    LOGIN
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    INHERIT
    NOREPLICATION
    CONNECTION LIMIT -1
    PASSWORD 'your_secure_password';

-- Grant necessary permissions
GRANT ALL PRIVILEGES ON DATABASE lab_operations TO lab_user;

-- Connect to the database and grant schema permissions
\c lab_operations
GRANT ALL ON SCHEMA public TO lab_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO lab_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO lab_user;

-- Grant future permissions
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO lab_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO lab_user;
```

### 3. Application Configuration

#### Update application.yml
```yaml
spring:
  profiles:
    active: postgres
  datasource:
    url: jdbc:postgresql://localhost:5432/lab_operations
    username: lab_user
    password: ${DB_PASSWORD:your_secure_password}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  jpa:
    hibernate:
      ddl-auto: create-drop  # Use 'validate' in production
    show-sql: false  # Set to true for debugging
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          time_zone: UTC
```

## 🔒 Security Configuration

### Environment Variables
Create a `.env` file (never commit to git):
```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=lab_operations
DB_USERNAME=lab_user
DB_PASSWORD=your_very_secure_password_here

# Application Configuration
SPRING_PROFILES_ACTIVE=postgres
```

### Production Security
```sql
-- Create read-only user for reporting
CREATE USER lab_readonly WITH
    LOGIN
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    PASSWORD 'readonly_password';

-- Grant read-only access
GRANT CONNECT ON DATABASE lab_operations TO lab_readonly;
GRANT USAGE ON SCHEMA public TO lab_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO lab_readonly;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO lab_readonly;
```

## 🐳 Docker Setup (Optional)

### docker-compose.yml
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    container_name: lab_operations_db
    environment:
      POSTGRES_DB: lab_operations
      POSTGRES_USER: lab_user
      POSTGRES_PASSWORD: lab_password
      PGDATA: /var/lib/postgresql/data/pgdata
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./scripts/init-db.sql:/docker-entrypoint-initdb.d/init-db.sql
    restart: unless-stopped

  pgadmin:
    image: dpage/pgadmin4:latest
    container_name: lab_operations_pgadmin
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@lab.com
      PGADMIN_DEFAULT_PASSWORD: admin
    ports:
      - "8081:80"
    depends_on:
      - postgres
    restart: unless-stopped

volumes:
  postgres_data:
```

### Start with Docker
```bash
# Start services
docker-compose up -d

# Check logs
docker-compose logs postgres

# Connect to database
docker exec -it lab_operations_db psql -U lab_user -d lab_operations
```

## 📊 Database Schema

### Tables Created by Hibernate
```sql
-- Visits table (Patient visits)
CREATE TABLE visits (
    visit_id BIGSERIAL PRIMARY KEY,
    patient_details JSON NOT NULL,
    status VARCHAR(255) CHECK (status IN ('PENDING','IN_PROGRESS','AWAITING_APPROVAL','APPROVED','BILLED','COMPLETED')),
    created_at TIMESTAMP(6)
);

-- Test Templates (Available lab tests)
CREATE TABLE test_templates (
    template_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    parameters JSON NOT NULL,
    base_price NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP(6)
);

-- Lab Tests (Actual tests performed)
CREATE TABLE lab_tests (
    test_id BIGSERIAL PRIMARY KEY,
    visit_id BIGINT NOT NULL REFERENCES visits(visit_id),
    test_template_id BIGINT NOT NULL REFERENCES test_templates(template_id),
    results JSON,
    status VARCHAR(255) CHECK (status IN ('PENDING','IN_PROGRESS','COMPLETED','APPROVED')),
    price NUMERIC(10,2) NOT NULL,
    approved BOOLEAN,
    approved_by VARCHAR(255),
    approved_at TIMESTAMP(6)
);

-- Billing (Payment information)
CREATE TABLE billing (
    bill_id BIGSERIAL PRIMARY KEY,
    visit_id BIGINT NOT NULL UNIQUE REFERENCES visits(visit_id),
    total_amount NUMERIC(10,2) NOT NULL,
    paid BOOLEAN,
    created_at TIMESTAMP(6)
);
```

## 🔍 Verification and Testing

### Test Database Connection
```bash
# Test connection
psql -U lab_user -d lab_operations -c "SELECT version();"

# Test JSON support
psql -U lab_user -d lab_operations -c "SELECT '{}' :: json;"

# Check database size
psql -U lab_user -d lab_operations -c "SELECT pg_size_pretty(pg_database_size('lab_operations'));"
```

### Application Health Check
```bash
# Start application
mvn spring-boot:run

# Check health endpoint
curl http://localhost:8080/actuator/health

# Test API endpoint
curl -X POST -H "Content-Type: application/json" \
  -d '{"name": "Test Template", "basePrice": 100.00, "parameters": {"param1": "value1"}}' \
  http://localhost:8080/test-templates
```

## 🚨 Troubleshooting

### Common Issues

#### Connection Refused
```bash
# Check if PostgreSQL is running
sudo systemctl status postgresql  # Linux
brew services list | grep postgresql  # macOS

# Check port availability
netstat -an | grep 5432
```

#### Authentication Failed
```bash
# Reset password
sudo -u postgres psql -c "ALTER USER lab_user PASSWORD 'new_password';"

# Check pg_hba.conf for authentication method
sudo find /etc -name pg_hba.conf 2>/dev/null
```

#### Permission Denied
```sql
-- Grant all permissions again
GRANT ALL PRIVILEGES ON DATABASE lab_operations TO lab_user;
GRANT ALL ON SCHEMA public TO lab_user;
```

#### JSON/JSONB Issues
```sql
-- Test JSON functionality
SELECT '{"key": "value"}' :: json;
SELECT '{"key": "value"}' :: jsonb;

-- Check PostgreSQL version (should be 9.2+ for JSON, 9.4+ for JSONB)
SELECT version();
```

## 📈 Performance Tuning

### Connection Pool Settings
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20        # Adjust based on load
      minimum-idle: 5              # Minimum connections
      connection-timeout: 30000    # 30 seconds
      idle-timeout: 600000         # 10 minutes
      max-lifetime: 1800000        # 30 minutes
      leak-detection-threshold: 60000  # 1 minute
```

### PostgreSQL Configuration
```sql
-- Recommended settings for development
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET default_statistics_target = 100;

-- Reload configuration
SELECT pg_reload_conf();
```

## 💾 Backup and Recovery

### Backup Commands
```bash
# Full database backup
pg_dump -U lab_user -h localhost -d lab_operations > lab_operations_backup.sql

# Compressed backup
pg_dump -U lab_user -h localhost -d lab_operations | gzip > lab_operations_backup.sql.gz

# Schema only
pg_dump -U lab_user -h localhost -d lab_operations --schema-only > schema_backup.sql

# Data only
pg_dump -U lab_user -h localhost -d lab_operations --data-only > data_backup.sql
```

### Restore Commands
```bash
# Restore from backup
psql -U lab_user -d lab_operations < lab_operations_backup.sql

# Restore compressed backup
gunzip -c lab_operations_backup.sql.gz | psql -U lab_user -d lab_operations
```

## 🔄 Migration Scripts

### Sample Migration Script
```sql
-- scripts/migrations/001_initial_setup.sql
-- Run this after database creation

-- Create indexes for better performance
CREATE INDEX idx_visits_status ON visits(status);
CREATE INDEX idx_visits_created_at ON visits(created_at);
CREATE INDEX idx_lab_tests_status ON lab_tests(status);
CREATE INDEX idx_lab_tests_visit_id ON lab_tests(visit_id);
CREATE INDEX idx_billing_paid ON billing(paid);

-- Create functions for JSON validation (optional)
CREATE OR REPLACE FUNCTION validate_patient_details(patient_json JSON)
RETURNS BOOLEAN AS $$
BEGIN
    -- Check required fields
    IF patient_json->>'name' IS NULL OR patient_json->>'name' = '' THEN
        RETURN FALSE;
    END IF;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;
```

## 📞 Support

### Getting Help
- **PostgreSQL Documentation**: https://www.postgresql.org/docs/
- **Spring Boot Database Guide**: https://spring.io/guides/gs/accessing-data-jpa/
- **Hibernate Documentation**: https://hibernate.org/orm/documentation/

### Monitoring
```sql
-- Check active connections
SELECT count(*) FROM pg_stat_activity WHERE datname = 'lab_operations';

-- Check database size
SELECT pg_size_pretty(pg_database_size('lab_operations'));

-- Check table sizes
SELECT schemaname,tablename,pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables WHERE schemaname = 'public' ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

---

## ✅ Checklist

- [ ] PostgreSQL installed and running
- [ ] Database `lab_operations` created
- [ ] User `lab_user` created with proper permissions
- [ ] Application configuration updated
- [ ] Connection tested successfully
- [ ] Application starts without errors
- [ ] API endpoints working
- [ ] Backup strategy implemented

**🎉 Your PostgreSQL database is ready for the Lab Operations Management System!**
