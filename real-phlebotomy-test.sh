#!/bin/bash

echo "🔍 REAL PHLEBOTOMY TESTING - NO BS"
echo "=================================="

echo "1. Creating test template..."
template_response=$(curl -s -X POST "http://localhost:8080/test-templates" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Real Test CBC",
    "description": "Complete Blood Count",
    "basePrice": 250.00,
    "parameters": {
      "sampleType": "WHOLE_BLOOD",
      "volumeRequired": 5.0,
      "containerType": "EDTA tube"
    }
  }')

echo "Template response: $template_response"
template_id=$(echo "$template_response" | jq -r '.templateId')
echo "Template ID: $template_id"

echo -e "\n2. Creating patient visit..."
visit_response=$(curl -s -X POST "http://localhost:8080/visits" \
  -H "Content-Type: application/json" \
  -d '{
    "patientDetails": {
      "name": "Real Test Patient",
      "age": 30,
      "gender": "MALE",
      "phone": "1234567890",
      "email": "test@real.com",
      "address": "Real Address"
    }
  }')

echo "Visit response: $visit_response"
visit_id=$(echo "$visit_response" | jq -r '.visitId')
echo "Visit ID: $visit_id"

echo -e "\n3. Ordering lab test..."
test_response=$(curl -s -X POST "http://localhost:8080/visits/$visit_id/tests" \
  -H "Content-Type: application/json" \
  -d "{\"testTemplateId\": $template_id}")

echo "Test response: $test_response"
test_id=$(echo "$test_response" | jq -r '.testId')
echo "Test ID: $test_id"

echo -e "\n4. Checking pending samples..."
pending_response=$(curl -s "http://localhost:8080/sample-collection/pending")
echo "Pending samples: $pending_response"

echo -e "\n5. Testing sample collection..."
collection_response=$(curl -s -w "HTTP_CODE:%{http_code}" -X POST "http://localhost:8080/sample-collection/collect/$test_id" \
  -H "Content-Type: application/json" \
  -d '{
    "sampleType": "WHOLE_BLOOD",
    "collectedBy": "test_phlebotomist",
    "collectionSite": "Left Arm",
    "containerType": "EDTA Tube",
    "volumeReceived": 5.0,
    "notes": "Test collection"
  }')

echo "Collection response: $collection_response"

echo -e "\n6. Checking samples in database..."
samples_response=$(curl -s "http://localhost:8080/samples")
echo "Samples: $samples_response"

echo -e "\n7. Testing dashboard JavaScript loading..."
js_response=$(curl -s -w "HTTP_CODE:%{http_code}" "http://localhost:8080/js/phlebotomy.js")
echo "JS response code: $(echo "$js_response" | grep -o 'HTTP_CODE:.*' | cut -d: -f2)"

echo -e "\n8. Testing CSS loading..."
css_response=$(curl -s -w "HTTP_CODE:%{http_code}" "http://localhost:8080/css/phlebotomy.css")
echo "CSS response code: $(echo "$css_response" | grep -o 'HTTP_CODE:.*' | cut -d: -f2)"

echo -e "\n9. Testing dashboard HTML structure..."
dashboard_html=$(curl -s "http://localhost:8080/phlebotomy/dashboard.html")

# Check for key elements
if echo "$dashboard_html" | grep -q "PhlebotomyApp"; then
    echo "✅ PhlebotomyApp class found in HTML"
else
    echo "❌ PhlebotomyApp class NOT found in HTML"
fi

if echo "$dashboard_html" | grep -q "pending-collections"; then
    echo "✅ Pending collections element found"
else
    echo "❌ Pending collections element NOT found"
fi

if echo "$dashboard_html" | grep -q "sample-collection"; then
    echo "✅ Sample collection section found"
else
    echo "❌ Sample collection section NOT found"
fi

echo -e "\n10. REAL BROWSER TEST INSTRUCTIONS:"
echo "=================================="
echo "Open: http://localhost:8080/phlebotomy/dashboard.html"
echo ""
echo "Check these things manually:"
echo "1. Does the page load without errors?"
echo "2. Are there any JavaScript errors in console (F12)?"
echo "3. Do the statistics cards show numbers?"
echo "4. Does clicking 'Sample Collection' show pending samples?"
echo "5. Can you click 'Collect' button on a sample?"
echo "6. Does the modal open when you click collect?"
echo "7. Can you submit the collection form?"
echo ""
echo "If ANY of these fail, the dashboard is broken!"

echo -e "\n11. API ENDPOINT VERIFICATION:"
echo "=============================="
echo "Pending samples API: $(curl -s -w "%{http_code}" -o /dev/null http://localhost:8080/sample-collection/pending)"
echo "Visits API: $(curl -s -w "%{http_code}" -o /dev/null http://localhost:8080/visits)"
echo "Test templates API: $(curl -s -w "%{http_code}" -o /dev/null http://localhost:8080/test-templates)"
echo "Samples API: $(curl -s -w "%{http_code}" -o /dev/null http://localhost:8080/samples)"
echo "Lab tests API: $(curl -s -w "%{http_code}" -o /dev/null http://localhost:8080/lab-tests)"

echo -e "\nDone. Now check the browser manually!"
