#!/bin/bash

echo "🔍 DEBUGGING SAMPLE COLLECTION ISSUE"
echo "===================================="

# Step 1: Create a fresh test scenario
echo ""
echo "🔐 Step 1: Login as reception and create test scenario..."
curl -s -c cookies.txt -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=reception&password=reception123" \
  --location > /dev/null

# Create patient visit
VISIT_RESPONSE=$(curl -s -b cookies.txt -X POST "http://localhost:8080/visits" \
  -H "Content-Type: application/json" \
  -d '{
    "patientDetails": {
      "name": "Debug Sample Patient",
      "age": 35,
      "gender": "Male",
      "phone": "9876543210",
      "email": "debug@sample.com",
      "address": "Debug Address"
    }
  }')

VISIT_ID=$(echo "$VISIT_RESPONSE" | jq -r '.visitId // empty')
echo "Created visit ID: $VISIT_ID"

# Order test
TEST_RESPONSE=$(curl -s -b cookies.txt -X POST "http://localhost:8080/visits/$VISIT_ID/tests" \
  -H "Content-Type: application/json" \
  -d '{
    "testTemplateId": 1
  }')

TEST_ID=$(echo "$TEST_RESPONSE" | jq -r '.testId // empty')
echo "Created test ID: $TEST_ID"

# Step 2: Login as phlebotomy and check pending samples
echo ""
echo "🔐 Step 2: Login as phlebotomy and check pending samples..."
curl -s -c cookies.txt -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=phlebotomy&password=phlebotomy123" \
  --location > /dev/null

echo ""
echo "💉 Checking pending samples..."
PENDING_SAMPLES=$(curl -s -b cookies.txt -X GET "http://localhost:8080/sample-collection/pending")
echo "Pending samples response:"
echo "$PENDING_SAMPLES" | jq .

PENDING_COUNT=$(echo "$PENDING_SAMPLES" | jq 'length // 0')
echo "Pending samples count: $PENDING_COUNT"

if [ "$PENDING_COUNT" -eq 0 ]; then
    echo "❌ No pending samples found - cannot test collection"
    exit 1
fi

# Step 3: Test sample collection API directly
echo ""
echo "🧪 Step 3: Testing sample collection API directly..."
echo "Attempting to collect sample for test ID: $TEST_ID"

COLLECTION_RESPONSE=$(curl -s -b cookies.txt -X POST "http://localhost:8080/sample-collection/collect/$TEST_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "sampleType": "WHOLE_BLOOD",
    "collectedBy": "phlebotomy",
    "collectionSite": "Left arm",
    "containerType": "EDTA tube",
    "volumeReceived": 5.0
  }')

echo "Collection API response:"
echo "$COLLECTION_RESPONSE"

# Check if collection was successful
SAMPLE_ID=$(echo "$COLLECTION_RESPONSE" | jq -r '.sampleId // empty')
if [ -n "$SAMPLE_ID" ] && [ "$SAMPLE_ID" != "null" ]; then
    echo "✅ Sample collection API working - Sample ID: $SAMPLE_ID"
else
    echo "❌ Sample collection API failed"
    echo "Error response: $COLLECTION_RESPONSE"
fi

# Step 4: Check updated pending samples
echo ""
echo "🔄 Step 4: Checking updated pending samples..."
UPDATED_PENDING=$(curl -s -b cookies.txt -X GET "http://localhost:8080/sample-collection/pending")
UPDATED_COUNT=$(echo "$UPDATED_PENDING" | jq 'length // 0')
echo "Updated pending samples count: $UPDATED_COUNT"

# Step 5: Test browser access
echo ""
echo "🌐 Step 5: Testing browser access..."
echo "Dashboard URL: http://localhost:8080/phlebotomy/dashboard.html"
echo "Login credentials: phlebotomy / phlebotomy123"

# Check if phlebotomy dashboard is accessible
DASHBOARD_STATUS=$(curl -s -b cookies.txt -o /dev/null -w "%{http_code}" "http://localhost:8080/phlebotomy/dashboard.html")
echo "Dashboard HTTP status: $DASHBOARD_STATUS"

if [ "$DASHBOARD_STATUS" = "200" ]; then
    echo "✅ Dashboard accessible"
else
    echo "❌ Dashboard not accessible"
fi

# Step 6: Check JavaScript files
echo ""
echo "📜 Step 6: Checking JavaScript files..."
JS_STATUS=$(curl -s -b cookies.txt -o /dev/null -w "%{http_code}" "http://localhost:8080/js/phlebotomy.js")
echo "JavaScript file HTTP status: $JS_STATUS"

if [ "$JS_STATUS" = "200" ]; then
    echo "✅ JavaScript file accessible"
else
    echo "❌ JavaScript file not accessible"
fi

# Cleanup
rm -f cookies.txt

echo ""
echo "🎯 SAMPLE COLLECTION DEBUG SUMMARY"
echo "=================================="
echo "Visit ID: $VISIT_ID"
echo "Test ID: $TEST_ID"
echo "Sample ID: $SAMPLE_ID"
echo "API Status: $([[ -n "$SAMPLE_ID" && "$SAMPLE_ID" != "null" ]] && echo "Working" || echo "Failed")"
echo "Dashboard Status: $([[ "$DASHBOARD_STATUS" = "200" ]] && echo "Accessible" || echo "Not Accessible")"
echo "JavaScript Status: $([[ "$JS_STATUS" = "200" ]] && echo "Accessible" || echo "Not Accessible")"

echo ""
echo "🔍 If API is working but browser collection fails, check:"
echo "1. Browser console for JavaScript errors"
echo "2. Network tab for failed requests"
echo "3. Modal display and form submission"
echo "4. Authentication session in browser"
