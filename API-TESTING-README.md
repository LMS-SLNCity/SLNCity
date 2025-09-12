# 🧪 Lab Operations API Testing Suite

A comprehensive shell script to test all API endpoints of the Lab Operations Management System and generate detailed reports.

## 📋 Features

- **Complete API Coverage**: Tests all 25+ API endpoints
- **Comprehensive Workflow Testing**: End-to-end business process validation
- **Multiple Report Formats**: HTML and JSON reports
- **Real-time Logging**: Detailed execution logs
- **Error Handling**: Tests both success and failure scenarios
- **Visual Reports**: Beautiful HTML reports with charts and statistics
- **CI/CD Ready**: Exit codes for automated testing

## 🚀 Quick Start

### Prerequisites

1. **Application Running**: Ensure the Lab Operations application is running
   ```bash
   mvn spring-boot:run
   ```

2. **curl installed**: The script uses curl for API calls
   ```bash
   # Check if curl is available
   curl --version
   ```

### Running the Tests

```bash
# Make the script executable (first time only)
chmod +x test-all-apis.sh

# Run all API tests
./test-all-apis.sh
```

## 📊 What Gets Tested

### 🏥 **Health & Application**
- Application health check
- Service availability

### 📋 **Test Template Management**
- ✅ Create test template
- ✅ Get test template by ID
- ✅ Get all test templates
- ✅ Search test templates by name
- ✅ Update test template
- ❌ Error handling for invalid data

### 🏥 **Visit Management**
- ✅ Create patient visit
- ✅ Get visit by ID
- ✅ Get all visits
- ✅ Filter visits by status
- ✅ Search visits by phone number
- ✅ Update visit status
- ❌ Error handling for non-existent visits

### 🧪 **Lab Test Workflow**
- ✅ Add test to visit
- ✅ Get tests for visit
- ✅ Update test results
- ✅ Approve test results
- ❌ Error handling for invalid operations

### 💰 **Billing System**
- ✅ Generate bill for visit
- ✅ Get bill by ID
- ✅ Get all bills
- ✅ Get unpaid bills
- ✅ Mark bill as paid
- ✅ Get revenue for period
- ❌ Error handling for billing operations

### ⚠️ **Error Handling**
- 404 errors for non-existent resources
- 400 errors for invalid data
- Validation error responses

## 📈 Generated Reports

### HTML Report (`api-test-report-YYYYMMDD-HHMMSS.html`)
- **Visual Dashboard**: Summary cards with test statistics
- **Detailed Results Table**: All test results with status, method, endpoint
- **Color-coded Status**: Green for pass, red for fail
- **Responsive Design**: Works on desktop and mobile
- **Professional Styling**: Ready for sharing with stakeholders

### JSON Report (`api-test-report-YYYYMMDD-HHMMSS.json`)
- **Machine Readable**: Perfect for CI/CD integration
- **Complete Data**: All test results and metadata
- **API Integration**: Easy to parse and process
- **Structured Format**: Consistent schema for automation

### Log File (`/tmp/lab-api-test/api-test.log`)
- **Detailed Execution Log**: Step-by-step test execution
- **Timestamps**: Precise timing information
- **Debug Information**: Request/response details
- **Error Details**: Full error messages and stack traces

## 🔧 Configuration

### Base URL
```bash
# Default configuration
BASE_URL="http://localhost:8080"

# To test against different environment
BASE_URL="https://your-api-server.com" ./test-all-apis.sh
```

### Custom Reports Location
```bash
# Reports are saved in current directory by default
# Logs are saved in /tmp/lab-api-test/
```

## 📋 Sample Output

```bash
🚀 Lab Operations API Test Suite
==================================

✅ Checking if application is running...
✅ Application is running!

📋 Testing Test Template APIs
✅ PASSED - Create Test Template - Status: 201
✅ PASSED - Get Test Template by ID - Status: 200
✅ PASSED - Get All Test Templates - Status: 200
...

💰 Testing Billing APIs
✅ PASSED - Generate Bill - Status: 200
✅ PASSED - Mark Bill as Paid - Status: 200
...

📊 TEST SUMMARY
=================
Total Tests: 25
✅ Passed: 25
❌ Failed: 0
Success Rate: 100%

🎉 All tests passed successfully!
📊 HTML Report: api-test-report-20231212-143022.html
📄 JSON Report: api-test-report-20231212-143022.json
```

## 🔍 Troubleshooting

### Application Not Running
```bash
Error: Application is not running at http://localhost:8080
Please start the application first: mvn spring-boot:run
```
**Solution**: Start the Spring Boot application before running tests.

### curl Not Found
```bash
Error: curl is required but not installed.
```
**Solution**: Install curl using your package manager.

### Permission Denied
```bash
Permission denied: ./test-all-apis.sh
```
**Solution**: Make the script executable with `chmod +x test-all-apis.sh`

## 🚀 CI/CD Integration

The script returns appropriate exit codes:
- **0**: All tests passed
- **1**: Some tests failed or application not running

### GitHub Actions Example
```yaml
- name: Run API Tests
  run: ./test-all-apis.sh
  
- name: Upload Test Reports
  uses: actions/upload-artifact@v3
  with:
    name: api-test-reports
    path: api-test-report-*.html
```

### Jenkins Pipeline Example
```groovy
stage('API Tests') {
    steps {
        sh './test-all-apis.sh'
        publishHTML([
            allowMissing: false,
            alwaysLinkToLastBuild: true,
            keepAll: true,
            reportDir: '.',
            reportFiles: 'api-test-report-*.html',
            reportName: 'API Test Report'
        ])
    }
}
```

## 📝 Customization

### Adding New Tests
1. Add test calls in the `run_tests()` function
2. Use the `test_api()` helper function
3. Extract IDs using `extract_id()` for chaining tests

### Custom Validation
Modify the `test_api()` function to add custom response validation beyond status codes.

---

**🎯 Ready to ensure your Lab Operations API is working perfectly!**
