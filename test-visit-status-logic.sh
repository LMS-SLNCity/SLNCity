#!/bin/bash

# Test script to verify visit status update logic
BASE_URL="http://localhost:8080"

echo "🧪 Testing Visit Status Update Logic"
echo "===================================="

# Create a test template
echo "1. Creating test template..."
TEMPLATE_RESPONSE=$(curl -s -X POST "$BASE_URL/test-templates" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Template for Status Logic",
    "description": "Testing status logic",
    "parameters": {"param1": {"type": "number"}},
    "basePrice": 100.00
  }')

TEMPLATE_ID=$(echo "$TEMPLATE_RESPONSE" | grep -o '"templateId":[0-9]*' | cut -d':' -f2)
echo "Template ID: $TEMPLATE_ID"

# Create a visit
echo "2. Creating visit..."
VISIT_RESPONSE=$(curl -s -X POST "$BASE_URL/visits" \
  -H "Content-Type: application/json" \
  -d '{
    "patientDetails": {
      "name": "Test Patient",
      "age": 30,
      "phone": "1234567890"
    }
  }')

VISIT_ID=$(echo "$VISIT_RESPONSE" | grep -o '"visitId":[0-9]*' | cut -d':' -f2)
echo "Visit ID: $VISIT_ID"

# Check initial visit status
echo "3. Initial visit status:"
curl -s "$BASE_URL/visits/$VISIT_ID" | grep -o '"status":"[^"]*"'

# Add first test to visit
echo "4. Adding first test..."
TEST1_RESPONSE=$(curl -s -X POST "$BASE_URL/visits/$VISIT_ID/tests" \
  -H "Content-Type: application/json" \
  -d "{\"testTemplateId\": $TEMPLATE_ID, \"price\": 100.00}")

TEST1_ID=$(echo "$TEST1_RESPONSE" | grep -o '"testId":[0-9]*' | cut -d':' -f2)
echo "Test 1 ID: $TEST1_ID"

# Add second test to visit
echo "5. Adding second test..."
TEST2_RESPONSE=$(curl -s -X POST "$BASE_URL/visits/$VISIT_ID/tests" \
  -H "Content-Type: application/json" \
  -d "{\"testTemplateId\": $TEMPLATE_ID, \"price\": 150.00}")

TEST2_ID=$(echo "$TEST2_RESPONSE" | grep -o '"testId":[0-9]*' | cut -d':' -f2)
echo "Test 2 ID: $TEST2_ID"

# Check visit status after adding tests
echo "6. Visit status after adding tests:"
curl -s "$BASE_URL/visits/$VISIT_ID" | grep -o '"status":"[^"]*"'

# Complete and approve ONLY the first test
echo "7. Completing first test..."
curl -s -X PATCH "$BASE_URL/visits/$VISIT_ID/tests/$TEST1_ID/results" \
  -H "Content-Type: application/json" \
  -d '{"results": {"param1": "value1"}}'

echo "8. Approving first test..."
curl -s -X PATCH "$BASE_URL/visits/$VISIT_ID/tests/$TEST1_ID/approve" \
  -H "Content-Type: application/json" \
  -d '{"approvedBy": "Dr. Test"}'

# Check visit status - should NOT be approved yet (second test still pending)
echo "9. Visit status after approving first test (should still be PENDING/IN_PROGRESS):"
VISIT_STATUS=$(curl -s "$BASE_URL/visits/$VISIT_ID" | grep -o '"status":"[^"]*"')
echo "$VISIT_STATUS"

if [[ "$VISIT_STATUS" == *"APPROVED"* ]]; then
    echo "❌ BUG FOUND: Visit marked as APPROVED when second test is still pending!"
else
    echo "✅ CORRECT: Visit not marked as approved yet"
fi

# Complete and approve the second test
echo "10. Completing second test..."
curl -s -X PATCH "$BASE_URL/visits/$VISIT_ID/tests/$TEST2_ID/results" \
  -H "Content-Type: application/json" \
  -d '{"results": {"param1": "value2"}}'

echo "11. Approving second test..."
curl -s -X PATCH "$BASE_URL/visits/$VISIT_ID/tests/$TEST2_ID/approve" \
  -H "Content-Type: application/json" \
  -d '{"approvedBy": "Dr. Test"}'

# Check visit status - should NOW be approved
echo "12. Visit status after approving all tests (should be APPROVED):"
FINAL_STATUS=$(curl -s "$BASE_URL/visits/$VISIT_ID" | grep -o '"status":"[^"]*"')
echo "$FINAL_STATUS"

if [[ "$FINAL_STATUS" == *"APPROVED"* ]]; then
    echo "✅ CORRECT: Visit marked as APPROVED after all tests approved"
else
    echo "❌ BUG: Visit should be APPROVED but is: $FINAL_STATUS"
fi

echo "🏁 Test completed!"
