#!/bin/bash

echo "🔍 DEBUGGING FRONTEND SAMPLE COLLECTION"
echo "======================================="

# Step 1: Check if application is running
echo ""
echo "🔐 Step 1: Check application status..."
APP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/actuator/health")
echo "Application status: $APP_STATUS"

if [ "$APP_STATUS" != "200" ]; then
    echo "❌ Application not running properly"
    exit 1
fi

# Step 2: Test login and get session
echo ""
echo "🔐 Step 2: Test phlebotomy login..."
LOGIN_RESPONSE=$(curl -s -c cookies.txt -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=phlebotomy&password=phlebotomy123" \
  --location -w "%{http_code}")

echo "Login response code: $LOGIN_RESPONSE"

# Step 3: Check dashboard access
echo ""
echo "🌐 Step 3: Check dashboard access..."
DASHBOARD_STATUS=$(curl -s -b cookies.txt -o /dev/null -w "%{http_code}" "http://localhost:8080/phlebotomy/dashboard.html")
echo "Dashboard status: $DASHBOARD_STATUS"

# Step 4: Check JavaScript file access
echo ""
echo "📜 Step 4: Check JavaScript file access..."
JS_STATUS=$(curl -s -b cookies.txt -o /dev/null -w "%{http_code}" "http://localhost:8080/js/phlebotomy.js")
echo "JavaScript status: $JS_STATUS"

# Step 5: Check CSS file access
echo ""
echo "🎨 Step 5: Check CSS file access..."
CSS_STATUS=$(curl -s -b cookies.txt -o /dev/null -w "%{http_code}" "http://localhost:8080/css/phlebotomy.css")
echo "CSS status: $CSS_STATUS"

# Step 6: Test pending samples API
echo ""
echo "💉 Step 6: Test pending samples API..."
PENDING_SAMPLES=$(curl -s -b cookies.txt -X GET "http://localhost:8080/sample-collection/pending")
PENDING_COUNT=$(echo "$PENDING_SAMPLES" | jq 'length // 0' 2>/dev/null || echo "0")
echo "Pending samples count: $PENDING_COUNT"

if [ "$PENDING_COUNT" -gt 0 ]; then
    echo "Sample data preview:"
    echo "$PENDING_SAMPLES" | jq '.[0] | {testId, patientName, testName, status}' 2>/dev/null || echo "Failed to parse JSON"
    
    # Step 7: Test sample collection API
    echo ""
    echo "🧪 Step 7: Test sample collection API..."
    TEST_ID=$(echo "$PENDING_SAMPLES" | jq -r '.[0].testId // empty' 2>/dev/null)
    
    if [ -n "$TEST_ID" ] && [ "$TEST_ID" != "null" ]; then
        echo "Testing collection for test ID: $TEST_ID"
        
        COLLECTION_RESPONSE=$(curl -s -b cookies.txt -X POST "http://localhost:8080/sample-collection/collect/$TEST_ID" \
          -H "Content-Type: application/json" \
          -d '{
            "sampleType": "WHOLE_BLOOD",
            "collectedBy": "phlebotomy",
            "collectionSite": "Left arm",
            "containerType": "EDTA tube",
            "volumeReceived": 5.0
          }' -w "%{http_code}")
        
        # Extract HTTP status code (last 3 characters)
        HTTP_CODE="${COLLECTION_RESPONSE: -3}"
        RESPONSE_BODY="${COLLECTION_RESPONSE%???}"
        
        echo "Collection API HTTP code: $HTTP_CODE"
        
        if [ "$HTTP_CODE" = "200" ]; then
            echo "✅ Collection API working"
            SAMPLE_ID=$(echo "$RESPONSE_BODY" | jq -r '.sampleId // empty' 2>/dev/null)
            echo "Sample ID created: $SAMPLE_ID"
        else
            echo "❌ Collection API failed"
            echo "Response: $RESPONSE_BODY"
        fi
    else
        echo "❌ No valid test ID found"
    fi
else
    echo "ℹ️  No pending samples to test collection"
fi

# Step 8: Check for common frontend issues
echo ""
echo "🔍 Step 8: Check for common frontend issues..."

# Check if phlebotomy.js has syntax errors
echo "Checking JavaScript syntax..."
node -c src/main/resources/static/js/phlebotomy.js 2>/dev/null && echo "✅ JavaScript syntax OK" || echo "❌ JavaScript syntax error"

# Check if CSS is valid (basic check)
echo "Checking CSS file size..."
CSS_SIZE=$(wc -c < src/main/resources/static/css/phlebotomy.css 2>/dev/null || echo "0")
echo "CSS file size: $CSS_SIZE bytes"

if [ "$CSS_SIZE" -gt 1000 ]; then
    echo "✅ CSS file seems complete"
else
    echo "❌ CSS file might be incomplete"
fi

# Step 9: Create test data if needed
echo ""
echo "🔧 Step 9: Create fresh test data..."

# Login as admin and create test template
curl -s -c cookies.txt -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin&password=admin123" \
  --location > /dev/null

TEMPLATE_RESPONSE=$(curl -s -b cookies.txt -X POST "http://localhost:8080/test-templates" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Complete Blood Count",
    "description": "Full blood analysis",
    "basePrice": 500.00,
    "parameters": {
      "hemoglobin": {"unit": "g/dL", "normalRange": "12-16"},
      "wbc_count": {"unit": "cells/μL", "normalRange": "4000-11000"}
    }
  }' 2>/dev/null)

TEMPLATE_ID=$(echo "$TEMPLATE_RESPONSE" | jq -r '.templateId // empty' 2>/dev/null)
echo "Test template ID: $TEMPLATE_ID"

# Login as reception and create visit
curl -s -c cookies.txt -X POST http://localhost:8080/login \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=reception&password=reception123" \
  --location > /dev/null

VISIT_RESPONSE=$(curl -s -b cookies.txt -X POST "http://localhost:8080/visits" \
  -H "Content-Type: application/json" \
  -d '{
    "patientDetails": {
      "name": "Debug Frontend Patient",
      "age": 30,
      "gender": "Male",
      "phone": "1234567890",
      "email": "debug@frontend.com",
      "address": "Debug Address"
    }
  }' 2>/dev/null)

VISIT_ID=$(echo "$VISIT_RESPONSE" | jq -r '.visitId // empty' 2>/dev/null)
echo "Visit ID: $VISIT_ID"

# Order test
if [ -n "$VISIT_ID" ] && [ "$VISIT_ID" != "null" ] && [ -n "$TEMPLATE_ID" ] && [ "$TEMPLATE_ID" != "null" ]; then
    TEST_RESPONSE=$(curl -s -b cookies.txt -X POST "http://localhost:8080/visits/$VISIT_ID/tests" \
      -H "Content-Type: application/json" \
      -d "{\"testTemplateId\": $TEMPLATE_ID}" 2>/dev/null)
    
    NEW_TEST_ID=$(echo "$TEST_RESPONSE" | jq -r '.testId // empty' 2>/dev/null)
    echo "New test ID: $NEW_TEST_ID"
    
    if [ -n "$NEW_TEST_ID" ] && [ "$NEW_TEST_ID" != "null" ]; then
        echo "✅ Fresh test data created successfully"
    else
        echo "❌ Failed to create test data"
    fi
else
    echo "❌ Failed to create visit or template"
fi

# Cleanup
rm -f cookies.txt

echo ""
echo "🎯 FRONTEND DEBUG SUMMARY"
echo "========================"
echo "Application: $([[ "$APP_STATUS" = "200" ]] && echo "✅ Running" || echo "❌ Not Running")"
echo "Dashboard: $([[ "$DASHBOARD_STATUS" = "200" ]] && echo "✅ Accessible" || echo "❌ Not Accessible")"
echo "JavaScript: $([[ "$JS_STATUS" = "200" ]] && echo "✅ Accessible" || echo "❌ Not Accessible")"
echo "CSS: $([[ "$CSS_STATUS" = "200" ]] && echo "✅ Accessible" || echo "❌ Not Accessible")"
echo "Pending Samples: $PENDING_COUNT"

echo ""
echo "🔍 NEXT STEPS:"
echo "1. Open browser to: http://localhost:8080/phlebotomy/dashboard.html"
echo "2. Login with: phlebotomy / phlebotomy123"
echo "3. Check browser console (F12) for JavaScript errors"
echo "4. Try clicking the 'Collect' button and observe behavior"
echo "5. Check Network tab for failed API requests"
