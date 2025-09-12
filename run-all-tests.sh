#!/bin/bash

# Lab Operations API - Complete Test Suite Orchestrator
# Runs all testing suites in sequence with comprehensive reporting

set -e

# Configuration
BASE_URL="http://localhost:8080"
REPORT_DIR="test-reports"
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
MASTER_REPORT="$REPORT_DIR/master-test-report-$TIMESTAMP.html"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# Test suite results
declare -A SUITE_RESULTS
declare -A SUITE_DURATIONS
declare -A SUITE_DETAILS

mkdir -p "$REPORT_DIR"

echo -e "${CYAN}🎯 Lab Operations API - Master Test Suite${NC}"
echo -e "${CYAN}=======================================${NC}"
echo "Timestamp: $TIMESTAMP"
echo "Base URL: $BASE_URL"
echo "Report Directory: $REPORT_DIR"
echo ""

# Function to run a test suite and capture results
run_test_suite() {
    local suite_name="$1"
    local script_path="$2"
    local description="$3"
    
    echo -e "${BLUE}🚀 Running: $suite_name${NC}"
    echo -e "Description: $description"
    echo -e "Script: $script_path"
    echo ""
    
    local start_time=$(date +%s)
    local exit_code=0
    
    # Check if script exists and is executable
    if [ ! -f "$script_path" ]; then
        echo -e "${RED}❌ Script not found: $script_path${NC}"
        SUITE_RESULTS["$suite_name"]="MISSING"
        SUITE_DURATIONS["$suite_name"]="0"
        SUITE_DETAILS["$suite_name"]="Script file not found"
        return 1
    fi
    
    if [ ! -x "$script_path" ]; then
        echo -e "${YELLOW}⚠️  Making script executable: $script_path${NC}"
        chmod +x "$script_path"
    fi
    
    # Run the test suite
    if ./"$script_path" > "$REPORT_DIR/${suite_name,,}-output-$TIMESTAMP.log" 2>&1; then
        exit_code=0
        SUITE_RESULTS["$suite_name"]="PASSED"
        echo -e "${GREEN}✅ $suite_name completed successfully${NC}"
    else
        exit_code=$?
        SUITE_RESULTS["$suite_name"]="FAILED"
        echo -e "${RED}❌ $suite_name failed with exit code: $exit_code${NC}"
    fi
    
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    SUITE_DURATIONS["$suite_name"]="$duration"
    
    # Extract key metrics from log file
    local log_file="$REPORT_DIR/${suite_name,,}-output-$TIMESTAMP.log"
    local details=""
    
    if [ -f "$log_file" ]; then
        # Try to extract test counts and success rates
        local total_tests=$(grep -o "Total Tests: [0-9]*" "$log_file" | tail -1 | grep -o "[0-9]*" || echo "0")
        local passed_tests=$(grep -o "Passed: [0-9]*" "$log_file" | tail -1 | grep -o "[0-9]*" || echo "0")
        local failed_tests=$(grep -o "Failed: [0-9]*" "$log_file" | tail -1 | grep -o "[0-9]*" || echo "0")
        
        if [ "$total_tests" -gt 0 ]; then
            local success_rate=$(echo "scale=1; ($passed_tests * 100) / $total_tests" | bc -l 2>/dev/null || echo "0")
            details="Tests: $total_tests, Passed: $passed_tests, Failed: $failed_tests, Success Rate: ${success_rate}%"
        else
            details="Duration: ${duration}s, Exit Code: $exit_code"
        fi
    else
        details="Duration: ${duration}s, Exit Code: $exit_code"
    fi
    
    SUITE_DETAILS["$suite_name"]="$details"
    
    echo -e "Duration: ${duration}s"
    echo -e "Details: $details"
    echo ""
    
    return $exit_code
}

# Function to check application health
check_application_health() {
    echo -e "${BLUE}🏥 Application Health Check${NC}"
    
    local max_attempts=3
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        echo -e "Attempt $attempt/$max_attempts..."
        
        local response=$(curl -s -w "%{http_code}" --max-time 10 "$BASE_URL/actuator/health" 2>/dev/null || echo "000")
        
        if [ "$response" = "200" ]; then
            echo -e "${GREEN}✅ Application is healthy${NC}"
            return 0
        else
            echo -e "${YELLOW}⚠️  Health check failed (HTTP: $response)${NC}"
            if [ $attempt -lt $max_attempts ]; then
                echo -e "Waiting 10 seconds before retry..."
                sleep 10
            fi
        fi
        
        ((attempt++))
    done
    
    echo -e "${RED}❌ Application health check failed after $max_attempts attempts${NC}"
    return 1
}

# Function to generate master report
generate_master_report() {
    echo -e "${CYAN}📊 Generating Master Test Report...${NC}"
    
    local total_suites=${#SUITE_RESULTS[@]}
    local passed_suites=0
    local failed_suites=0
    local total_duration=0
    
    # Calculate summary statistics
    for suite in "${!SUITE_RESULTS[@]}"; do
        if [ "${SUITE_RESULTS[$suite]}" = "PASSED" ]; then
            ((passed_suites++))
        else
            ((failed_suites++))
        fi
        total_duration=$((total_duration + SUITE_DURATIONS[$suite]))
    done
    
    local success_rate=$(echo "scale=1; ($passed_suites * 100) / $total_suites" | bc -l 2>/dev/null || echo "0")
    
    cat > "$MASTER_REPORT" << EOF
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lab Operations API - Master Test Report</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f8f9fa; color: #333; line-height: 1.6; }
        .container { max-width: 1200px; margin: 0 auto; padding: 20px; }
        .header { background: linear-gradient(135deg, #6c5ce7, #a29bfe); color: white; padding: 40px; border-radius: 15px; text-align: center; margin-bottom: 30px; box-shadow: 0 8px 25px rgba(108, 92, 231, 0.3); }
        .header h1 { font-size: 3em; margin-bottom: 10px; font-weight: 700; }
        .header p { font-size: 1.3em; opacity: 0.9; }
        .summary-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 25px; margin-bottom: 40px; }
        .summary-card { background: white; padding: 30px; border-radius: 15px; text-align: center; box-shadow: 0 5px 15px rgba(0,0,0,0.1); transition: transform 0.3s ease; }
        .summary-card:hover { transform: translateY(-5px); }
        .summary-number { font-size: 3em; font-weight: bold; margin-bottom: 10px; }
        .summary-label { color: #666; font-size: 1em; text-transform: uppercase; letter-spacing: 1px; font-weight: 600; }
        .success { color: #00b894; }
        .danger { color: #e17055; }
        .info { color: #0984e3; }
        .warning { color: #fdcb6e; }
        .section { background: white; margin-bottom: 30px; border-radius: 15px; box-shadow: 0 5px 15px rgba(0,0,0,0.1); overflow: hidden; }
        .section-header { background: #f8f9fa; padding: 25px; border-bottom: 2px solid #e9ecef; }
        .section-header h2 { color: #2d3436; font-size: 1.8em; font-weight: 600; }
        .section-content { padding: 30px; }
        .suite-table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        .suite-table th, .suite-table td { padding: 15px; text-align: left; border-bottom: 1px solid #e9ecef; }
        .suite-table th { background: #f8f9fa; font-weight: 600; color: #2d3436; font-size: 1.1em; }
        .suite-table tr:hover { background: #f8f9fa; }
        .badge { padding: 8px 16px; border-radius: 25px; font-size: 0.9em; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }
        .badge-success { background: #d1f2eb; color: #00b894; }
        .badge-danger { background: #ffeaa7; color: #e17055; }
        .badge-warning { background: #fff3cd; color: #856404; }
        .progress-container { margin: 20px 0; }
        .progress-bar { width: 100%; height: 25px; background: #e9ecef; border-radius: 15px; overflow: hidden; }
        .progress-fill { height: 100%; background: linear-gradient(90deg, #00b894, #00cec9); border-radius: 15px; transition: width 0.5s ease; }
        .progress-text { text-align: center; margin-top: 10px; font-size: 1.2em; font-weight: 600; }
        .footer { text-align: center; padding: 30px; color: #636e72; font-size: 1em; }
        .timestamp { background: #6c5ce7; color: white; padding: 5px 15px; border-radius: 20px; font-family: monospace; }
        @media (max-width: 768px) { 
            .summary-grid { grid-template-columns: 1fr 1fr; } 
            .suite-table { font-size: 0.9em; }
            .header h1 { font-size: 2em; }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🎯 Lab Operations API</h1>
            <p>Master Test Suite Report</p>
            <div style="margin-top: 20px;">
                <span class="timestamp">$TIMESTAMP</span>
            </div>
        </div>

        <div class="summary-grid">
            <div class="summary-card">
                <div class="summary-number info">$total_suites</div>
                <div class="summary-label">Test Suites</div>
            </div>
            <div class="summary-card">
                <div class="summary-number success">$passed_suites</div>
                <div class="summary-label">Passed</div>
            </div>
            <div class="summary-card">
                <div class="summary-number danger">$failed_suites</div>
                <div class="summary-label">Failed</div>
            </div>
            <div class="summary-card">
                <div class="summary-number warning">${total_duration}s</div>
                <div class="summary-label">Total Duration</div>
            </div>
        </div>

        <div class="section">
            <div class="section-header">
                <h2>📊 Overall Success Rate</h2>
            </div>
            <div class="section-content">
                <div class="progress-container">
                    <div class="progress-bar">
                        <div class="progress-fill" style="width: ${success_rate}%"></div>
                    </div>
                    <div class="progress-text">${success_rate}% Success Rate</div>
                </div>
            </div>
        </div>

        <div class="section">
            <div class="section-header">
                <h2>🧪 Test Suite Results</h2>
            </div>
            <div class="section-content">
                <table class="suite-table">
                    <thead>
                        <tr>
                            <th>Test Suite</th>
                            <th>Status</th>
                            <th>Duration</th>
                            <th>Details</th>
                        </tr>
                    </thead>
                    <tbody>
EOF

    # Add test suite results to HTML
    for suite in "${!SUITE_RESULTS[@]}"; do
        local status="${SUITE_RESULTS[$suite]}"
        local duration="${SUITE_DURATIONS[$suite]}"
        local details="${SUITE_DETAILS[$suite]}"
        
        local badge_class="badge-warning"
        if [ "$status" = "PASSED" ]; then
            badge_class="badge-success"
        elif [ "$status" = "FAILED" ]; then
            badge_class="badge-danger"
        fi
        
        cat >> "$MASTER_REPORT" << EOF
                        <tr>
                            <td><strong>$suite</strong></td>
                            <td><span class="badge $badge_class">$status</span></td>
                            <td>${duration}s</td>
                            <td>$details</td>
                        </tr>
EOF
    done

    cat >> "$MASTER_REPORT" << EOF
                    </tbody>
                </table>
            </div>
        </div>

        <div class="section">
            <div class="section-header">
                <h2>📁 Generated Reports</h2>
            </div>
            <div class="section-content">
                <p>Individual test suite reports and logs have been generated in the <code>$REPORT_DIR</code> directory:</p>
                <ul style="margin-top: 15px; padding-left: 20px;">
EOF

    # List all generated files
    for file in "$REPORT_DIR"/*-$TIMESTAMP.*; do
        if [ -f "$file" ]; then
            local filename=$(basename "$file")
            cat >> "$MASTER_REPORT" << EOF
                    <li><code>$filename</code></li>
EOF
        fi
    done

    cat >> "$MASTER_REPORT" << EOF
                </ul>
            </div>
        </div>

        <div class="footer">
            <p>Generated on $(date) | Lab Operations API Master Test Suite</p>
            <p>Total execution time: ${total_duration} seconds</p>
        </div>
    </div>
</body>
</html>
EOF

    echo -e "📄 Master report generated: $MASTER_REPORT"
}

# Main execution function
main() {
    local start_time=$(date +%s)
    
    echo -e "${CYAN}🔍 Pre-flight Checks${NC}"
    
    # Check if application is running
    if ! check_application_health; then
        echo -e "${RED}❌ Cannot proceed without healthy application${NC}"
        exit 1
    fi
    
    echo ""
    echo -e "${CYAN}🚀 Starting Test Suite Execution${NC}"
    echo ""
    
    # Run all test suites
    run_test_suite "Basic API Tests" "api-test-working.sh" "Core functionality and workflow testing"
    run_test_suite "Edge Case Tests" "api-focused-edge-tests.sh" "Comprehensive edge case and validation testing"
    run_test_suite "Robust Tests" "api-robust-test-suite.sh" "Performance, security, and reliability testing"
    run_test_suite "Stress Tests" "api-stress-test.sh" "Load and stress testing under high concurrency"
    
    # Generate master report
    generate_master_report
    
    local end_time=$(date +%s)
    local total_execution_time=$((end_time - start_time))
    
    echo ""
    echo -e "${CYAN}📊 Execution Summary${NC}"
    echo -e "Total Execution Time: ${total_execution_time}s"
    echo -e "Test Suites Run: ${#SUITE_RESULTS[@]}"
    
    local passed_count=0
    local failed_count=0
    
    for suite in "${!SUITE_RESULTS[@]}"; do
        if [ "${SUITE_RESULTS[$suite]}" = "PASSED" ]; then
            ((passed_count++))
        else
            ((failed_count++))
        fi
    done
    
    echo -e "Passed: ${GREEN}$passed_count${NC}"
    echo -e "Failed: ${RED}$failed_count${NC}"
    
    echo ""
    echo -e "📄 Master Report: $MASTER_REPORT"
    echo ""
    
    if [ $failed_count -eq 0 ]; then
        echo -e "${GREEN}🎉 All test suites completed successfully!${NC}"
        exit 0
    else
        echo -e "${RED}⚠️  Some test suites failed. Check individual reports for details.${NC}"
        exit 1
    fi
}

# Execute main function
main "$@"
