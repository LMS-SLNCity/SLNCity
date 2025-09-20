#!/usr/bin/env node

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

class UITestRunner {
    constructor() {
        this.results = {
            playwright: null,
            selenium: null,
            comparison: null
        };
    }

    async checkDependencies() {
        console.log('🔍 Checking dependencies...');
        
        const requiredPackages = [
            'playwright',
            'selenium-webdriver'
        ];

        const missingPackages = [];
        
        for (const pkg of requiredPackages) {
            try {
                require.resolve(pkg);
                console.log(`✅ ${pkg} is installed`);
            } catch (error) {
                missingPackages.push(pkg);
                console.log(`❌ ${pkg} is missing`);
            }
        }

        if (missingPackages.length > 0) {
            console.log('\n📦 Installing missing packages...');
            try {
                execSync(`npm install ${missingPackages.join(' ')}`, { stdio: 'inherit' });
                console.log('✅ Dependencies installed successfully');
            } catch (error) {
                console.error('❌ Failed to install dependencies:', error.message);
                throw error;
            }
        }
    }

    async checkServerStatus() {
        console.log('🌐 Checking server status...');
        
        const http = require('http');
        
        return new Promise((resolve, reject) => {
            const req = http.get('http://localhost:8080', (res) => {
                console.log(`✅ Server is running (HTTP ${res.statusCode})`);
                resolve(true);
            });
            
            req.on('error', (error) => {
                console.log('❌ Server is not running');
                console.log('💡 Please start the server with: mvn spring-boot:run');
                reject(new Error('Server not running'));
            });
            
            req.setTimeout(5000, () => {
                req.destroy();
                reject(new Error('Server connection timeout'));
            });
        });
    }

    async runPlaywrightTests() {
        console.log('\n🎭 Running Playwright Tests...');
        
        try {
            const UITestingSuite = require('./ui-testing-suite.js');
            const suite = new UITestingSuite();
            await suite.runAllTests();
            
            // Load results
            if (fs.existsSync('ui-test-report.json')) {
                this.results.playwright = JSON.parse(fs.readFileSync('ui-test-report.json', 'utf8'));
                console.log('✅ Playwright tests completed');
            }
        } catch (error) {
            console.error('❌ Playwright tests failed:', error.message);
            this.results.playwright = { error: error.message };
        }
    }

    async runSeleniumTests() {
        console.log('\n🔧 Running Selenium Tests...');
        
        try {
            const SeleniumUITest = require('./selenium-ui-test.js');
            const suite = new SeleniumUITest();
            await suite.runAllTests();
            
            // Load results
            if (fs.existsSync('selenium-test-report.json')) {
                this.results.selenium = JSON.parse(fs.readFileSync('selenium-test-report.json', 'utf8'));
                console.log('✅ Selenium tests completed');
            }
        } catch (error) {
            console.error('❌ Selenium tests failed:', error.message);
            this.results.selenium = { error: error.message };
        }
    }

    generateComparisonReport() {
        console.log('\n📊 Generating Comparison Report...');
        
        const comparison = {
            timestamp: new Date().toISOString(),
            dashboards: {}
        };

        // Compare results for each dashboard
        const dashboardNames = ['Admin', 'Reception', 'Phlebotomy', 'Lab-Technician'];
        
        dashboardNames.forEach(name => {
            const playwrightResult = this.results.playwright?.results?.find(r => r.name === name);
            const seleniumResult = this.results.selenium?.results?.find(r => r.name === name);
            
            comparison.dashboards[name] = {
                playwright: playwrightResult || { status: 'not_tested' },
                selenium: seleniumResult || { status: 'not_tested' },
                issues: this.compareIssues(playwrightResult, seleniumResult),
                recommendation: this.generateRecommendation(playwrightResult, seleniumResult)
            };
        });

        this.results.comparison = comparison;
        
        // Save comparison report
        fs.writeFileSync('ui-comparison-report.json', JSON.stringify(comparison, null, 2));
        
        // Generate HTML comparison report
        const htmlReport = this.generateComparisonHTML(comparison);
        fs.writeFileSync('ui-comparison-report.html', htmlReport);
        
        console.log('📄 Comparison reports generated:');
        console.log('  - ui-comparison-report.json');
        console.log('  - ui-comparison-report.html');
    }

    compareIssues(playwrightResult, seleniumResult) {
        const issues = {
            common: [],
            playwrightOnly: [],
            seleniumOnly: [],
            severity: 'low'
        };

        if (!playwrightResult || !seleniumResult) {
            return issues;
        }

        const pIssues = playwrightResult.issues || [];
        const sIssues = seleniumResult.issues || [];

        // Find common issues
        pIssues.forEach(pIssue => {
            if (sIssues.some(sIssue => this.issuesSimilar(pIssue, sIssue))) {
                issues.common.push(pIssue);
            } else {
                issues.playwrightOnly.push(pIssue);
            }
        });

        // Find Selenium-only issues
        sIssues.forEach(sIssue => {
            if (!pIssues.some(pIssue => this.issuesSimilar(pIssue, sIssue))) {
                issues.seleniumOnly.push(sIssue);
            }
        });

        // Determine severity
        const totalIssues = issues.common.length + issues.playwrightOnly.length + issues.seleniumOnly.length;
        if (totalIssues > 5) {
            issues.severity = 'high';
        } else if (totalIssues > 2) {
            issues.severity = 'medium';
        }

        return issues;
    }

    issuesSimilar(issue1, issue2) {
        // Simple similarity check - could be improved
        const normalize = str => str.toLowerCase().replace(/[^a-z0-9]/g, '');
        return normalize(issue1).includes(normalize(issue2)) || 
               normalize(issue2).includes(normalize(issue1));
    }

    generateRecommendation(playwrightResult, seleniumResult) {
        if (!playwrightResult && !seleniumResult) {
            return 'Both tests failed - server or configuration issue';
        }

        if (!playwrightResult) {
            return 'Playwright test failed - check Playwright configuration';
        }

        if (!seleniumResult) {
            return 'Selenium test failed - check WebDriver setup';
        }

        const pIssues = playwrightResult.issues?.length || 0;
        const sIssues = seleniumResult.issues?.length || 0;
        const totalIssues = pIssues + sIssues;

        if (totalIssues === 0) {
            return '✅ Dashboard appears to be working correctly';
        } else if (totalIssues < 3) {
            return '⚠️ Minor issues found - needs attention';
        } else if (totalIssues < 6) {
            return '🔧 Moderate issues found - requires fixes';
        } else {
            return '🚨 Major issues found - needs complete revamp';
        }
    }

    generateComparisonHTML(comparison) {
        return `
<!DOCTYPE html>
<html>
<head>
    <title>UI Testing Comparison Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background: #f8f9fa; padding: 20px; border-radius: 5px; margin-bottom: 20px; }
        .dashboard { border: 1px solid #ddd; margin: 20px 0; padding: 20px; border-radius: 5px; }
        .severity-high { border-left: 5px solid #dc3545; }
        .severity-medium { border-left: 5px solid #ffc107; }
        .severity-low { border-left: 5px solid #28a745; }
        .test-result { display: flex; gap: 20px; margin: 15px 0; }
        .test-column { flex: 1; padding: 15px; background: #f8f9fa; border-radius: 3px; }
        .issues { background: #fff3cd; padding: 10px; margin: 10px 0; border-radius: 3px; }
        .recommendation { background: #d1ecf1; padding: 15px; margin: 15px 0; border-radius: 5px; }
        .common-issues { background: #f8d7da; padding: 10px; margin: 10px 0; border-radius: 3px; }
        ul { margin: 5px 0; padding-left: 20px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>🧪 UI Testing Comparison Report</h1>
        <p>Generated: ${comparison.timestamp}</p>
        <p>This report compares Playwright and Selenium test results to identify UI issues across all dashboards.</p>
    </div>
    
    ${Object.entries(comparison.dashboards).map(([name, data]) => `
        <div class="dashboard severity-${data.issues.severity}">
            <h2>${name} Dashboard</h2>
            
            <div class="recommendation">
                <h3>🎯 Recommendation</h3>
                <p>${data.recommendation}</p>
            </div>
            
            ${data.issues.common.length > 0 ? `
                <div class="common-issues">
                    <h3>🔴 Critical Issues (Found by Both Tools)</h3>
                    <ul>
                        ${data.issues.common.map(issue => `<li>${issue}</li>`).join('')}
                    </ul>
                </div>
            ` : ''}
            
            <div class="test-result">
                <div class="test-column">
                    <h3>🎭 Playwright Results</h3>
                    <p><strong>Status:</strong> ${data.playwright.status || 'not_tested'}</p>
                    <p><strong>Load Time:</strong> ${data.playwright.loadTime || 'N/A'}ms</p>
                    ${data.playwright.issues && data.playwright.issues.length > 0 ? `
                        <div class="issues">
                            <h4>Issues:</h4>
                            <ul>
                                ${data.playwright.issues.map(issue => `<li>${issue}</li>`).join('')}
                            </ul>
                        </div>
                    ` : '<p>✅ No issues found</p>'}
                </div>
                
                <div class="test-column">
                    <h3>🔧 Selenium Results</h3>
                    <p><strong>Status:</strong> ${data.selenium.status || 'not_tested'}</p>
                    <p><strong>Load Time:</strong> ${data.selenium.loadTime || 'N/A'}ms</p>
                    ${data.selenium.issues && data.selenium.issues.length > 0 ? `
                        <div class="issues">
                            <h4>Issues:</h4>
                            <ul>
                                ${data.selenium.issues.map(issue => `<li>${issue}</li>`).join('')}
                            </ul>
                        </div>
                    ` : '<p>✅ No issues found</p>'}
                </div>
            </div>
        </div>
    `).join('')}
    
    <div class="header">
        <h2>📸 Screenshots</h2>
        <p>Check the following directories for visual evidence:</p>
        <ul>
            <li><code>ui-test-screenshots/</code> - Playwright screenshots</li>
            <li><code>selenium-screenshots/</code> - Selenium screenshots</li>
        </ul>
    </div>
</body>
</html>`;
    }

    async run() {
        try {
            console.log('🚀 Starting Comprehensive UI Testing Suite...\n');
            
            // Check dependencies
            await this.checkDependencies();
            
            // Check server status
            await this.checkServerStatus();
            
            // Run both test suites
            await this.runPlaywrightTests();
            await this.runSeleniumTests();
            
            // Generate comparison report
            this.generateComparisonReport();
            
            console.log('\n🎉 UI Testing Complete!');
            console.log('\n📄 Generated Reports:');
            console.log('  - ui-test-report.html (Playwright)');
            console.log('  - selenium-test-report.json (Selenium)');
            console.log('  - ui-comparison-report.html (Comparison)');
            console.log('\n📸 Screenshots:');
            console.log('  - ui-test-screenshots/ (Playwright)');
            console.log('  - selenium-screenshots/ (Selenium)');
            
            // Print summary
            this.printSummary();
            
        } catch (error) {
            console.error('\n❌ UI Testing failed:', error.message);
            process.exit(1);
        }
    }

    printSummary() {
        console.log('\n📈 TESTING SUMMARY:');
        
        if (this.results.comparison) {
            Object.entries(this.results.comparison.dashboards).forEach(([name, data]) => {
                const totalIssues = (data.issues.common?.length || 0) + 
                                  (data.issues.playwrightOnly?.length || 0) + 
                                  (data.issues.seleniumOnly?.length || 0);
                
                const status = totalIssues === 0 ? '✅' : 
                              totalIssues < 3 ? '⚠️' : 
                              totalIssues < 6 ? '🔧' : '🚨';
                
                console.log(`${status} ${name}: ${totalIssues} issues found`);
            });
        }
        
        console.log('\n💡 Next Steps:');
        console.log('1. Open ui-comparison-report.html in your browser');
        console.log('2. Review screenshots for visual issues');
        console.log('3. Fix identified problems starting with critical issues');
        console.log('4. Re-run tests to verify fixes');
    }
}

// Run the comprehensive test suite
const runner = new UITestRunner();
runner.run();
