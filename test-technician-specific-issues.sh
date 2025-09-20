#!/bin/bash

echo "🔬 SPECIFIC LAB TECHNICIAN ISSUES TEST"
echo "====================================="

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "\n${BLUE}Phase 1: Testing Dashboard Access${NC}"

# Test dashboard HTML directly
echo "Testing dashboard HTML access..."
dashboard_response=$(curl -s -w "HTTP_CODE:%{http_code}" "http://localhost:8080/technician/dashboard.html")
dashboard_code=$(echo "$dashboard_response" | grep -o 'HTTP_CODE:.*' | cut -d: -f2)
dashboard_content=$(echo "$dashboard_response" | sed 's/HTTP_CODE:.*//')

if [ "$dashboard_code" = "200" ]; then
    echo -e "${GREEN}✅ Dashboard HTML accessible (HTTP 200)${NC}"
    
    # Check if HTML contains expected elements
    if echo "$dashboard_content" | grep -q "Lab Technician Dashboard"; then
        echo -e "${GREEN}✅ Dashboard contains expected title${NC}"
    else
        echo -e "${RED}❌ Dashboard missing expected title${NC}"
    fi
    
    if echo "$dashboard_content" | grep -q "technician.js"; then
        echo -e "${GREEN}✅ Dashboard references technician.js${NC}"
    else
        echo -e "${RED}❌ Dashboard missing technician.js reference${NC}"
    fi
    
    if echo "$dashboard_content" | grep -q "pending-tests"; then
        echo -e "${GREEN}✅ Dashboard contains statistics elements${NC}"
    else
        echo -e "${RED}❌ Dashboard missing statistics elements${NC}"
    fi
    
elif [ "$dashboard_code" = "302" ]; then
    echo -e "${YELLOW}⚠️ Dashboard redirecting (HTTP 302) - might be authentication${NC}"
    # Follow redirect
    redirect_location=$(curl -s -I "http://localhost:8080/technician/dashboard.html" | grep -i location | cut -d' ' -f2 | tr -d '\r')
    echo "Redirect location: $redirect_location"
else
    echo -e "${RED}❌ Dashboard not accessible (HTTP $dashboard_code)${NC}"
fi

echo -e "\n${BLUE}Phase 2: Testing JavaScript and CSS Resources${NC}"

# Test JavaScript
echo "Testing technician.js..."
js_response=$(curl -s -w "HTTP_CODE:%{http_code}" "http://localhost:8080/js/technician.js")
js_code=$(echo "$js_response" | grep -o 'HTTP_CODE:.*' | cut -d: -f2)

if [ "$js_code" = "200" ]; then
    echo -e "${GREEN}✅ Technician JavaScript accessible${NC}"
    js_content=$(echo "$js_response" | sed 's/HTTP_CODE:.*//')
    
    # Check for key JavaScript elements
    if echo "$js_content" | grep -q "TechnicianApp"; then
        echo -e "${GREEN}✅ TechnicianApp class found${NC}"
    else
        echo -e "${RED}❌ TechnicianApp class missing${NC}"
    fi
    
    if echo "$js_content" | grep -q "DOMContentLoaded"; then
        echo -e "${GREEN}✅ DOM loading fix applied${NC}"
    else
        echo -e "${RED}❌ DOM loading fix missing${NC}"
    fi
    
    if echo "$js_content" | grep -q "window.technicianApp"; then
        echo -e "${GREEN}✅ Global window assignment found${NC}"
    else
        echo -e "${RED}❌ Global window assignment missing${NC}"
    fi
else
    echo -e "${RED}❌ Technician JavaScript not accessible (HTTP $js_code)${NC}"
fi

# Test CSS
echo "Testing technician.css..."
css_response=$(curl -s -w "HTTP_CODE:%{http_code}" "http://localhost:8080/css/technician.css" -o /dev/null)
css_code=$(echo "$css_response" | grep -o 'HTTP_CODE:.*' | cut -d: -f2)

if [ "$css_code" = "200" ]; then
    echo -e "${GREEN}✅ Technician CSS accessible${NC}"
else
    echo -e "${RED}❌ Technician CSS not accessible (HTTP $css_code)${NC}"
fi

echo -e "\n${BLUE}Phase 3: Testing API Endpoints Used by Dashboard${NC}"

# Test specific endpoints the dashboard uses
endpoints=(
    "/api/v1/equipment:Equipment API"
    "/lab-tests:Lab Tests API"
    "/lab-tests/pending:Pending Tests API"
    "/lab-tests/statistics:Statistics API"
    "/samples:Samples API"
    "/visits:Visits API"
)

for endpoint_info in "${endpoints[@]}"; do
    endpoint=$(echo "$endpoint_info" | cut -d: -f1)
    name=$(echo "$endpoint_info" | cut -d: -f2)
    
    echo "Testing $name ($endpoint)..."
    response=$(curl -s -w "HTTP_CODE:%{http_code}" "http://localhost:8080$endpoint")
    code=$(echo "$response" | grep -o 'HTTP_CODE:.*' | cut -d: -f2)
    data=$(echo "$response" | sed 's/HTTP_CODE:.*//')
    
    if [ "$code" = "200" ]; then
        # Try to parse as JSON and count items
        count=$(echo "$data" | jq 'length' 2>/dev/null || echo "N/A")
        echo -e "${GREEN}✅ $name: $count items${NC}"
    else
        echo -e "${RED}❌ $name: HTTP $code${NC}"
        if [ ${#data} -lt 200 ]; then
            echo "   Response: $data"
        fi
    fi
done

echo -e "\n${BLUE}Phase 4: Testing Specific Dashboard Functionality${NC}"

# Test if we can start a test (if there are any)
echo "Testing test processing functionality..."
tests_response=$(curl -s "http://localhost:8080/lab-tests")
tests_count=$(echo "$tests_response" | jq 'length' 2>/dev/null || echo "0")

if [ "$tests_count" -gt 0 ]; then
    echo -e "${GREEN}✅ Found $tests_count lab tests for processing${NC}"
    
    # Get first test ID
    first_test_id=$(echo "$tests_response" | jq -r '.[0].testId' 2>/dev/null)
    if [ "$first_test_id" != "null" ] && [ "$first_test_id" != "" ]; then
        echo "First test ID: $first_test_id"
        
        # Test if we can get test details
        test_detail_response=$(curl -s -w "HTTP_CODE:%{http_code}" "http://localhost:8080/lab-tests/$first_test_id")
        test_detail_code=$(echo "$test_detail_response" | grep -o 'HTTP_CODE:.*' | cut -d: -f2)
        
        if [ "$test_detail_code" = "200" ]; then
            echo -e "${GREEN}✅ Can retrieve individual test details${NC}"
        else
            echo -e "${YELLOW}⚠️ Cannot retrieve individual test details (HTTP $test_detail_code)${NC}"
        fi
    fi
else
    echo -e "${YELLOW}⚠️ No lab tests found for processing${NC}"
fi

echo -e "\n${BLUE}Phase 5: Testing Equipment Management${NC}"

equipment_response=$(curl -s "http://localhost:8080/api/v1/equipment")
equipment_count=$(echo "$equipment_response" | jq 'length' 2>/dev/null || echo "0")

if [ "$equipment_count" -gt 0 ]; then
    echo -e "${GREEN}✅ Found $equipment_count equipment items${NC}"
    
    # Show equipment details
    echo "Equipment details:"
    echo "$equipment_response" | jq -r '.[] | "  • \(.name) (\(.manufacturer) \(.model)) - Status: \(.status)"' 2>/dev/null || echo "  • Equipment data available but not parseable"
else
    echo -e "${YELLOW}⚠️ No equipment found${NC}"
fi

echo -e "\n${BLUE}Phase 6: Security and Authentication Check${NC}"

# Check if technician endpoints are properly secured
echo "Testing technician endpoint security..."
technician_response=$(curl -s -w "HTTP_CODE:%{http_code}" "http://localhost:8080/technician/dashboard.html")
technician_code=$(echo "$technician_response" | grep -o 'HTTP_CODE:.*' | cut -d: -f2)

case $technician_code in
    200)
        echo -e "${GREEN}✅ Technician dashboard accessible (no authentication required)${NC}"
        ;;
    302)
        echo -e "${YELLOW}⚠️ Technician dashboard redirecting (authentication required)${NC}"
        ;;
    401)
        echo -e "${RED}❌ Technician dashboard requires authentication${NC}"
        ;;
    403)
        echo -e "${RED}❌ Technician dashboard access forbidden${NC}"
        ;;
    *)
        echo -e "${RED}❌ Technician dashboard unexpected response: HTTP $technician_code${NC}"
        ;;
esac

echo -e "\n${BLUE}Summary and Recommendations${NC}"

# Count successful tests
total_tests=10
passed_tests=0

[ "$dashboard_code" = "200" ] && ((passed_tests++))
[ "$js_code" = "200" ] && ((passed_tests++))
[ "$css_code" = "200" ] && ((passed_tests++))
[ "$tests_count" -gt 0 ] && ((passed_tests++))
[ "$equipment_count" -gt 0 ] && ((passed_tests++))

success_rate=$((passed_tests * 100 / total_tests))

echo "Test Results:"
echo "• Passed: $passed_tests/$total_tests"
echo "• Success Rate: $success_rate%"

if [ $success_rate -ge 80 ]; then
    echo -e "\n${GREEN}🎉 Lab Technician Dashboard appears to be mostly functional!${NC}"
    echo -e "${GREEN}✅ Most core components are working${NC}"
elif [ $success_rate -ge 60 ]; then
    echo -e "\n${YELLOW}⚠️ Lab Technician Dashboard has some issues${NC}"
    echo -e "${YELLOW}🔧 Some components need attention${NC}"
else
    echo -e "\n${RED}❌ Lab Technician Dashboard has significant issues${NC}"
    echo -e "${RED}🚨 Major fixes required${NC}"
fi

echo -e "\n${BLUE}Troubleshooting Steps:${NC}"
echo "1. Open browser developer tools (F12)"
echo "2. Go to http://localhost:8080/technician/dashboard.html"
echo "3. Check Console tab for JavaScript errors"
echo "4. Check Network tab for failed resource loads"
echo "5. Verify that data is loading in the dashboard sections"

echo -e "\n${GREEN}=== SPECIFIC ISSUES TEST COMPLETED ===${NC}"
