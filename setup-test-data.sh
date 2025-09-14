#!/bin/bash

echo "Setting up test data..."

# Login as reception
curl -s -c cookies.txt -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=reception&password=reception123" \
  --location > /dev/null

# Create a visit with tests
VISIT_RESPONSE=$(curl -s -b cookies.txt -X POST http://localhost:8080/visits \
  -H "Content-Type: application/json" \
  -d '{
    "patientDetails": {
      "name": "Jane Smith",
      "age": "28",
      "gender": "F",
      "phone": "9876543210",
      "email": "jane.smith@example.com"
    }
  }')

VISIT_ID=$(echo "$VISIT_RESPONSE" | jq -r '.visitId')
echo "Created visit ID: $VISIT_ID"

# Order CBC test
curl -s -b cookies.txt -X POST "http://localhost:8080/visits/$VISIT_ID/tests" \
  -H "Content-Type: application/json" \
  -d '{
    "testTemplateId": 1,
    "price": 450.00
  }' > /dev/null

# Order Blood Sugar test  
curl -s -b cookies.txt -X POST "http://localhost:8080/visits/$VISIT_ID/tests" \
  -H "Content-Type: application/json" \
  -d '{
    "testTemplateId": 2,
    "price": 150.00
  }' > /dev/null

echo "Tests ordered for visit $VISIT_ID"

# Verify the visit has tests
VISIT_WITH_TESTS=$(curl -s -b cookies.txt -X GET "http://localhost:8080/visits/$VISIT_ID")
TEST_COUNT=$(echo "$VISIT_WITH_TESTS" | jq '.labTests | length')
echo "Visit now has $TEST_COUNT tests"

rm -f cookies.txt
echo "Test data setup complete!"
