const { chromium } = require('playwright');

async function runPhlebotomyTests() {
    console.log('🩸 REAL PHLEBOTOMY DASHBOARD TESTING');
    console.log('===================================');
    
    const browser = await chromium.launch({ headless: false });
    const context = await browser.newContext();
    const page = await context.newPage();
    
    // Listen for console messages and errors
    page.on('console', msg => {
        console.log(`BROWSER CONSOLE [${msg.type()}]: ${msg.text()}`);
    });
    
    page.on('pageerror', error => {
        console.log(`BROWSER ERROR: ${error.message}`);
    });
    
    try {
        console.log('\n1. Loading phlebotomy dashboard...');
        await page.goto('http://localhost:8080/phlebotomy/dashboard.html');
        
        // Wait for page to load
        await page.waitForTimeout(2000);
        
        console.log('\n2. Checking page title...');
        const title = await page.title();
        console.log(`Page title: ${title}`);
        
        console.log('\n3. Checking if PhlebotomyApp is loaded...');
        const phlebotomyAppExists = await page.evaluate(() => {
            return typeof PhlebotomyApp !== 'undefined';
        });
        console.log(`PhlebotomyApp exists: ${phlebotomyAppExists}`);
        
        console.log('\n4. Checking if phlebotomyApp instance exists...');
        const phlebotomyAppInstance = await page.evaluate(() => {
            return typeof phlebotomyApp !== 'undefined';
        });
        console.log(`phlebotomyApp instance exists: ${phlebotomyAppInstance}`);
        
        console.log('\n5. Checking dashboard elements...');
        
        // Check sidebar
        const sidebar = await page.$('.sidebar');
        console.log(`Sidebar exists: ${sidebar !== null}`);
        
        // Check navigation links
        const navLinks = await page.$$('.nav-link');
        console.log(`Navigation links count: ${navLinks.length}`);
        
        // Check main content
        const mainContent = await page.$('.main-content');
        console.log(`Main content exists: ${mainContent !== null}`);
        
        // Check statistics cards
        const statsCards = await page.$$('.stat-card');
        console.log(`Statistics cards count: ${statsCards.length}`);
        
        console.log('\n6. Testing navigation...');
        
        // Click on Sample Collection
        const sampleCollectionLink = await page.$('a[data-section="sample-collection"]');
        if (sampleCollectionLink) {
            await sampleCollectionLink.click();
            await page.waitForTimeout(1000);
            console.log('✅ Clicked Sample Collection link');
            
            // Check if sample collection section is visible
            const sampleCollectionSection = await page.$('#sample-collection');
            const isVisible = await sampleCollectionSection?.isVisible();
            console.log(`Sample Collection section visible: ${isVisible}`);
        } else {
            console.log('❌ Sample Collection link not found');
        }
        
        console.log('\n7. Testing API calls...');
        
        // Test if the page makes API calls
        const apiCalls = [];
        page.on('request', request => {
            if (request.url().includes('/sample-collection/') || 
                request.url().includes('/visits') || 
                request.url().includes('/samples')) {
                apiCalls.push(request.url());
            }
        });
        
        // Trigger dashboard load
        await page.evaluate(() => {
            if (typeof phlebotomyApp !== 'undefined' && phlebotomyApp.loadDashboardData) {
                phlebotomyApp.loadDashboardData();
            }
        });
        
        await page.waitForTimeout(2000);
        console.log(`API calls made: ${apiCalls.length}`);
        apiCalls.forEach(url => console.log(`  - ${url}`));
        
        console.log('\n8. Testing sample collection functionality...');
        
        // Check if there are any pending samples displayed
        const pendingSamplesTable = await page.$('#pending-samples-table tbody');
        if (pendingSamplesTable) {
            const rows = await pendingSamplesTable.$$('tr');
            console.log(`Pending samples rows: ${rows.length}`);
            
            if (rows.length > 0) {
                // Try to click the first collect button
                const collectButton = await rows[0].$('.btn-collect');
                if (collectButton) {
                    await collectButton.click();
                    await page.waitForTimeout(1000);
                    console.log('✅ Clicked collect button');
                    
                    // Check if modal opened
                    const modal = await page.$('#sample-collection-modal');
                    const modalVisible = await modal?.isVisible();
                    console.log(`Collection modal visible: ${modalVisible}`);
                } else {
                    console.log('❌ Collect button not found');
                }
            } else {
                console.log('⚠️ No pending samples found');
            }
        } else {
            console.log('❌ Pending samples table not found');
        }
        
        console.log('\n9. Checking for JavaScript errors...');
        
        // Execute some JavaScript to check for errors
        const jsTest = await page.evaluate(() => {
            const errors = [];
            
            // Check if required functions exist
            if (typeof PhlebotomyApp === 'undefined') {
                errors.push('PhlebotomyApp class not defined');
            }
            
            if (typeof phlebotomyApp === 'undefined') {
                errors.push('phlebotomyApp instance not created');
            } else {
                // Check if key methods exist
                if (typeof phlebotomyApp.loadDashboardData !== 'function') {
                    errors.push('loadDashboardData method missing');
                }
                if (typeof phlebotomyApp.showSection !== 'function') {
                    errors.push('showSection method missing');
                }
                if (typeof phlebotomyApp.loadPendingSamples !== 'function') {
                    errors.push('loadPendingSamples method missing');
                }
            }
            
            return errors;
        });
        
        if (jsTest.length === 0) {
            console.log('✅ No JavaScript errors found');
        } else {
            console.log('❌ JavaScript errors found:');
            jsTest.forEach(error => console.log(`  - ${error}`));
        }
        
        console.log('\n10. Final assessment...');
        
        const assessment = {
            pageLoads: title.includes('Phlebotomy'),
            jsLoaded: phlebotomyAppExists,
            instanceCreated: phlebotomyAppInstance,
            navigationWorks: navLinks.length >= 7,
            elementsPresent: sidebar !== null && mainContent !== null,
            jsErrors: jsTest.length === 0
        };
        
        const passedTests = Object.values(assessment).filter(Boolean).length;
        const totalTests = Object.keys(assessment).length;
        
        console.log(`\n=== PHLEBOTOMY DASHBOARD TEST RESULTS ===`);
        console.log(`Tests passed: ${passedTests}/${totalTests}`);
        console.log(`Success rate: ${Math.round((passedTests/totalTests) * 100)}%`);
        
        Object.entries(assessment).forEach(([test, passed]) => {
            console.log(`${passed ? '✅' : '❌'} ${test}`);
        });
        
        if (passedTests === totalTests) {
            console.log('\n🎉 PHLEBOTOMY DASHBOARD IS WORKING!');
        } else {
            console.log('\n❌ PHLEBOTOMY DASHBOARD HAS ISSUES!');
            console.log('Check the browser window for visual inspection.');
        }
        
        // Keep browser open for manual inspection
        console.log('\nBrowser will stay open for manual inspection...');
        console.log('Press Ctrl+C to close when done.');
        
        // Wait indefinitely
        await new Promise(() => {});
        
    } catch (error) {
        console.error('Test failed:', error);
    } finally {
        // Don't close browser automatically for manual inspection
        // await browser.close();
    }
}

// Run the tests
runPhlebotomyTests().catch(console.error);
