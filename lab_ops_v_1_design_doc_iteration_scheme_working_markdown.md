# Lab Operations Software – Design Doc (Updated)

This document captures the system design, iteration scheme, and working notes for developing a **lab operations software** that runs on a self-hosted server. This version reflects the updated **visit-only model** with inline patient details stored as `JSONB`, allowing future migration to full patient accounts.

---

## 1. System Overview
- Self-hosted lab management software.
- Each **visit** is unique and contains patient details inline (no persistent accounts yet).
- Admins can define test templates (parameters, fields, price).
- Reception creates visits, phlebotomy collects samples, lab enters results, approvers validate, and billing completes the cycle.
- Flexible schema with `JSONB` for parameters and patient details.
- APIs expose CRUD operations for all entities.
- Test suite ensures reliability.

---

## 2. Data Model (Postgres)

### visits
```sql
CREATE TABLE visits (
    visit_id SERIAL PRIMARY KEY,
    patient_details JSONB NOT NULL, -- stores name, age, gender, phone, address inline
    created_at TIMESTAMP DEFAULT NOW(),
    status VARCHAR(50) DEFAULT 'pending' -- pending, in-progress, awaiting-approval, approved, billed, completed
);
```

### test_templates
```sql
CREATE TABLE test_templates (
    template_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    parameters JSONB NOT NULL, -- defines dynamic fields, reference ranges, types
    base_price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);
```

### lab_tests
```sql
CREATE TABLE lab_tests (
    test_id SERIAL PRIMARY KEY,
    visit_id INT NOT NULL REFERENCES visits(visit_id),
    test_template_id INT NOT NULL REFERENCES test_templates(template_id),
    status VARCHAR(50) DEFAULT 'pending',
    price DECIMAL(10,2) NOT NULL,
    results JSONB,
    approved BOOLEAN DEFAULT FALSE,
    approved_by VARCHAR(255),
    approved_at TIMESTAMP
);
```

### billing
```sql
CREATE TABLE billing (
    bill_id SERIAL PRIMARY KEY,
    visit_id INT NOT NULL REFERENCES visits(visit_id),
    total_amount DECIMAL(10,2) NOT NULL,
    paid BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);
```

---

## 3. API Specification

### Visits
- **Create Visit**: `POST /visits`
  - Body: `{ "patient_details": { "name": "John Doe", "age": 40, "gender": "M", "phone": "9999999999", "address": "Hyderabad" } }`
- **Get Visit**: `GET /visits/{id}`
- **List Visits**: `GET /visits?status=pending`

### Test Templates
- **Add Template**: `POST /test-templates`
- **List Templates**: `GET /test-templates`

### Lab Tests
- **Add Test to Visit**: `POST /visits/{visitId}/tests`
- **Update Results**: `PATCH /visits/{visitId}/tests/{testId}/results`
- **Approve Results**: `PATCH /visits/{visitId}/tests/{testId}/approve`

### Billing
- **Generate Bill**: `GET /visits/{visitId}/bill`

---

## 4. Visit Lifecycle
1. **Reception**: Create visit with patient details.
2. **Phlebotomy**: Sample collection.
3. **Lab Processing**: Add tests, enter results.
4. **Approval**: Supervisor/doctor approves.
5. **Billing**: Bill is generated and marked paid.
6. **Completion**: Visit is closed.

---

## 5. Iteration Scheme

### Iteration 1 (Core CRUD)
- CRUD APIs for `visits`, `test_templates`, `lab_tests`, `billing`.
- JSONB handling for patient details and parameters.
- Integration tests for CRUD endpoints.

### Iteration 2 (Lifecycle & Workflow)
- Implement visit status transitions.
- Approval workflow for lab tests.
- Bill calculation from tests.

### Iteration 3 (Frontend Basics)
- Dynamic form rendering from JSONB parameters.
- Highlight out-of-range values but do not block.
- Technician UI for entering results.

### Iteration 4 (Admin & Reporting)
- Admin dashboard for approvals.
- Billing dashboard.
- Export reports (CSV/PDF).

---

## 6. Testing Strategy
- **Unit tests** for services and validators.
- **Integration tests** with Postgres (Testcontainers).
- **API tests** using RestAssured or Postman.
- **Lifecycle tests** for visit workflow.

---

## 7. Deployment Notes
- Postgres + Spring Boot app in Docker Compose.
- Expose via Cloudflare Tunnel (maps dynamic IP → fixed domain).
- Lightweight deployment on local CPU (i5, 8GB RAM, 256GB SSD).

---

## 8. Future Extension (Patient Accounts)
- Current model: inline `patient_details JSONB` in visits.
- Future: migrate to `patients` table and reference via `patient_id`.
- Migration path: Extract unique patients by phone → link visits → drop inline columns.

---

## ✅ Acceptance Criteria
- Visit lifecycle works end-to-end without patient accounts.
- Flexible schema using JSONB for patient details and test parameters.
- APIs cover CRUD for visits, templates, tests, billing.
- Test suite validates all endpoints and workflows.

