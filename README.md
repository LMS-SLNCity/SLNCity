# Lab Operations Management System

A comprehensive lab operations software built with Spring Boot and PostgreSQL, designed for managing patient visits, lab tests, and billing in a clinical laboratory setting.

## Features

- **Visit Management**: Create and track patient visits with inline patient details stored as JSONB
- **Test Templates**: Define reusable test templates with dynamic parameters and pricing
- **Lab Test Processing**: Add tests to visits, enter results, and manage approval workflow
- **Billing System**: Generate bills and track payments
- **RESTful APIs**: Complete CRUD operations for all entities
- **Docker Support**: Easy deployment with Docker Compose

## Technology Stack

- **Backend**: Spring Boot 3.2, Java 17
- **Database**: PostgreSQL 15 with JSONB support
- **Build Tool**: Maven
- **Testing**: JUnit 5, Testcontainers
- **Containerization**: Docker, Docker Compose

## Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose (for containerized deployment)

### Running with Docker Compose (Recommended)

1. Clone the repository:
```bash
git clone <repository-url>
cd lab-operations
```

2. Start the application:
```bash
docker-compose up -d
```

3. The application will be available at `http://localhost:8080`

### Running Locally

1. Start PostgreSQL database:
```bash
docker run -d \
  --name lab-postgres \
  -e POSTGRES_DB=lab_operations \
  -e POSTGRES_USER=lab_user \
  -e POSTGRES_PASSWORD=lab_password \
  -p 5432:5432 \
  postgres:15
```

2. Build and run the application:
```bash
mvn clean package
java -jar target/lab-operations-*.jar
```

## API Documentation

### Visits

- **Create Visit**: `POST /visits`
- **Get Visit**: `GET /visits/{id}`
- **List Visits**: `GET /visits?status=pending`
- **Update Status**: `PATCH /visits/{id}/status?status=in-progress`
- **Search by Phone**: `GET /visits/search?phone=9999999999`

### Test Templates

- **Create Template**: `POST /test-templates`
- **Get Template**: `GET /test-templates/{id}`
- **List Templates**: `GET /test-templates`
- **Search Templates**: `GET /test-templates/search?name=blood`

### Lab Tests

- **Add Test to Visit**: `POST /visits/{visitId}/tests`
- **Update Results**: `PATCH /visits/{visitId}/tests/{testId}/results`
- **Approve Results**: `PATCH /visits/{visitId}/tests/{testId}/approve`

### Billing

- **Generate Bill**: `GET /billing/visits/{visitId}/bill`
- **Mark as Paid**: `PATCH /billing/{billId}/pay`
- **Get All Bills**: `GET /billing`

## Sample API Usage

### Create a Visit
```bash
curl -X POST http://localhost:8080/visits \
  -H "Content-Type: application/json" \
  -d '{
    "patientDetails": {
      "name": "John Doe",
      "age": 35,
      "gender": "M",
      "phone": "9999999999",
      "address": "Hyderabad"
    }
  }'
```

### Create a Test Template
```bash
curl -X POST http://localhost:8080/test-templates \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Blood Sugar Test",
    "description": "Fasting glucose level",
    "parameters": {
      "fields": [
        {
          "name": "glucose",
          "type": "number",
          "unit": "mg/dL",
          "reference_range": {"min": 70, "max": 100}
        }
      ]
    },
    "basePrice": 200.00
  }'
```

## Visit Lifecycle

1. **Reception**: Create visit with patient details
2. **Phlebotomy**: Sample collection (status: in-progress)
3. **Lab Processing**: Add tests, enter results
4. **Approval**: Supervisor approves results (status: approved)
5. **Billing**: Generate bill (status: billed)
6. **Payment**: Mark as paid (status: completed)

## Testing

Run the test suite:
```bash
mvn test
```

The tests use Testcontainers to spin up a PostgreSQL instance automatically.

## Database Schema

The system uses four main tables:
- `visits`: Patient visits with JSONB patient details
- `test_templates`: Reusable test definitions with JSONB parameters
- `lab_tests`: Individual tests with JSONB results
- `billing`: Billing information

## Configuration

Key configuration properties:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/lab_operations
    username: lab_user
    password: lab_password
  
  jpa:
    hibernate:
      ddl-auto: validate
```

## Health Checks

The application includes health check endpoints:
- Health: `GET /actuator/health`
- Info: `GET /actuator/info`

## Future Enhancements

- Patient account management
- Advanced reporting and analytics
- Role-based access control
- Integration with lab equipment
- Mobile application support

## License

This project is licensed under the MIT License.
