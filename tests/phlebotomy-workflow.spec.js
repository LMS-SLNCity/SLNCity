const { test, expect } = require('@playwright/test');

test.describe('Phlebotomy Workflow - Complete UI Testing', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate to the phlebotomy dashboard
    await page.goto('http://localhost:8080/phlebotomy/dashboard.html');
    // Wait for the page to fully load
    await page.waitForLoadState('networkidle');
  });

  test('should load phlebotomy dashboard with all elements', async ({ page }) => {
    // Check if the page title is correct
    await expect(page).toHaveTitle(/Phlebotomy Dashboard/);

    // Check if main layout elements are visible
    await expect(page.locator('.sidebar')).toBeVisible();
    await expect(page.locator('.main-content')).toBeVisible();
    await expect(page.locator('.stats-grid')).toBeVisible();

    // Check if header is present
    await expect(page.locator('h1')).toContainText('Phlebotomy Dashboard');
  });

  test('should display complete navigation menu', async ({ page }) => {
    // Check if all 7 navigation items are present and visible
    const navItems = [
      { href: '#dashboard', text: 'Dashboard' },
      { href: '#sample-collection', text: 'Sample Collection' },
      { href: '#collection-queue', text: 'Collection Queue' },
      { href: '#sample-tracking', text: 'Sample Tracking' },
      { href: '#collection-history', text: 'Collection History' },
      { href: '#supplies', text: 'Supplies' },
      { href: '#reports', text: 'Reports' }
    ];

    for (const item of navItems) {
      await expect(page.locator(`nav a[href="${item.href}"]`)).toBeVisible();
      await expect(page.locator(`nav a[href="${item.href}"]`)).toContainText(item.text);
    }
  });

  test('should navigate between all sections correctly', async ({ page }) => {
    const sections = [
      '#sample-collection',
      '#collection-queue',
      '#sample-tracking',
      '#collection-history',
      '#supplies',
      '#reports',
      '#dashboard'
    ];

    for (const section of sections) {
      await page.click(`nav a[href="${section}"]`);
      await expect(page.locator(section)).toBeVisible();

      // Verify the section is active
      await expect(page.locator(`nav a[href="${section}"]`)).toHaveClass(/active/);
    }
  });

  test('should display all statistics cards with correct data', async ({ page }) => {
    // Wait for stats to load
    await page.waitForTimeout(2000);

    // Check if all 4 stat cards are present
    const statCards = page.locator('.stat-card');
    await expect(statCards).toHaveCount(4);

    // Check specific stat card content
    await expect(statCards.nth(0)).toContainText('Pending Collections');
    await expect(statCards.nth(1)).toContainText('Today\'s Collections');
    await expect(statCards.nth(2)).toContainText('Samples Processing');
    await expect(statCards.nth(3)).toContainText('Collection Efficiency');

    // Check if stat values are displayed (should be numbers)
    for (let i = 0; i < 4; i++) {
      const statValue = statCards.nth(i).locator('.stat-value');
      await expect(statValue).toBeVisible();
    }
  });

  test('should load and display pending samples correctly', async ({ page }) => {
    // Navigate to sample collection section
    await page.click('nav a[href="#sample-collection"]');
    await expect(page.locator('#sample-collection')).toBeVisible();

    // Wait for data to load
    await page.waitForTimeout(2000);

    // Check if pending samples table is visible
    await expect(page.locator('#pendingSamplesTable')).toBeVisible();

    // Check table headers
    const expectedHeaders = ['Patient', 'Test', 'Sample Type', 'Priority', 'Actions'];
    for (const header of expectedHeaders) {
      await expect(page.locator(`th:has-text("${header}")`)).toBeVisible();
    }

    // Check if there are any pending samples (should be at least 1 from our test data)
    const tableRows = page.locator('#pendingSamplesTable tbody tr');
    const rowCount = await tableRows.count();
    expect(rowCount).toBeGreaterThan(0);
  });

  test('should open and handle sample collection modal', async ({ page }) => {
    // Navigate to sample collection section
    await page.click('nav a[href="#sample-collection"]');
    await page.waitForTimeout(2000);

    // Check if there are collect buttons and click the first one
    const collectButtons = page.locator('button:has-text("Collect")');
    const buttonCount = await collectButtons.count();

    if (buttonCount > 0) {
      await collectButtons.first().click();

      // Check if modal opens
      await expect(page.locator('#sampleCollectionModal')).toBeVisible();
      await expect(page.locator('.modal-title')).toContainText('Sample Collection');

      // Check if all form fields are present
      await expect(page.locator('#sampleType')).toBeVisible();
      await expect(page.locator('#collectionSite')).toBeVisible();
      await expect(page.locator('#containerType')).toBeVisible();
      await expect(page.locator('#volumeReceived')).toBeVisible();
      await expect(page.locator('#collectionNotes')).toBeVisible();

      // Test form interaction
      await page.selectOption('#sampleType', 'WHOLE_BLOOD');
      await page.fill('#collectionSite', 'Left Arm');
      await page.fill('#containerType', 'EDTA Tube');
      await page.fill('#volumeReceived', '5.0');
      await page.fill('#collectionNotes', 'Test collection');

      // Test modal close functionality
      await page.click('.modal .close');
      await expect(page.locator('#sampleCollectionModal')).not.toBeVisible();
    }
  });

  test('should perform complete sample collection workflow', async ({ page }) => {
    // Navigate to sample collection section
    await page.click('nav a[href="#sample-collection"]');
    await page.waitForTimeout(2000);

    // Get initial pending count
    const initialPendingText = await page.locator('.stat-card').nth(0).locator('.stat-value').textContent();
    const initialPending = parseInt(initialPendingText || '0');

    // Check if there are collect buttons
    const collectButtons = page.locator('button:has-text("Collect")');
    const buttonCount = await collectButtons.count();

    if (buttonCount > 0) {
      await collectButtons.first().click();

      // Fill out the collection form
      await expect(page.locator('#sampleCollectionModal')).toBeVisible();
      await page.selectOption('#sampleType', 'WHOLE_BLOOD');
      await page.fill('#collectionSite', 'Left Arm');
      await page.fill('#containerType', 'EDTA Tube');
      await page.fill('#volumeReceived', '5.0');
      await page.fill('#collectionNotes', 'Automated test collection');

      // Submit the form
      await page.click('button:has-text("Collect Sample")');

      // Wait for the collection to process
      await page.waitForTimeout(3000);

      // Check if modal closes
      await expect(page.locator('#sampleCollectionModal')).not.toBeVisible();

      // Verify the pending count decreased
      await page.waitForTimeout(2000);
      const newPendingText = await page.locator('.stat-card').nth(0).locator('.stat-value').textContent();
      const newPending = parseInt(newPendingText || '0');

      expect(newPending).toBeLessThan(initialPending);
    }
  });

  test('should display collection queue with proper data', async ({ page }) => {
    // Navigate to collection queue section
    await page.click('nav a[href="#collection-queue"]');
    await expect(page.locator('#collection-queue')).toBeVisible();
    await page.waitForTimeout(2000);

    // Check if collection queue table is visible
    await expect(page.locator('#collectionQueueTable')).toBeVisible();

    // Check for queue-specific elements
    await expect(page.locator('h2:has-text("Collection Queue")')).toBeVisible();
  });

  test('should display sample tracking functionality', async ({ page }) => {
    // Navigate to sample tracking section
    await page.click('nav a[href="#sample-tracking"]');
    await expect(page.locator('#sample-tracking')).toBeVisible();
    await page.waitForTimeout(2000);

    // Check if sample tracking table is visible
    await expect(page.locator('#sampleTrackingTable')).toBeVisible();

    // Check for tracking-specific elements
    await expect(page.locator('h2:has-text("Sample Tracking")')).toBeVisible();
  });

  test('should display collection history with data', async ({ page }) => {
    // Navigate to collection history section
    await page.click('nav a[href="#collection-history"]');
    await expect(page.locator('#collection-history')).toBeVisible();
    await page.waitForTimeout(2000);

    // Check if collection history table is visible
    await expect(page.locator('#collectionHistoryTable')).toBeVisible();

    // Check for history-specific elements
    await expect(page.locator('h2:has-text("Collection History")')).toBeVisible();
  });

  test('should display supplies management section', async ({ page }) => {
    // Navigate to supplies section
    await page.click('nav a[href="#supplies"]');
    await expect(page.locator('#supplies')).toBeVisible();
    await page.waitForTimeout(2000);

    // Check if supplies table is visible
    await expect(page.locator('#suppliesTable')).toBeVisible();

    // Check for supplies-specific elements
    await expect(page.locator('h2:has-text("Supplies Management")')).toBeVisible();
  });

  test('should display reports section with options', async ({ page }) => {
    // Navigate to reports section
    await page.click('nav a[href="#reports"]');
    await expect(page.locator('#reports')).toBeVisible();
    await page.waitForTimeout(2000);

    // Check for reports-specific elements
    await expect(page.locator('h2:has-text("Reports")')).toBeVisible();

    // Check for report generation options
    await expect(page.locator('button:has-text("Generate")')).toBeVisible();
  });

  test('should handle responsive design elements', async ({ page }) => {
    // Test desktop view
    await page.setViewportSize({ width: 1200, height: 800 });
    await expect(page.locator('.sidebar')).toBeVisible();
    await expect(page.locator('.main-content')).toBeVisible();

    // Test tablet view
    await page.setViewportSize({ width: 768, height: 600 });
    await page.waitForTimeout(1000);

    // Test mobile view
    await page.setViewportSize({ width: 480, height: 600 });
    await page.waitForTimeout(1000);

    // Reset to desktop
    await page.setViewportSize({ width: 1200, height: 800 });
  });

  test('should verify all dashboard sections are functional', async ({ page }) => {
    const sections = [
      { id: '#dashboard', title: 'Dashboard' },
      { id: '#sample-collection', title: 'Sample Collection' },
      { id: '#collection-queue', title: 'Collection Queue' },
      { id: '#sample-tracking', title: 'Sample Tracking' },
      { id: '#collection-history', title: 'Collection History' },
      { id: '#supplies', title: 'Supplies Management' },
      { id: '#reports', title: 'Reports' }
    ];

    for (const section of sections) {
      // Navigate to section
      await page.click(`nav a[href="${section.id}"]`);
      await expect(page.locator(section.id)).toBeVisible();

      // Wait for content to load
      await page.waitForTimeout(1000);

      // Verify section has content
      const sectionContent = page.locator(section.id);
      await expect(sectionContent).not.toBeEmpty();
    }
  });

});
