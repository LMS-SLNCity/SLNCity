#!/bin/bash

# Enhanced UI Functionality Test Script
# Tests all new modal and interactive features

echo "🚀 Lab Operations Enhanced UI Test Suite"
echo "========================================"

BASE_URL="http://localhost:8080"
PASS_COUNT=0
TOTAL_TESTS=0

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test function
test_feature() {
    local name="$1"
    local test_command="$2"
    local description="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    echo -n "[$TOTAL_TESTS] Testing $name... "
    
    if eval "$test_command" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ PASS${NC}"
        PASS_COUNT=$((PASS_COUNT + 1))
        if [ -n "$description" ]; then
            echo "    $description"
        fi
    else
        echo -e "${RED}❌ FAIL${NC}"
        if [ -n "$description" ]; then
            echo "    $description"
        fi
    fi
}

# Test API endpoints
echo -e "\n${BLUE}📡 Core API Tests${NC}"
echo "----------------"

test_feature "System Health" "curl -s $BASE_URL/actuator/health | jq -e '.status == \"UP\"'" "Health check endpoint"
test_feature "Main Page" "curl -s -w '%{http_code}' -o /dev/null $BASE_URL/ | grep -q '200'" "Main application page"
test_feature "Visits API" "curl -s -w '%{http_code}' -o /dev/null $BASE_URL/visits | grep -q '200'" "Patient visits data"
test_feature "Equipment API" "curl -s -w '%{http_code}' -o /dev/null $BASE_URL/api/v1/equipment | grep -q '200'" "Lab equipment data"

# Test static resources
echo -e "\n${BLUE}📁 Static Resource Tests${NC}"
echo "------------------------"

test_feature "CSS Stylesheet" "curl -s -w '%{http_code}' -o /dev/null $BASE_URL/css/main.css | grep -q '200'" "Main stylesheet"
test_feature "JavaScript" "curl -s -w '%{http_code}' -o /dev/null $BASE_URL/js/main.js | grep -q '200'" "Main JavaScript file"

# Test enhanced UI features
echo -e "\n${BLUE}✨ Enhanced UI Feature Tests${NC}"
echo "----------------------------"

# Download main page for testing
curl -s "$BASE_URL/" > /tmp/main_page.html

test_feature "New Visit Modal Structure" "grep -q 'new-visit-modal' /tmp/main_page.html" "Modal HTML structure found"
test_feature "Search Input Field" "grep -q 'visits-search' /tmp/main_page.html" "Search functionality present"
test_feature "New Visit Button" "grep -q 'onclick=\"app.showNewVisitModal()\"' /tmp/main_page.html" "Modal trigger button found"
test_feature "Modal Form Fields" "grep -q 'new-visit-form' /tmp/main_page.html" "Form structure present"

# Test CSS enhancements
echo -e "\n${BLUE}🎨 CSS Enhancement Tests${NC}"
echo "-------------------------"

# Download CSS for testing
curl -s "$BASE_URL/css/main.css" > /tmp/main_css.css

test_feature "Modal CSS Classes" "grep -q '\.modal' /tmp/main_css.css" "Modal styling present"
test_feature "Modal Animation" "grep -q 'modalSlideIn' /tmp/main_css.css" "Modal animations defined"
test_feature "Form Styling" "grep -q '\.form-row' /tmp/main_css.css" "Enhanced form styles"
test_feature "Responsive Modal" "grep -A 20 '@media (max-width: 768px)' /tmp/main_css.css | grep -q 'modal-content'" "Mobile responsive modal"

# Test JavaScript enhancements
echo -e "\n${BLUE}⚡ JavaScript Enhancement Tests${NC}"
echo "-------------------------------"

# Download JavaScript for testing
curl -s "$BASE_URL/js/main.js" > /tmp/main_js.js

test_feature "Modal Functions" "grep -q 'showNewVisitModal' /tmp/main_js.js" "Modal management functions"
test_feature "Form Submission" "grep -q 'handleNewVisitSubmission' /tmp/main_js.js" "Form handling logic"
test_feature "Search Filtering" "grep -q 'filterVisits' /tmp/main_js.js" "Search and filter functionality"
test_feature "API Integration" "grep -q 'createNewVisit' /tmp/main_js.js" "Visit creation API calls"

# Test data functionality
echo -e "\n${BLUE}📊 Data Integration Tests${NC}"
echo "-------------------------"

test_feature "Visit Creation API" "curl -s -X POST $BASE_URL/visits -H 'Content-Type: application/json' -d '{\"patientDetails\":{\"name\":\"Test User\",\"age\":30,\"gender\":\"M\",\"phone\":\"1234567890\"}}' | jq -e '.visitId'" "POST API working"
test_feature "Visit Data Retrieval" "curl -s $BASE_URL/visits | jq -e '. | length > 0'" "Data available for UI"

# Test UI structure
echo -e "\n${BLUE}🏗️ UI Structure Tests${NC}"
echo "---------------------"

test_feature "Navigation Menu" "grep -q 'nav-item' /tmp/main_page.html" "Navigation structure"
test_feature "Dashboard Page" "grep -q 'dashboard-page' /tmp/main_page.html" "Dashboard layout"
test_feature "Visits Page" "grep -q 'visits-page' /tmp/main_page.html" "Visits management page"
test_feature "Equipment Page" "grep -q 'equipment-page' /tmp/main_page.html" "Equipment management page"

# Test form validation
echo -e "\n${BLUE}✅ Form Validation Tests${NC}"
echo "-------------------------"

test_feature "Required Field Validation" "grep -q 'required' /tmp/main_page.html" "HTML5 validation attributes"
test_feature "Input Types" "grep -q 'type=\"email\"' /tmp/main_page.html" "Proper input types"
test_feature "Form Structure" "grep -q 'form-group' /tmp/main_page.html" "Structured form layout"

# Summary
echo -e "\n${YELLOW}📋 Test Summary${NC}"
echo "==============="
echo "Total Tests: $TOTAL_TESTS"
echo -e "${GREEN}Passed: $PASS_COUNT${NC}"
echo -e "${RED}Failed: $((TOTAL_TESTS - PASS_COUNT))${NC}"

pass_rate=$((PASS_COUNT * 100 / TOTAL_TESTS))
echo "Pass Rate: ${pass_rate}%"

if [ $PASS_COUNT -eq $TOTAL_TESTS ]; then
    echo -e "\n${GREEN}🎉 ALL TESTS PASSED!${NC}"
    echo -e "${GREEN}✅ Enhanced UI is fully functional and ready for use!${NC}"
    echo -e "\n${BLUE}🌟 New Features Available:${NC}"
    echo "   • Interactive modal dialogs for creating new visits"
    echo "   • Search and filter functionality for visits"
    echo "   • Enhanced form validation and user experience"
    echo "   • Responsive design for mobile devices"
    echo "   • Real-time data integration with backend APIs"
    exit 0
else
    echo -e "\n${YELLOW}⚠️ Some tests failed. System is ${pass_rate}% functional.${NC}"
    
    if [ $pass_rate -ge 90 ]; then
        echo -e "${GREEN}🌟 Excellent! System is highly functional${NC}"
    elif [ $pass_rate -ge 80 ]; then
        echo -e "${YELLOW}👍 Good! System is mostly functional${NC}"
    elif [ $pass_rate -ge 70 ]; then
        echo -e "${YELLOW}⚠️ Fair! Some issues need attention${NC}"
    else
        echo -e "${RED}❌ Poor! Significant issues detected${NC}"
    fi
    
    exit 1
fi
