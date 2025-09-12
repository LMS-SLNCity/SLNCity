#!/bin/bash

# Lab Operations API Stress Testing Suite
# Advanced performance and reliability testing

set -e

# Configuration
BASE_URL="http://localhost:8080"
REPORT_DIR="test-reports"
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
STRESS_REPORT="$REPORT_DIR/stress-test-report-$TIMESTAMP.html"
METRICS_FILE="$REPORT_DIR/stress-metrics-$TIMESTAMP.csv"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m'

# Test parameters
MAX_CONCURRENT_USERS=20
RAMP_UP_TIME=30
TEST_DURATION=60
REQUESTS_PER_SECOND=10

# Metrics
declare -a RESPONSE_TIMES=()
declare -a ERROR_RATES=()
declare -a THROUGHPUT_DATA=()

mkdir -p "$REPORT_DIR"

echo -e "${CYAN}🔥 Lab Operations API Stress Testing Suite${NC}"
echo -e "${CYAN}==========================================${NC}"
echo "Base URL: $BASE_URL"
echo "Max Concurrent Users: $MAX_CONCURRENT_USERS"
echo "Test Duration: ${TEST_DURATION}s"
echo "Report: $STRESS_REPORT"
echo ""

# Initialize metrics file
echo "timestamp,endpoint,response_time,http_code,bytes_received,concurrent_users" > "$METRICS_FILE"

# Health check before stress testing
check_health() {
    echo -e "${BLUE}🏥 Pre-test Health Check${NC}"
    local response=$(curl -s -w "%{http_code}" --max-time 5 "$BASE_URL/actuator/health" 2>/dev/null || echo "000")
    
    if [ "$response" = "200" ]; then
        echo -e "✅ Application is healthy"
        return 0
    else
        echo -e "${RED}❌ Application health check failed${NC}"
        return 1
    fi
}

# Stress test function for specific endpoint
stress_test_endpoint() {
    local endpoint="$1"
    local method="${2:-GET}"
    local data="${3:-}"
    local test_name="$4"
    
    echo -e "${PURPLE}🔄 Stress Testing: $test_name${NC}"
    echo "Endpoint: $method $endpoint"
    
    local start_time=$(date +%s)
    local end_time=$((start_time + TEST_DURATION))
    local pids=()
    local user_count=0
    
    # Ramp up users gradually
    local ramp_interval=$((RAMP_UP_TIME / MAX_CONCURRENT_USERS))
    
    while [ $(date +%s) -lt $end_time ]; do
        current_time=$(date +%s)
        
        # Add new users during ramp-up period
        if [ $user_count -lt $MAX_CONCURRENT_USERS ] && [ $((current_time - start_time)) -lt $RAMP_UP_TIME ]; then
            if [ $((current_time - start_time)) -ge $((user_count * ramp_interval)) ]; then
                {
                    simulate_user "$endpoint" "$method" "$data" $user_count $end_time
                } &
                pids+=($!)
                ((user_count++))
                echo -e "  👤 User $user_count started"
            fi
        fi
        
        sleep 1
    done
    
    echo -e "  ⏳ Waiting for all users to complete..."
    
    # Wait for all users to complete
    for pid in "${pids[@]}"; do
        wait "$pid" 2>/dev/null || true
    done
    
    # Analyze results
    analyze_stress_results "$test_name"
}

# Simulate individual user behavior
simulate_user() {
    local endpoint="$1"
    local method="$2"
    local data="$3"
    local user_id="$4"
    local end_time="$5"
    
    local user_log="/tmp/stress_user_${user_id}.log"
    local request_count=0
    
    while [ $(date +%s) -lt $end_time ]; do
        local request_start=$(date +%s.%N)
        
        # Prepare curl command
        local curl_cmd="curl -s -w '%{http_code}|%{time_total}|%{size_download}' --max-time 10"
        
        case "$method" in
            "GET")
                curl_cmd="$curl_cmd '$BASE_URL$endpoint'"
                ;;
            "POST"|"PUT"|"PATCH")
                curl_cmd="$curl_cmd -X $method -H 'Content-Type: application/json' -d '$data' '$BASE_URL$endpoint'"
                ;;
        esac
        
        # Execute request
        local response=$(eval "$curl_cmd" 2>/dev/null || echo "000|timeout|0")
        local request_end=$(date +%s.%N)
        
        # Parse response
        local http_code=$(echo "$response" | cut -d'|' -f1)
        local response_time=$(echo "$response" | cut -d'|' -f2)
        local bytes_received=$(echo "$response" | cut -d'|' -f3)
        
        # Log metrics
        echo "$(date +%s),$endpoint,$response_time,$http_code,$bytes_received,$user_id" >> "$METRICS_FILE"
        
        ((request_count++))
        
        # Random think time between requests (0.1-1.0 seconds)
        local think_time=$(echo "scale=2; $(shuf -i 10-100 -n 1) / 100" | bc -l)
        sleep "$think_time"
    done
    
    echo "$request_count" > "/tmp/user_${user_id}_requests.count"
}

# Analyze stress test results
analyze_stress_results() {
    local test_name="$1"
    
    echo -e "${CYAN}📊 Analyzing Results for: $test_name${NC}"
    
    # Calculate metrics from CSV
    local total_requests=$(tail -n +2 "$METRICS_FILE" | wc -l)
    local successful_requests=$(tail -n +2 "$METRICS_FILE" | awk -F',' '$4 >= 200 && $4 < 400' | wc -l)
    local error_requests=$((total_requests - successful_requests))
    
    local avg_response_time=$(tail -n +2 "$METRICS_FILE" | awk -F',' '{sum+=$3; count++} END {if(count>0) printf "%.3f", sum/count; else print "0"}')
    local max_response_time=$(tail -n +2 "$METRICS_FILE" | awk -F',' 'BEGIN{max=0} {if($3>max) max=$3} END {printf "%.3f", max}')
    local min_response_time=$(tail -n +2 "$METRICS_FILE" | awk -F',' 'BEGIN{min=999999} {if($3<min && $3>0) min=$3} END {printf "%.3f", min}')
    
    local error_rate=$(echo "scale=2; ($error_requests * 100) / $total_requests" | bc -l 2>/dev/null || echo "0")
    local throughput=$(echo "scale=2; $total_requests / $TEST_DURATION" | bc -l)
    
    # Calculate percentiles
    local p95_response_time=$(tail -n +2 "$METRICS_FILE" | awk -F',' '{print $3}' | sort -n | awk 'BEGIN{c=0} {a[c++]=$1} END{print a[int(c*0.95)]}')
    local p99_response_time=$(tail -n +2 "$METRICS_FILE" | awk -F',' '{print $3}' | sort -n | awk 'BEGIN{c=0} {a[c++]=$1} END{print a[int(c*0.99)]}')
    
    echo -e "  📈 Performance Metrics:"
    echo -e "    Total Requests: $total_requests"
    echo -e "    Successful: $successful_requests"
    echo -e "    Errors: $error_requests"
    echo -e "    Error Rate: ${error_rate}%"
    echo -e "    Throughput: ${throughput} req/sec"
    echo -e "    Avg Response Time: ${avg_response_time}s"
    echo -e "    Min Response Time: ${min_response_time}s"
    echo -e "    Max Response Time: ${max_response_time}s"
    echo -e "    95th Percentile: ${p95_response_time}s"
    echo -e "    99th Percentile: ${p99_response_time}s"
    
    # Performance assessment
    if (( $(echo "$error_rate > 5.0" | bc -l) )); then
        echo -e "  ${RED}❌ High error rate detected!${NC}"
    elif (( $(echo "$avg_response_time > 2.0" | bc -l) )); then
        echo -e "  ${YELLOW}⚠️  Slow average response time${NC}"
    else
        echo -e "  ${GREEN}✅ Performance within acceptable limits${NC}"
    fi
    
    # Store results for report
    RESPONSE_TIMES+=("$avg_response_time")
    ERROR_RATES+=("$error_rate")
    THROUGHPUT_DATA+=("$throughput")
}

# Memory and CPU monitoring
monitor_system_resources() {
    echo -e "${BLUE}🖥️  System Resource Monitoring${NC}"
    
    local monitor_duration=60
    local monitor_interval=5
    local iterations=$((monitor_duration / monitor_interval))
    
    echo "timestamp,cpu_usage,memory_usage,disk_usage" > "$REPORT_DIR/system-metrics-$TIMESTAMP.csv"
    
    for ((i=1; i<=iterations; i++)); do
        local cpu_usage=$(top -l 1 | grep "CPU usage" | awk '{print $3}' | sed 's/%//' 2>/dev/null || echo "0")
        local memory_usage=$(vm_stat | grep "Pages active" | awk '{print $3}' | sed 's/\.//' 2>/dev/null || echo "0")
        local disk_usage=$(df -h . | tail -1 | awk '{print $5}' | sed 's/%//' 2>/dev/null || echo "0")
        
        echo "$(date +%s),$cpu_usage,$memory_usage,$disk_usage" >> "$REPORT_DIR/system-metrics-$TIMESTAMP.csv"
        
        if [ $((i % 6)) -eq 0 ]; then
            echo -e "  📊 CPU: ${cpu_usage}%, Memory: ${memory_usage}, Disk: ${disk_usage}%"
        fi
        
        sleep $monitor_interval
    done
}

# Generate comprehensive stress test report
generate_stress_report() {
    echo -e "${CYAN}📄 Generating Stress Test Report...${NC}"
    
    local total_requests=$(tail -n +2 "$METRICS_FILE" | wc -l)
    local avg_throughput=$(echo "${THROUGHPUT_DATA[@]}" | awk '{for(i=1;i<=NF;i++) sum+=$i; print sum/NF}')
    local avg_error_rate=$(echo "${ERROR_RATES[@]}" | awk '{for(i=1;i<=NF;i++) sum+=$i; print sum/NF}')
    
    cat > "$STRESS_REPORT" << EOF
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lab Operations API - Stress Test Report</title>
    <style>
        body { font-family: 'Segoe UI', sans-serif; margin: 0; padding: 20px; background: #f5f7fa; }
        .container { max-width: 1200px; margin: 0 auto; }
        .header { background: linear-gradient(135deg, #ff6b6b, #ee5a24); color: white; padding: 30px; border-radius: 10px; text-align: center; margin-bottom: 30px; }
        .metrics-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin-bottom: 30px; }
        .metric-card { background: white; padding: 25px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); text-align: center; }
        .metric-value { font-size: 2.5em; font-weight: bold; margin-bottom: 10px; }
        .metric-label { color: #666; font-size: 0.9em; text-transform: uppercase; }
        .section { background: white; margin-bottom: 30px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); padding: 30px; }
        .success { color: #27ae60; }
        .warning { color: #f39c12; }
        .danger { color: #e74c3c; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🔥 Lab Operations API Stress Test</h1>
            <p>Performance and Reliability Analysis - $TIMESTAMP</p>
        </div>
        
        <div class="metrics-grid">
            <div class="metric-card">
                <div class="metric-value success">$total_requests</div>
                <div class="metric-label">Total Requests</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">$MAX_CONCURRENT_USERS</div>
                <div class="metric-label">Max Concurrent Users</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">${TEST_DURATION}s</div>
                <div class="metric-label">Test Duration</div>
            </div>
            <div class="metric-card">
                <div class="metric-value">${avg_throughput}</div>
                <div class="metric-label">Avg Throughput (req/sec)</div>
            </div>
        </div>
        
        <div class="section">
            <h2>📊 Test Summary</h2>
            <p>The stress test simulated $MAX_CONCURRENT_USERS concurrent users over $TEST_DURATION seconds, generating $total_requests total requests.</p>
            <p><strong>Average Error Rate:</strong> ${avg_error_rate}%</p>
            <p><strong>Test Configuration:</strong> Ramp-up time: ${RAMP_UP_TIME}s, Target RPS: $REQUESTS_PER_SECOND</p>
        </div>
        
        <div class="section">
            <h2>📈 Performance Analysis</h2>
            <p>Detailed metrics have been captured in: <code>$METRICS_FILE</code></p>
            <p>System resource monitoring data: <code>$REPORT_DIR/system-metrics-$TIMESTAMP.csv</code></p>
        </div>
    </div>
</body>
</html>
EOF
}

# Main execution
main() {
    if ! check_health; then
        echo -e "${RED}❌ Application health check failed. Exiting.${NC}"
        exit 1
    fi
    
    echo ""
    echo -e "${CYAN}🚀 Starting Stress Test Suite${NC}"
    echo ""
    
    # Start system monitoring in background
    monitor_system_resources &
    local monitor_pid=$!
    
    # Run stress tests on key endpoints
    stress_test_endpoint "/actuator/health" "GET" "" "Health Check Endpoint"
    stress_test_endpoint "/test-templates" "GET" "" "List Templates Endpoint"
    stress_test_endpoint "/visits" "GET" "" "List Visits Endpoint"
    
    # Stop system monitoring
    kill $monitor_pid 2>/dev/null || true
    
    # Generate report
    generate_stress_report
    
    echo ""
    echo -e "${GREEN}🎉 Stress testing completed!${NC}"
    echo -e "📄 Report: $STRESS_REPORT"
    echo -e "📊 Metrics: $METRICS_FILE"
    
    # Cleanup temporary files
    rm -f /tmp/stress_user_*.log /tmp/user_*_requests.count
}

# Run main function
main "$@"
