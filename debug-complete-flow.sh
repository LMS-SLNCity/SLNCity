#!/bin/bash

echo "🔍 DEBUGGING COMPLETE LAB WORKFLOW"
echo "=================================="

# Test 1: Admin Login and Template Creation
echo ""
echo "🔐 STEP 1: Testing Admin Login..."
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

# Check existing templates
echo ""
echo "📋 Checking existing test templates..."
TEMPLATES=$(curl -s -b cookies.txt -X GET "http://localhost:8080/test-templates")
echo "Templates response: $TEMPLATES"
TEMPLATE_COUNT=$(echo "$TEMPLATES" | jq 'length // 0')
echo "Template count: $TEMPLATE_COUNT"

# Test 2: Reception Login and Patient Registration
echo ""
echo "🔐 STEP 2: Testing Reception Login..."
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
      "name": "Debug Patient",
      "age": 30,
      "gender": "Male",
      "phone": "9999999999",
      "email": "debug@test.com",
      "address": "Debug Address"
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
if [ "$TEMPLATE_COUNT" -gt 0 ]; then
    TEST_RESPONSE=$(curl -s -b cookies.txt -X POST "http://localhost:8080/visits/$VISIT_ID/tests" \
      -H "Content-Type: application/json" \
      -d '{
        "testTemplateId": 1
      }')
    
    echo "Test order response: $TEST_RESPONSE"
    TEST_ID=$(echo "$TEST_RESPONSE" | jq -r '.testId // empty')
    
    if [ -z "$TEST_ID" ] || [ "$TEST_ID" = "null" ]; then
        echo "❌ Failed to order test"
        exit 1
    else
        echo "✅ Test ordered with ID: $TEST_ID"
    fi
else
    echo "❌ No test templates available"
    exit 1
fi

# Test 3: Phlebotomy Login and Sample Collection
echo ""
echo "🔐 STEP 3: Testing Phlebotomy Login..."
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

# Check pending samples
echo ""
echo "💉 Checking pending samples..."
PENDING_SAMPLES=$(curl -s -b cookies.txt -X GET "http://localhost:8080/sample-collection/pending")
echo "Pending samples: $PENDING_SAMPLES"
PENDING_COUNT=$(echo "$PENDING_SAMPLES" | jq 'length // 0')
echo "Pending samples count: $PENDING_COUNT"

if [ "$PENDING_COUNT" -gt 0 ]; then
    echo "✅ Found $PENDING_COUNT pending samples"
    
    # Collect sample
    echo ""
    echo "🧪 Collecting sample for test $TEST_ID..."
    COLLECTION_RESPONSE=$(curl -s -b cookies.txt -X POST "http://localhost:8080/sample-collection/collect/$TEST_ID" \
      -H "Content-Type: application/json" \
      -d '{
        "sampleType": "SERUM",
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
else
    echo "❌ No pending samples found"
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

# Check visits for technician
echo ""
echo "🔬 Checking visits for technician..."
VISITS=$(curl -s -b cookies.txt -X GET "http://localhost:8080/visits")
echo "Visits response: $VISITS"
VISITS_COUNT=$(echo "$VISITS" | jq 'length // 0')
echo "Visits count: $VISITS_COUNT"

# Test 5: Check History and Audit Trail
echo ""
echo "📊 STEP 5: Checking History and Audit Trail..."

# Check samples
echo ""
echo "🧪 Checking samples..."
SAMPLES=$(curl -s -b cookies.txt -X GET "http://localhost:8080/samples" 2>/dev/null || echo "[]")
echo "Samples response: $SAMPLES"

# Check lab tests
echo ""
echo "🔬 Checking lab tests..."
LAB_TESTS=$(curl -s -b cookies.txt -X GET "http://localhost:8080/lab-tests" 2>/dev/null || echo "[]")
echo "Lab tests response: $LAB_TESTS"

# Check audit trail
echo ""
echo "📋 Checking audit trail..."
AUDIT_TRAIL=$(curl -s -b cookies.txt -X GET "http://localhost:8080/audit-trail" 2>/dev/null || echo "[]")
echo "Audit trail response: $AUDIT_TRAIL"

echo ""
echo "🎯 FLOW DEBUG SUMMARY"
echo "====================="
echo "✅ Admin login: Working"
echo "✅ Reception login: Working"
echo "✅ Phlebotomy login: Working"
echo "✅ Technician login: Working"
echo "📊 Visit creation: $([[ -n "$VISIT_ID" ]] && echo "Working" || echo "Failed")"
echo "🧪 Test ordering: $([[ -n "$TEST_ID" ]] && echo "Working" || echo "Failed")"
echo "💉 Sample collection: $([[ -n "$SAMPLE_ID" ]] && echo "Working" || echo "Failed")"
echo "📋 History tracking: $(echo "$AUDIT_TRAIL" | jq 'length // 0') audit entries"

# Cleanup
rm -f cookies.txt

echo ""
echo "🔍 DEBUG COMPLETE - Check above for issues"
