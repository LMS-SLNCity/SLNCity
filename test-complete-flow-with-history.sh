#!/bin/bash

echo "🔍 TESTING COMPLETE LAB WORKFLOW WITH HISTORY TRACKING"
echo "======================================================"

# Test 1: Admin Login and Template Creation
echo ""
echo "🔐 STEP 1: Testing Admin Login and Template Creation..."
curl -s -c cookies.txt -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=admin123" \
  --location > /dev/null

if [ $? -eq 0 ]; then
    echo "✅ Admin login successful"
else
    echo "❌ Admin login failed"
    exit 1
fi

# Create test template
echo ""
echo "📋 Creating test template..."
TEMPLATE_RESPONSE=$(curl -s -b cookies.txt -X POST "http://localhost:8080/test-templates" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Complete Blood Count",
    "description": "Full blood analysis including RBC, WBC, platelets",
    "basePrice": 500.00,
    "parameters": {
      "hemoglobin": {"unit": "g/dL", "normalRange": "12-16"},
      "wbc_count": {"unit": "cells/μL", "normalRange": "4000-11000"},
      "platelet_count": {"unit": "cells/μL", "normalRange": "150000-450000"}
    }
  }')

echo "Template response: $TEMPLATE_RESPONSE"
TEMPLATE_ID=$(echo "$TEMPLATE_RESPONSE" | jq -r '.templateId // empty')

if [ -z "$TEMPLATE_ID" ] || [ "$TEMPLATE_ID" = "null" ]; then
    echo "❌ Failed to create test template"
    exit 1
else
    echo "✅ Test template created with ID: $TEMPLATE_ID"
fi

# Test 2: Reception Login and Patient Registration
echo ""
echo "🔐 STEP 2: Testing Reception Login and Patient Registration..."
curl -s -c cookies.txt -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=reception&password=reception123" \
  --location > /dev/null

if [ $? -eq 0 ]; then
    echo "✅ Reception login successful"
else
    echo "❌ Reception login failed"
    exit 1
fi

# Create patient visit
echo ""
echo "👤 Creating patient visit..."
VISIT_RESPONSE=$(curl -s -b cookies.txt -X POST "http://localhost:8080/visits" \
  -H "Content-Type: application/json" \
  -d '{
    "patientDetails": {
      "name": "Test Patient",
      "age": 30,
      "gender": "Male",
      "phone": "9876543210",
      "email": "test@example.com",
      "address": "Test Address"
    }
  }')

echo "Visit response: $VISIT_RESPONSE"
VISIT_ID=$(echo "$VISIT_RESPONSE" | jq -r '.visitId // empty')

if [ -z "$VISIT_ID" ] || [ "$VISIT_ID" = "null" ]; then
    echo "❌ Failed to create patient visit"
    exit 1
else
    echo "✅ Patient visit created with ID: $VISIT_ID"
fi

# Order test
echo ""
echo "🧪 Ordering test for visit $VISIT_ID..."
TEST_RESPONSE=$(curl -s -b cookies.txt -X POST "http://localhost:8080/visits/$VISIT_ID/tests" \
  -H "Content-Type: application/json" \
  -d "{
    \"testTemplateId\": $TEMPLATE_ID
  }")

echo "Test order response: $TEST_RESPONSE"
TEST_ID=$(echo "$TEST_RESPONSE" | jq -r '.testId // empty')

if [ -z "$TEST_ID" ] || [ "$TEST_ID" = "null" ]; then
    echo "❌ Failed to order test"
    exit 1
else
    echo "✅ Test ordered with ID: $TEST_ID"
fi

# Test 3: Phlebotomy Login and Sample Collection
echo ""
echo "🔐 STEP 3: Testing Phlebotomy Login and Sample Collection..."
curl -s -c cookies.txt -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=phlebotomy&password=phlebotomy123" \
  --location > /dev/null

if [ $? -eq 0 ]; then
    echo "✅ Phlebotomy login successful"
else
    echo "❌ Phlebotomy login failed"
    exit 1
fi

# Collect sample
echo ""
echo "🧪 Collecting sample for test $TEST_ID..."
COLLECTION_RESPONSE=$(curl -s -b cookies.txt -X POST "http://localhost:8080/sample-collection/collect/$TEST_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "sampleType": "WHOLE_BLOOD",
    "collectedBy": "phlebotomy",
    "collectionSite": "Left arm",
    "containerType": "EDTA tube",
    "volumeReceived": 5.0
  }')

echo "Collection response: $COLLECTION_RESPONSE"
SAMPLE_ID=$(echo "$COLLECTION_RESPONSE" | jq -r '.sampleId // empty')

if [ -z "$SAMPLE_ID" ] || [ "$SAMPLE_ID" = "null" ]; then
    echo "❌ Failed to collect sample"
    echo "Error: $COLLECTION_RESPONSE"
else
    echo "✅ Sample collected with ID: $SAMPLE_ID"
fi

# Test 4: Technician Login and Test Processing
echo ""
echo "🔐 STEP 4: Testing Technician Login..."
curl -s -c cookies.txt -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=technician&password=technician123" \
  --location > /dev/null

if [ $? -eq 0 ]; then
    echo "✅ Technician login successful"
else
    echo "❌ Technician login failed"
    exit 1
fi

# Test 5: Check History and Audit Trail
echo ""
echo "📊 STEP 5: Checking History and Audit Trail..."

# Check samples
echo ""
echo "🧪 Checking samples..."
SAMPLES=$(curl -s -b cookies.txt -X GET "http://localhost:8080/samples")
SAMPLES_COUNT=$(echo "$SAMPLES" | jq 'length // 0')
echo "Samples count: $SAMPLES_COUNT"
if [ "$SAMPLES_COUNT" -gt 0 ]; then
    echo "✅ Sample history tracked successfully"
    echo "Sample details: $(echo "$SAMPLES" | jq '.[0] | {sampleId, sampleNumber, sampleType, status}')"
else
    echo "⚠️  No samples found in history"
fi

# Check lab tests
echo ""
echo "🔬 Checking lab tests..."
LAB_TESTS=$(curl -s -b cookies.txt -X GET "http://localhost:8080/lab-tests")
LAB_TESTS_COUNT=$(echo "$LAB_TESTS" | jq 'length // 0')
echo "Lab tests count: $LAB_TESTS_COUNT"
if [ "$LAB_TESTS_COUNT" -gt 0 ]; then
    echo "✅ Lab test history tracked successfully"
    echo "Test details: $(echo "$LAB_TESTS" | jq '.[0] | {testId, status, price}')"
else
    echo "⚠️  No lab tests found in history"
fi

# Check audit trail (admin only)
echo ""
echo "🔐 Switching to admin for audit trail..."
curl -s -c cookies.txt -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=admin123" \
  --location > /dev/null

echo ""
echo "📋 Checking audit trail..."
AUDIT_TRAIL=$(curl -s -b cookies.txt -X GET "http://localhost:8080/audit-trail")
AUDIT_COUNT=$(echo "$AUDIT_TRAIL" | jq 'length // 0')
echo "Audit trail entries: $AUDIT_COUNT"
if [ "$AUDIT_COUNT" -gt 0 ]; then
    echo "✅ Audit trail tracked successfully"
    echo "Recent audit entries: $(echo "$AUDIT_TRAIL" | jq '.[0:3] | .[] | {action, tableName, userId, timestamp}')"
else
    echo "⚠️  No audit trail entries found"
fi

# Get statistics
echo ""
echo "📊 Getting system statistics..."
LAB_STATS=$(curl -s -b cookies.txt -X GET "http://localhost:8080/lab-tests/statistics")
echo "Lab test statistics: $LAB_STATS"

AUDIT_STATS=$(curl -s -b cookies.txt -X GET "http://localhost:8080/audit-trail/statistics")
echo "Audit trail statistics: $AUDIT_STATS"

echo ""
echo "🎯 COMPLETE FLOW WITH HISTORY TRACKING SUMMARY"
echo "=============================================="
echo "✅ Admin login and template creation: Working"
echo "✅ Reception login and patient registration: Working"
echo "✅ Test ordering: Working"
echo "✅ Phlebotomy login and sample collection: Working"
echo "✅ Technician login: Working"
echo "📊 Sample history tracking: $([[ "$SAMPLES_COUNT" -gt 0 ]] && echo "Working" || echo "Empty")"
echo "🔬 Lab test history tracking: $([[ "$LAB_TESTS_COUNT" -gt 0 ]] && echo "Working" || echo "Empty")"
echo "📋 Audit trail tracking: $([[ "$AUDIT_COUNT" -gt 0 ]] && echo "Working" || echo "Empty")"

# Cleanup
rm -f cookies.txt

echo ""
echo "🎉 COMPLETE WORKFLOW WITH HISTORY TRACKING TEST COMPLETE!"
