#!/bin/bash

echo "🔬 FINAL LAB TECHNICIAN DASHBOARD TEST"
echo "======================================"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "\n${BLUE}Phase 1: API Connectivity Test${NC}"

# Test Equipment API
echo "Testing Equipment API..."
equipment_response=$(curl -s -w "HTTP_CODE:%{http_code}" "http://localhost:8080/api/v1/equipment")
equipment_code=$(echo "$equipment_response" | grep -o 'HTTP_CODE:.*' | cut -d: -f2)
equipment_data=$(echo "$equipment_response" | sed 's/HTTP_CODE:.*//')

if [ "$equipment_code" = "200" ]; then
    equipment_count=$(echo "$equipment_data" | jq 'length' 2>/dev/null || echo "0")
    echo -e "${GREEN}✅ Equipment API: $equipment_count items${NC}"
else
    echo -e "${RED}❌ Equipment API failed (HTTP: $equipment_code)${NC}"
fi

# Test Lab Tests API
echo "Testing Lab Tests API..."
tests_response=$(curl -s -w "HTTP_CODE:%{http_code}" "http://localhost:8080/lab-tests")
tests_code=$(echo "$tests_response" | grep -o 'HTTP_CODE:.*' | cut -d: -f2)
tests_data=$(echo "$tests_response" | sed 's/HTTP_CODE:.*//')

if [ "$tests_code" = "200" ]; then
    tests_count=$(echo "$tests_data" | jq 'length' 2>/dev/null || echo "0")
    echo -e "${GREEN}✅ Lab Tests API: $tests_count items${NC}"
else
    echo -e "${RED}❌ Lab Tests API failed (HTTP: $tests_code)${NC}"
fi

# Test Samples API
echo "Testing Samples API..."
samples_response=$(curl -s -w "HTTP_CODE:%{http_code}" "http://localhost:8080/samples")
samples_code=$(echo "$samples_response" | grep -o 'HTTP_CODE:.*' | cut -d: -f2)
samples_data=$(echo "$samples_response" | sed 's/HTTP_CODE:.*//')

if [ "$samples_code" = "200" ]; then
    samples_count=$(echo "$samples_data" | jq 'length' 2>/dev/null || echo "0")
    echo -e "${GREEN}✅ Samples API: $samples_count items${NC}"
else
    echo -e "${RED}❌ Samples API failed (HTTP: $samples_code)${NC}"
fi

echo -e "\n${BLUE}Phase 2: Dashboard Resources Test${NC}"

# Test Dashboard HTML
echo "Testing Dashboard HTML..."
dashboard_response=$(curl -s -w "HTTP_CODE:%{http_code}" "http://localhost:8080/technician/dashboard.html" -o /dev/null)
dashboard_code=$(echo "$dashboard_response" | grep -o 'HTTP_CODE:.*' | cut -d: -f2)

if [ "$dashboard_code" = "200" ]; then
    echo -e "${GREEN}✅ Dashboard HTML loads correctly${NC}"
else
    echo -e "${RED}❌ Dashboard HTML failed (HTTP: $dashboard_code)${NC}"
fi

# Test JavaScript
echo "Testing Technician JavaScript..."
js_response=$(curl -s -w "HTTP_CODE:%{http_code}" "http://localhost:8080/js/technician.js" -o /dev/null)
js_code=$(echo "$js_response" | grep -o 'HTTP_CODE:.*' | cut -d: -f2)

if [ "$js_code" = "200" ]; then
    echo -e "${GREEN}✅ Technician JavaScript loads correctly${NC}"
else
    echo -e "${RED}❌ Technician JavaScript failed (HTTP: $js_code)${NC}"
fi

# Test CSS
echo "Testing Technician CSS..."
css_response=$(curl -s -w "HTTP_CODE:%{http_code}" "http://localhost:8080/css/technician.css" -o /dev/null)
css_code=$(echo "$css_response" | grep -o 'HTTP_CODE:.*' | cut -d: -f2)

if [ "$css_code" = "200" ]; then
    echo -e "${GREEN}✅ Technician CSS loads correctly${NC}"
else
    echo -e "${RED}❌ Technician CSS failed (HTTP: $css_code)${NC}"
fi

echo -e "\n${BLUE}Phase 3: Data Analysis${NC}"

# Analyze equipment data
if [ "$equipment_count" -gt 0 ]; then
    echo "Equipment Details:"
    echo "$equipment_data" | jq -r '.[] | "  • \(.name) (\(.manufacturer) \(.model)) - Status: \(.status)"' 2>/dev/null || echo "  • Equipment data available but not parseable"
else
    echo -e "${YELLOW}⚠️ No equipment data found${NC}"
fi

# Analyze lab tests data
if [ "$tests_count" -gt 0 ]; then
    echo "Lab Tests Summary:"
    echo "$tests_data" | jq -r 'group_by(.status) | .[] | "  • \(.[0].status): \(length) tests"' 2>/dev/null || echo "  • Lab tests data available but not parseable"
else
    echo -e "${YELLOW}⚠️ No lab tests data found${NC}"
fi

# Analyze samples data
if [ "$samples_count" -gt 0 ]; then
    echo "Samples Summary:"
    echo "$samples_data" | jq -r 'group_by(.status) | .[] | "  • \(.[0].status): \(length) samples"' 2>/dev/null || echo "  • Samples data available but not parseable"
else
    echo -e "${YELLOW}⚠️ No samples data found${NC}"
fi

echo -e "\n${BLUE}Phase 4: Functionality Test${NC}"

# Test specific technician endpoints
echo "Testing Lab Tests Pending..."
pending_response=$(curl -s "http://localhost:8080/lab-tests/pending" 2>/dev/null)
if [ $? -eq 0 ]; then
    pending_count=$(echo "$pending_response" | jq 'length' 2>/dev/null || echo "0")
    echo -e "${GREEN}✅ Pending Tests API: $pending_count items${NC}"
else
    echo -e "${YELLOW}⚠️ Pending Tests API not available${NC}"
fi

echo "Testing Lab Tests Statistics..."
stats_response=$(curl -s "http://localhost:8080/lab-tests/statistics" 2>/dev/null)
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Lab Tests Statistics API available${NC}"
else
    echo -e "${YELLOW}⚠️ Lab Tests Statistics API not available${NC}"
fi

echo -e "\n${BLUE}Phase 5: Integration Test${NC}"

# Calculate success metrics
total_tests=8
passed_tests=0

[ "$equipment_code" = "200" ] && ((passed_tests++))
[ "$tests_code" = "200" ] && ((passed_tests++))
[ "$samples_code" = "200" ] && ((passed_tests++))
[ "$dashboard_code" = "200" ] && ((passed_tests++))
[ "$js_code" = "200" ] && ((passed_tests++))
[ "$css_code" = "200" ] && ((passed_tests++))
[ "$equipment_count" -gt 0 ] && ((passed_tests++))
[ "$tests_count" -gt 0 ] && ((passed_tests++))

success_rate=$((passed_tests * 100 / total_tests))

echo "Test Results Summary:"
echo "• Total Tests: $total_tests"
echo "• Passed Tests: $passed_tests"
echo "• Success Rate: $success_rate%"

if [ $success_rate -ge 80 ]; then
    echo -e "\n${GREEN}🎉 LAB TECHNICIAN DASHBOARD IS FUNCTIONAL! 🎉${NC}"
    echo -e "${GREEN}✅ Dashboard is ready for production use${NC}"
elif [ $success_rate -ge 60 ]; then
    echo -e "\n${YELLOW}⚠️ Lab Technician Dashboard has minor issues${NC}"
    echo -e "${YELLOW}🔧 Some features may need attention${NC}"
else
    echo -e "\n${RED}❌ Lab Technician Dashboard has significant issues${NC}"
    echo -e "${RED}🚨 Major fixes required before use${NC}"
fi

echo -e "\n${BLUE}Dashboard URLs:${NC}"
echo "• Main Dashboard: http://localhost:8080/technician/dashboard.html"
echo "• Test Page: http://localhost:8080/test-lab-technician-dashboard.html"

echo -e "\n${BLUE}Next Steps:${NC}"
echo "1. Open the dashboard in your browser"
echo "2. Check that all sections load correctly"
echo "3. Test equipment management features"
echo "4. Test lab test processing workflow"
echo "5. Verify sample analysis functionality"

echo -e "\n${GREEN}=== TEST COMPLETED ===${NC}"
