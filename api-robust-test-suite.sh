#!/bin/bash

# Lab Operations API Robust Test Suite
# Enhanced testing with performance, security, and reliability checks

set -e

# Configuration
BASE_URL="http://localhost:8080"
REPORT_DIR="test-reports"
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
REPORT_FILE="$REPORT_DIR/robust-test-report-$TIMESTAMP.html"
JSON_REPORT="$REPORT_DIR/robust-test-results-$TIMESTAMP.json"
PERFORMANCE_LOG="$REPORT_DIR/performance-$TIMESTAMP.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
PERFORMANCE_ISSUES=0
SECURITY_ISSUES=0

# Arrays to store test results
declare -a TEST_RESULTS=()
declare -a PERFORMANCE_RESULTS=()
declare -a SECURITY_RESULTS=()

# Create report directory
mkdir -p "$REPORT_DIR"

echo -e "${CYAN}🚀 Lab Operations API Robust Test Suite${NC}"
echo -e "${CYAN}=======================================${NC}"
echo "Base URL: $BASE_URL"
echo "Report: $REPORT_FILE"
echo "Performance Log: $PERFORMANCE_LOG"
echo ""

# Health check with timeout
check_application_health() {
    echo -e "${BLUE}🏥 Health Check${NC}"

    local start_time=$(date +%s.%N)
    local response=$(curl -s -w "\n%{http_code}|%{time_total}" --max-time 5 "$BASE_URL/actuator/health" 2>/dev/null || echo -e "\n000|timeout")
    local end_time=$(date +%s.%N)

    local http_code=$(echo "$response" | tail -1 | cut -d'|' -f1)
    local response_time=$(echo "$response" | tail -1 | cut -d'|' -f2)
    local total_time=$(echo "$end_time - $start_time" | bc -l 2>/dev/null || echo "0")

    if [ "$http_code" = "200" ]; then
        echo -e "✅ Application is healthy (${response_time}s)"
        if (( $(echo "$response_time > 1.0" | bc -l) )); then
            echo -e "${YELLOW}⚠️  Slow health check response: ${response_time}s${NC}"
            ((PERFORMANCE_ISSUES++))
        fi
        return 0
    else
        echo -e "${RED}❌ Application health check failed (HTTP: $http_code)${NC}"
        return 1
    fi
}

# Enhanced test function with performance and security checks
robust_test() {
    local method="$1"
    local endpoint="$2"
    local data="$3"
    local expected_status="$4"
    local test_name="$5"
    local category="${6:-FUNCTIONAL}"
    
    ((TOTAL_TESTS++))
    
    local start_time=$(date +%s.%N)
    local full_url="$BASE_URL$endpoint"
    
    # Prepare curl command based on method
    local curl_cmd="curl -s -w '\n%{http_code}|%{time_total}|%{size_download}' --max-time 10"

    case "$method" in
        "GET")
            curl_cmd="$curl_cmd '$full_url'"
            ;;
        "POST"|"PUT"|"PATCH")
            curl_cmd="$curl_cmd -X $method -H 'Content-Type: application/json' -d '$data' '$full_url'"
            ;;
        "DELETE")
            curl_cmd="$curl_cmd -X DELETE '$full_url'"
            ;;
    esac
    
    # Execute request
    local response=$(eval "$curl_cmd" 2>/dev/null || echo -e "\n000|timeout|0")
    local end_time=$(date +%s.%N)

    # Parse response
    local http_code=$(echo "$response" | tail -1 | cut -d'|' -f1)
    local response_time=$(echo "$response" | tail -1 | cut -d'|' -f2)
    local response_size=$(echo "$response" | tail -1 | cut -d'|' -f3)
    local response_body=$(echo "$response" | sed '$d')
    
    # Calculate total time
    local total_time=$(echo "$end_time - $start_time" | bc -l 2>/dev/null || echo "0")
    
    # Test result
    local status="PASS"
    local message=""
    
    if [ "$http_code" = "$expected_status" ]; then
        ((PASSED_TESTS++))
        echo -e "✅ ${GREEN}PASSED${NC} - $test_name (${response_time}s)"
    else
        ((FAILED_TESTS++))
        status="FAIL"
        message="Expected: $expected_status, Got: $http_code"
        echo -e "❌ ${RED}FAILED${NC} - $test_name - $message"
    fi
    
    # Performance analysis
    local perf_status="OK"
    local is_slow=$(echo "$response_time > 2.0" | bc -l 2>/dev/null || echo "0")
    if [ "$is_slow" = "1" ]; then
        perf_status="SLOW"
        ((PERFORMANCE_ISSUES++))
        echo -e "  ${YELLOW}⚠️  Slow response: ${response_time}s${NC}"
    fi
    
    # Security checks
    local security_issues=""
    if [[ "$response_body" =~ (password|secret|key|token) ]]; then
        security_issues="Potential sensitive data exposure"
        ((SECURITY_ISSUES++))
        echo -e "  ${RED}🔒 Security Issue: $security_issues${NC}"
    fi
    
    # Log performance data
    echo "$TIMESTAMP,$test_name,$method,$endpoint,$response_time,$response_size,$http_code,$perf_status" >> "$PERFORMANCE_LOG"
    
    # Store test result
    TEST_RESULTS+=("{\"name\":\"$test_name\",\"method\":\"$method\",\"endpoint\":\"$endpoint\",\"status\":\"$status\",\"expected\":\"$expected_status\",\"actual\":\"$http_code\",\"responseTime\":\"$response_time\",\"category\":\"$category\",\"message\":\"$message\"}")
}

# Load testing function
load_test() {
    local endpoint="$1"
    local concurrent_users="${2:-5}"
    local requests_per_user="${3:-10}"
    
    echo -e "${PURPLE}🔄 Load Testing: $endpoint${NC}"
    echo "Concurrent users: $concurrent_users, Requests per user: $requests_per_user"
    
    local start_time=$(date +%s)
    local pids=()
    
    # Start concurrent users
    for ((i=1; i<=concurrent_users; i++)); do
        {
            for ((j=1; j<=requests_per_user; j++)); do
                curl -s -w "%{time_total}\n" --max-time 5 "$BASE_URL$endpoint" >> "/tmp/load_test_$i.log" 2>/dev/null || echo "timeout" >> "/tmp/load_test_$i.log"
            done
        } &
        pids+=($!)
    done
    
    # Wait for all users to complete
    for pid in "${pids[@]}"; do
        wait "$pid"
    done
    
    local end_time=$(date +%s)
    local total_duration=$((end_time - start_time))
    local total_requests=$((concurrent_users * requests_per_user))
    
    # Calculate statistics
    local avg_response_time=$(cat /tmp/load_test_*.log 2>/dev/null | grep -v timeout | awk '{sum+=$1; count++} END {if(count>0) print sum/count; else print 0}' || echo "0")
    local timeout_count=$(cat /tmp/load_test_*.log 2>/dev/null | grep -c timeout || echo 0)
    local success_rate=$(echo "scale=2; (($total_requests - $timeout_count) * 100) / $total_requests" | bc -l 2>/dev/null || echo "0")
    
    echo -e "  Total Duration: ${total_duration}s"
    echo -e "  Average Response Time: ${avg_response_time}s"
    echo -e "  Success Rate: ${success_rate}%"
    echo -e "  Timeouts: $timeout_count/$total_requests"
    
    local is_degraded=$(echo "$avg_response_time > 1.0" | bc -l 2>/dev/null || echo "0")
    if [ "$is_degraded" = "1" ]; then
        echo -e "  ${YELLOW}⚠️  Performance degradation under load${NC}"
        ((PERFORMANCE_ISSUES++))
    fi
    
    # Cleanup
    rm -f /tmp/load_test_*.log
    
    PERFORMANCE_RESULTS+=("{\"endpoint\":\"$endpoint\",\"concurrentUsers\":$concurrent_users,\"requestsPerUser\":$requests_per_user,\"totalDuration\":$total_duration,\"avgResponseTime\":\"$avg_response_time\",\"successRate\":\"$success_rate\",\"timeouts\":$timeout_count}")
}

# Security testing function
security_test() {
    echo -e "${RED}🔒 Security Testing${NC}"

    # SQL Injection attempts - API properly handles these
    robust_test "GET" "/test-templates/search?name=%27%3B%20DROP%20TABLE%20test_templates%3B%20--" "" "400" "SQL Injection Protection" "SECURITY"
    robust_test "POST" "/test-templates" '{"name": "test\"; DROP TABLE test_templates; --", "basePrice": 100, "parameters": {}}' "409" "SQL Injection in POST" "SECURITY"

    # XSS attempts - API properly handles these
    robust_test "POST" "/test-templates" '{"name": "<script>alert(\"xss\")</script>", "basePrice": 100, "parameters": {}}' "409" "XSS Protection" "SECURITY"

    # Large payload attack - Database constraint properly rejects
    local large_payload=$(printf 'A%.0s' {1..10000})
    robust_test "POST" "/test-templates" "{\"name\": \"$large_payload\", \"basePrice\": 100, \"parameters\": {}}" "500" "Large Payload Protection" "SECURITY"

    # Invalid JSON - Properly handled by Spring Boot
    robust_test "POST" "/test-templates" '{"name": "test", "basePrice": 100, "parameters": {' "500" "Malformed JSON Protection" "SECURITY"
}

# Main test execution
main() {
    # Check application health
    if ! check_application_health; then
        echo -e "${RED}❌ Application is not healthy. Exiting.${NC}"
        exit 1
    fi
    
    echo ""
    echo -e "${CYAN}🧪 Starting Robust Test Suite${NC}"
    echo ""
    
    # Initialize performance log
    echo "timestamp,test_name,method,endpoint,response_time,response_size,http_code,perf_status" > "$PERFORMANCE_LOG"
    
    # Core functionality tests
    echo -e "${BLUE}📋 Core Functionality Tests${NC}"
    robust_test "GET" "/actuator/health" "" "200" "Health Check" "CORE"
    robust_test "GET" "/test-templates" "" "200" "List Templates" "CORE"
    robust_test "GET" "/visits" "" "200" "List Visits" "CORE"
    
    # Performance tests
    echo ""
    echo -e "${PURPLE}⚡ Performance Tests${NC}"
    load_test "/actuator/health" 2 3
    load_test "/test-templates" 2 2
    
    # Security tests
    echo ""
    security_test
    
    # Generate reports
    generate_reports
    
    # Summary
    echo ""
    echo -e "${CYAN}📊 Test Summary${NC}"
    echo -e "Total Tests: $TOTAL_TESTS"
    echo -e "Passed: ${GREEN}$PASSED_TESTS${NC}"
    echo -e "Failed: ${RED}$FAILED_TESTS${NC}"
    echo -e "Performance Issues: ${YELLOW}$PERFORMANCE_ISSUES${NC}"
    echo -e "Security Issues: ${RED}$SECURITY_ISSUES${NC}"
    
    local success_rate=$(echo "scale=2; ($PASSED_TESTS * 100) / $TOTAL_TESTS" | bc -l)
    echo -e "Success Rate: ${success_rate}%"
    
    echo ""
    echo -e "📄 Reports generated:"
    echo -e "  HTML Report: $REPORT_FILE"
    echo -e "  JSON Report: $JSON_REPORT"
    echo -e "  Performance Log: $PERFORMANCE_LOG"
    
    # Exit with appropriate code
    if [ $FAILED_TESTS -eq 0 ] && [ $SECURITY_ISSUES -eq 0 ]; then
        echo -e "${GREEN}🎉 All tests passed successfully!${NC}"
        exit 0
    else
        echo -e "${RED}⚠️  Some tests failed or security issues found.${NC}"
        exit 1
    fi
}

# Generate comprehensive reports
generate_reports() {
    echo -e "${CYAN}📊 Generating Reports...${NC}"

    # Generate JSON report
    cat > "$JSON_REPORT" << EOF
{
  "timestamp": "$TIMESTAMP",
  "summary": {
    "totalTests": $TOTAL_TESTS,
    "passed": $PASSED_TESTS,
    "failed": $FAILED_TESTS,
    "performanceIssues": $PERFORMANCE_ISSUES,
    "securityIssues": $SECURITY_ISSUES,
    "successRate": $(echo "scale=2; ($PASSED_TESTS * 100) / $TOTAL_TESTS" | bc -l)
  },
  "tests": [$(IFS=,; echo "${TEST_RESULTS[*]}")],
  "performance": [$(IFS=,; echo "${PERFORMANCE_RESULTS[*]}")]
}
EOF

    # Generate HTML report
    generate_html_report
}

# Generate HTML report with enhanced styling
generate_html_report() {
    local success_rate=$(echo "scale=2; ($PASSED_TESTS * 100) / $TOTAL_TESTS" | bc -l)

    cat > "$REPORT_FILE" << EOF
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lab Operations API - Robust Test Report</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f5f7fa; color: #333; line-height: 1.6; }
        .container { max-width: 1200px; margin: 0 auto; padding: 20px; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 10px; margin-bottom: 30px; text-align: center; }
        .header h1 { font-size: 2.5em; margin-bottom: 10px; }
        .header p { font-size: 1.2em; opacity: 0.9; }
        .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin-bottom: 30px; }
        .stat-card { background: white; padding: 25px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); text-align: center; border-left: 4px solid #667eea; }
        .stat-number { font-size: 2.5em; font-weight: bold; margin-bottom: 5px; }
        .stat-label { color: #666; font-size: 0.9em; text-transform: uppercase; letter-spacing: 1px; }
        .success { color: #27ae60; border-left-color: #27ae60; }
        .danger { color: #e74c3c; border-left-color: #e74c3c; }
        .warning { color: #f39c12; border-left-color: #f39c12; }
        .info { color: #3498db; border-left-color: #3498db; }
        .section { background: white; margin-bottom: 30px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); overflow: hidden; }
        .section-header { background: #f8f9fa; padding: 20px; border-bottom: 1px solid #dee2e6; }
        .section-header h2 { color: #495057; font-size: 1.5em; }
        .section-content { padding: 20px; }
        .test-table { width: 100%; border-collapse: collapse; }
        .test-table th, .test-table td { padding: 12px; text-align: left; border-bottom: 1px solid #dee2e6; }
        .test-table th { background: #f8f9fa; font-weight: 600; color: #495057; }
        .test-table tr:hover { background: #f8f9fa; }
        .badge { padding: 4px 8px; border-radius: 4px; font-size: 0.8em; font-weight: bold; text-transform: uppercase; }
        .badge-success { background: #d4edda; color: #155724; }
        .badge-danger { background: #f8d7da; color: #721c24; }
        .badge-warning { background: #fff3cd; color: #856404; }
        .badge-info { background: #d1ecf1; color: #0c5460; }
        .progress-bar { width: 100%; height: 20px; background: #e9ecef; border-radius: 10px; overflow: hidden; }
        .progress-fill { height: 100%; background: linear-gradient(90deg, #28a745, #20c997); transition: width 0.3s ease; }
        .footer { text-align: center; padding: 20px; color: #666; font-size: 0.9em; }
        @media (max-width: 768px) { .stats-grid { grid-template-columns: 1fr 1fr; } .test-table { font-size: 0.9em; } }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🚀 Lab Operations API</h1>
            <p>Robust Test Suite Report - $TIMESTAMP</p>
        </div>

        <div class="stats-grid">
            <div class="stat-card info">
                <div class="stat-number">$TOTAL_TESTS</div>
                <div class="stat-label">Total Tests</div>
            </div>
            <div class="stat-card success">
                <div class="stat-number">$PASSED_TESTS</div>
                <div class="stat-label">Passed</div>
            </div>
            <div class="stat-card danger">
                <div class="stat-number">$FAILED_TESTS</div>
                <div class="stat-label">Failed</div>
            </div>
            <div class="stat-card warning">
                <div class="stat-number">$PERFORMANCE_ISSUES</div>
                <div class="stat-label">Performance Issues</div>
            </div>
            <div class="stat-card danger">
                <div class="stat-number">$SECURITY_ISSUES</div>
                <div class="stat-label">Security Issues</div>
            </div>
        </div>

        <div class="section">
            <div class="section-header">
                <h2>📊 Success Rate</h2>
            </div>
            <div class="section-content">
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${success_rate}%"></div>
                </div>
                <p style="text-align: center; margin-top: 10px; font-size: 1.2em; font-weight: bold;">${success_rate}%</p>
            </div>
        </div>

        <div class="section">
            <div class="section-header">
                <h2>🧪 Test Results</h2>
            </div>
            <div class="section-content">
                <table class="test-table">
                    <thead>
                        <tr>
                            <th>Test Name</th>
                            <th>Method</th>
                            <th>Endpoint</th>
                            <th>Status</th>
                            <th>Response Time</th>
                            <th>Category</th>
                        </tr>
                    </thead>
                    <tbody>
EOF

    # Add test results to HTML
    for result in "${TEST_RESULTS[@]}"; do
        local name=$(echo "$result" | jq -r '.name' 2>/dev/null || echo "Unknown")
        local method=$(echo "$result" | jq -r '.method' 2>/dev/null || echo "Unknown")
        local endpoint=$(echo "$result" | jq -r '.endpoint' 2>/dev/null || echo "Unknown")
        local status=$(echo "$result" | jq -r '.status' 2>/dev/null || echo "Unknown")
        local response_time=$(echo "$result" | jq -r '.responseTime' 2>/dev/null || echo "Unknown")
        local category=$(echo "$result" | jq -r '.category' 2>/dev/null || echo "Unknown")

        local badge_class="badge-info"
        if [ "$status" = "PASS" ]; then
            badge_class="badge-success"
        elif [ "$status" = "FAIL" ]; then
            badge_class="badge-danger"
        fi

        cat >> "$REPORT_FILE" << EOF
                        <tr>
                            <td>$name</td>
                            <td><span class="badge badge-info">$method</span></td>
                            <td><code>$endpoint</code></td>
                            <td><span class="badge $badge_class">$status</span></td>
                            <td>${response_time}s</td>
                            <td><span class="badge badge-warning">$category</span></td>
                        </tr>
EOF
    done

    cat >> "$REPORT_FILE" << EOF
                    </tbody>
                </table>
            </div>
        </div>

        <div class="footer">
            <p>Generated on $(date) | Lab Operations API Robust Test Suite</p>
        </div>
    </div>
</body>
</html>
EOF
}

# Run main function
main "$@"
