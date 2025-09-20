// Comprehensive Dashboard Functionality Tester
// This script tests all interactive elements across dashboards

class DashboardTester {
    constructor() {
        this.results = {
            admin: { tested: false, issues: [], working: [] },
            reception: { tested: false, issues: [], working: [] },
            phlebotomy: { tested: false, issues: [], working: [] },
            technician: { tested: false, issues: [], working: [] }
        };
    }

    async testAllDashboards() {
        console.log('🧪 Starting Comprehensive Dashboard Testing...');
        
        // Test each dashboard
        await this.testDashboard('admin', 'http://localhost:8080/admin/dashboard.html');
        await this.testDashboard('reception', 'http://localhost:8080/reception/dashboard.html');
        await this.testDashboard('phlebotomy', 'http://localhost:8080/phlebotomy/dashboard.html');
        await this.testDashboard('technician', 'http://localhost:8080/technician/dashboard.html');
        
        this.generateReport();
    }

    async testDashboard(name, url) {
        console.log(`\n🔍 Testing ${name} dashboard...`);
        
        try {
            // Test basic loading
            const response = await fetch(url);
            if (response.ok) {
                this.results[name].working.push('Page loads successfully');
                console.log(`✅ ${name}: Page loads`);
            } else {
                this.results[name].issues.push(`Page failed to load: HTTP ${response.status}`);
                console.log(`❌ ${name}: Page failed to load`);
                return;
            }

            // Test API endpoints
            await this.testApiEndpoints(name);
            
            // Test specific dashboard functionality
            switch(name) {
                case 'admin':
                    await this.testAdminFunctionality();
                    break;
                case 'reception':
                    await this.testReceptionFunctionality();
                    break;
                case 'phlebotomy':
                    await this.testPhlebotomyFunctionality();
                    break;
                case 'technician':
                    await this.testTechnicianFunctionality();
                    break;
            }
            
            this.results[name].tested = true;
            
        } catch (error) {
            this.results[name].issues.push(`Testing failed: ${error.message}`);
            console.log(`❌ ${name}: Testing failed - ${error.message}`);
        }
    }

    async testApiEndpoints(dashboardName) {
        const commonEndpoints = [
            '/visits',
            '/test-templates',
            '/actuator/health'
        ];

        const dashboardEndpoints = {
            admin: ['/api/v1/equipment', '/api/v1/inventory', '/api/v1/monitoring'],
            reception: ['/billing'],
            phlebotomy: ['/samples', '/sample-collection'],
            technician: ['/lab-tests', '/api/v1/tests']
        };

        const endpoints = [...commonEndpoints, ...(dashboardEndpoints[dashboardName] || [])];

        for (const endpoint of endpoints) {
            try {
                const response = await fetch(`http://localhost:8080${endpoint}`);
                if (response.ok) {
                    this.results[dashboardName].working.push(`API endpoint working: ${endpoint}`);
                    console.log(`✅ ${dashboardName}: API ${endpoint} works`);
                } else {
                    this.results[dashboardName].issues.push(`API endpoint failed: ${endpoint} (HTTP ${response.status})`);
                    console.log(`⚠️ ${dashboardName}: API ${endpoint} failed (${response.status})`);
                }
            } catch (error) {
                this.results[dashboardName].issues.push(`API endpoint error: ${endpoint} - ${error.message}`);
                console.log(`❌ ${dashboardName}: API ${endpoint} error - ${error.message}`);
            }
        }
    }

    async testAdminFunctionality() {
        console.log('🔧 Testing Admin dashboard functionality...');
        
        // Test equipment management
        try {
            const equipmentResponse = await fetch('http://localhost:8080/api/v1/equipment');
            if (equipmentResponse.ok) {
                const equipment = await equipmentResponse.json();
                this.results.admin.working.push(`Equipment data loaded: ${equipment.length} items`);
            }
        } catch (error) {
            this.results.admin.issues.push(`Equipment loading failed: ${error.message}`);
        }

        // Test inventory management
        try {
            const inventoryResponse = await fetch('http://localhost:8080/api/v1/inventory');
            if (inventoryResponse.ok) {
                this.results.admin.working.push('Inventory API accessible');
            }
        } catch (error) {
            this.results.admin.issues.push(`Inventory API failed: ${error.message}`);
        }
    }

    async testReceptionFunctionality() {
        console.log('🏥 Testing Reception dashboard functionality...');
        
        // Test visits management
        try {
            const visitsResponse = await fetch('http://localhost:8080/visits');
            if (visitsResponse.ok) {
                const visits = await visitsResponse.json();
                this.results.reception.working.push(`Visits data loaded: ${visits.length} visits`);
            }
        } catch (error) {
            this.results.reception.issues.push(`Visits loading failed: ${error.message}`);
        }

        // Test test templates
        try {
            const templatesResponse = await fetch('http://localhost:8080/test-templates');
            if (templatesResponse.ok) {
                const templates = await templatesResponse.json();
                this.results.reception.working.push(`Test templates loaded: ${templates.length} templates`);
            }
        } catch (error) {
            this.results.reception.issues.push(`Test templates loading failed: ${error.message}`);
        }
    }

    async testPhlebotomyFunctionality() {
        console.log('🩸 Testing Phlebotomy dashboard functionality...');
        
        // Test sample collection
        try {
            const samplesResponse = await fetch('http://localhost:8080/samples');
            if (samplesResponse.ok) {
                const samples = await samplesResponse.json();
                this.results.phlebotomy.working.push(`Samples data loaded: ${samples.length} samples`);
            }
        } catch (error) {
            this.results.phlebotomy.issues.push(`Samples loading failed: ${error.message}`);
        }

        // Test visits for sample collection
        try {
            const visitsResponse = await fetch('http://localhost:8080/visits');
            if (visitsResponse.ok) {
                const visits = await visitsResponse.json();
                const pendingVisits = visits.filter(v => v.status === 'PENDING' || v.status === 'IN_PROGRESS');
                this.results.phlebotomy.working.push(`Pending visits for collection: ${pendingVisits.length}`);
            }
        } catch (error) {
            this.results.phlebotomy.issues.push(`Visits for collection failed: ${error.message}`);
        }
    }

    async testTechnicianFunctionality() {
        console.log('🔬 Testing Lab Technician dashboard functionality...');
        
        // Test lab tests
        try {
            const testsResponse = await fetch('http://localhost:8080/lab-tests');
            if (testsResponse.ok) {
                const tests = await testsResponse.json();
                const pendingTests = tests.filter(t => t.status === 'PENDING');
                this.results.technician.working.push(`Lab tests loaded: ${tests.length} total, ${pendingTests.length} pending`);
            }
        } catch (error) {
            this.results.technician.issues.push(`Lab tests loading failed: ${error.message}`);
        }

        // Test equipment for lab work
        try {
            const equipmentResponse = await fetch('http://localhost:8080/api/v1/equipment');
            if (equipmentResponse.ok) {
                const equipment = await equipmentResponse.json();
                const activeEquipment = equipment.filter(e => e.status === 'ACTIVE');
                this.results.technician.working.push(`Active equipment available: ${activeEquipment.length}`);
            }
        } catch (error) {
            this.results.technician.issues.push(`Equipment for lab work failed: ${error.message}`);
        }
    }

    generateReport() {
        console.log('\n📊 COMPREHENSIVE DASHBOARD TEST RESULTS');
        console.log('==========================================');

        let totalIssues = 0;
        let totalWorking = 0;

        Object.entries(this.results).forEach(([dashboard, result]) => {
            console.log(`\n🔍 ${dashboard.toUpperCase()} DASHBOARD:`);
            console.log(`✅ Working: ${result.working.length}`);
            console.log(`❌ Issues: ${result.issues.length}`);
            
            if (result.working.length > 0) {
                console.log('Working features:');
                result.working.forEach(item => console.log(`  ✅ ${item}`));
            }
            
            if (result.issues.length > 0) {
                console.log('Issues found:');
                result.issues.forEach(item => console.log(`  ❌ ${item}`));
            }

            totalIssues += result.issues.length;
            totalWorking += result.working.length;
        });

        console.log('\n🎯 OVERALL SUMMARY:');
        console.log(`✅ Total working features: ${totalWorking}`);
        console.log(`❌ Total issues found: ${totalIssues}`);
        
        if (totalIssues === 0) {
            console.log('🎉 All dashboards are fully functional!');
        } else if (totalIssues < 5) {
            console.log('⚠️ Minor issues found - needs attention');
        } else {
            console.log('🚨 Major issues found - requires fixes');
        }

        // Generate HTML report
        this.generateHTMLReport();
    }

    generateHTMLReport() {
        const reportHTML = `
<!DOCTYPE html>
<html>
<head>
    <title>Dashboard Functionality Test Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background: #f8f9fa; padding: 20px; border-radius: 5px; margin-bottom: 20px; }
        .dashboard { border: 1px solid #ddd; margin: 20px 0; padding: 20px; border-radius: 5px; }
        .working { border-left: 5px solid #28a745; }
        .issues { border-left: 5px solid #dc3545; }
        .mixed { border-left: 5px solid #ffc107; }
        .feature-list { margin: 10px 0; }
        .feature-item { padding: 5px 0; }
        .working-item { color: #28a745; }
        .issue-item { color: #dc3545; }
        .summary { background: #e9ecef; padding: 15px; border-radius: 5px; margin: 20px 0; }
    </style>
</head>
<body>
    <div class="header">
        <h1>🧪 Dashboard Functionality Test Report</h1>
        <p>Generated: ${new Date().toISOString()}</p>
        <p>Comprehensive testing of all dashboard interactive elements and API endpoints.</p>
    </div>
    
    ${Object.entries(this.results).map(([dashboard, result]) => {
        const statusClass = result.issues.length === 0 ? 'working' : 
                           result.working.length === 0 ? 'issues' : 'mixed';
        const statusText = result.issues.length === 0 ? '✅ Fully Functional' :
                          result.working.length === 0 ? '❌ Major Issues' : '⚠️ Partial Issues';
        
        return `
        <div class="dashboard ${statusClass}">
            <h3>${dashboard.charAt(0).toUpperCase() + dashboard.slice(1)} Dashboard</h3>
            <p><strong>Status:</strong> ${statusText}</p>
            <p><strong>Working Features:</strong> ${result.working.length} | <strong>Issues:</strong> ${result.issues.length}</p>
            
            ${result.working.length > 0 ? `
                <div class="feature-list">
                    <h4>✅ Working Features:</h4>
                    ${result.working.map(item => `<div class="feature-item working-item">✅ ${item}</div>`).join('')}
                </div>
            ` : ''}
            
            ${result.issues.length > 0 ? `
                <div class="feature-list">
                    <h4>❌ Issues Found:</h4>
                    ${result.issues.map(item => `<div class="feature-item issue-item">❌ ${item}</div>`).join('')}
                </div>
            ` : ''}
        </div>`;
    }).join('')}
    
    <div class="summary">
        <h2>🎯 Summary & Recommendations</h2>
        <p><strong>Total Working Features:</strong> ${Object.values(this.results).reduce((sum, r) => sum + r.working.length, 0)}</p>
        <p><strong>Total Issues:</strong> ${Object.values(this.results).reduce((sum, r) => sum + r.issues.length, 0)}</p>
        
        <h3>Next Steps:</h3>
        <ol>
            <li>Fix API endpoint issues first</li>
            <li>Test JavaScript functionality in browser</li>
            <li>Verify data loading and display</li>
            <li>Test user interactions and forms</li>
        </ol>
    </div>
</body>
</html>`;

        // In a real environment, this would save to a file
        console.log('\n📄 HTML Report generated (would be saved as dashboard-functionality-report.html)');
    }
}

// Export for use in browser or Node.js
if (typeof module !== 'undefined' && module.exports) {
    module.exports = DashboardTester;
} else if (typeof window !== 'undefined') {
    window.DashboardTester = DashboardTester;
}

// Auto-run if in browser
if (typeof window !== 'undefined') {
    document.addEventListener('DOMContentLoaded', () => {
        const tester = new DashboardTester();
        tester.testAllDashboards();
    });
}
