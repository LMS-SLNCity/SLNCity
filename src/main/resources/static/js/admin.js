// Admin Dashboard JavaScript
class AdminDashboard {
    constructor() {
        this.apiEndpoints = {
            visits: '/visits',
            billing: '/billing',
            equipment: '/api/v1/equipment',
            inventory: '/api/v1/inventory',
            samples: '/samples',
            tests: '/api/v1/tests',
            reports: '/api/v1/reports',
            health: '/actuator/health',
            monitoring: '/api/v1/monitoring',
            workflow: '/api/v1/workflow'
        };
        this.currentPage = 'overview';
        this.init();
    }

    init() {
        this.setupNavigation();
        this.setupEventListeners();
        this.loadDashboardData();
        this.startHealthMonitoring();
    }

    setupNavigation() {
        const navItems = document.querySelectorAll('.nav-item');
        navItems.forEach(item => {
            item.addEventListener('click', (e) => {
                const page = item.dataset.page;
                this.navigateToPage(page);
            });
        });
    }

    setupEventListeners() {
        // Refresh button
        document.getElementById('refresh-btn').addEventListener('click', () => {
            this.refreshCurrentPage();
        });

        // Mobile sidebar toggle (if needed)
        this.setupMobileNavigation();
    }

    setupMobileNavigation() {
        // Add mobile menu toggle if screen is small
        if (window.innerWidth <= 768) {
            const sidebar = document.querySelector('.sidebar');
            const mainContent = document.querySelector('.main-content');
            
            // Add menu toggle button
            const menuToggle = document.createElement('button');
            menuToggle.innerHTML = '<i class="fas fa-bars"></i>';
            menuToggle.className = 'mobile-menu-toggle';
            menuToggle.style.cssText = `
                position: fixed;
                top: 1rem;
                left: 1rem;
                z-index: 1001;
                background: var(--primary-color);
                color: white;
                border: none;
                padding: 0.5rem;
                border-radius: 4px;
                cursor: pointer;
            `;
            
            document.body.appendChild(menuToggle);
            
            menuToggle.addEventListener('click', () => {
                sidebar.classList.toggle('open');
            });
            
            // Close sidebar when clicking outside
            mainContent.addEventListener('click', () => {
                sidebar.classList.remove('open');
            });
        }
    }

    navigateToPage(page) {
        // Update navigation
        document.querySelectorAll('.nav-item').forEach(item => {
            item.classList.remove('active');
        });
        document.querySelector(`[data-page="${page}"]`).classList.add('active');

        // Update page content
        document.querySelectorAll('.page').forEach(p => {
            p.classList.remove('active');
        });
        document.getElementById(`${page}-page`).classList.add('active');

        // Update page title
        this.updatePageTitle(page);
        
        // Load page-specific data
        this.loadPageData(page);
        
        this.currentPage = page;
    }

    updatePageTitle(page) {
        const titles = {
            overview: { title: 'System Overview', subtitle: 'Complete administrative control panel' },
            users: { title: 'User Management', subtitle: 'Manage system users and roles' },
            equipment: { title: 'Equipment Management', subtitle: 'Laboratory equipment and maintenance' },
            inventory: { title: 'Inventory Management', subtitle: 'Stock levels and procurement' },
            'test-templates': { title: 'Test Template Management', subtitle: 'Create and manage laboratory test templates' },
            visits: { title: 'Visit Management', subtitle: 'Patient visits and appointments' },
            reports: { title: 'Reports & Analytics', subtitle: 'System performance and insights' },
            system: { title: 'System Configuration', subtitle: 'Application settings and preferences' },
            audit: { title: 'Audit Trail', subtitle: 'Security logs and user activities' }
        };

        const pageInfo = titles[page] || { title: 'Dashboard', subtitle: 'Lab Operations Management' };
        document.getElementById('page-title').textContent = pageInfo.title;
        document.getElementById('page-subtitle').textContent = pageInfo.subtitle;
    }

    async loadDashboardData() {
        try {
            await Promise.all([
                this.loadSystemStats(),
                this.loadSystemHealth(),
                this.loadRecentActivities()
            ]);
        } catch (error) {
            console.error('Error loading dashboard data:', error);
            this.showNotification('Error loading dashboard data', 'error');
        }
    }

    async loadSystemStats() {
        try {
            // Load visits count
            const visitsResponse = await fetch(this.apiEndpoints.visits);
            if (visitsResponse.ok) {
                const visits = await visitsResponse.json();
                document.getElementById('total-visits').textContent = visits.length;
            }

            // Load equipment count
            const equipmentResponse = await fetch(this.apiEndpoints.equipment);
            if (equipmentResponse.ok) {
                const equipment = await equipmentResponse.json();
                document.getElementById('total-equipment').textContent = equipment.length;
            }

            // Load billing data for revenue
            const billingResponse = await fetch(this.apiEndpoints.billing);
            if (billingResponse.ok) {
                const bills = await billingResponse.json();
                const totalRevenue = bills.reduce((sum, bill) => sum + (bill.totalAmount || 0), 0);
                document.getElementById('total-revenue').textContent = `₹${totalRevenue.toLocaleString()}`;
            }

            // Set active users (mock data for now)
            document.getElementById('total-users').textContent = '4';

        } catch (error) {
            console.error('Error loading system stats:', error);
        }
    }

    async loadSystemHealth() {
        try {
            const response = await fetch(this.apiEndpoints.health);
            if (response.ok) {
                const health = await response.json();
                const isHealthy = health.status === 'UP';
                
                // Update system status indicator
                const statusIndicator = document.getElementById('system-status');
                const statusText = document.getElementById('status-text');
                const healthIndicator = document.getElementById('health-indicator');
                
                if (isHealthy) {
                    statusIndicator.style.background = 'var(--success-color)';
                    statusText.textContent = 'System Healthy';
                    healthIndicator.style.color = 'var(--success-color)';
                } else {
                    statusIndicator.style.background = 'var(--danger-color)';
                    statusText.textContent = 'System Issues';
                    healthIndicator.style.color = 'var(--danger-color)';
                }

                // Update detailed health information
                this.updateHealthDetails(health);
            }
        } catch (error) {
            console.error('Error loading system health:', error);
            document.getElementById('status-text').textContent = 'Health Check Failed';
            document.getElementById('system-status').style.background = 'var(--danger-color)';
        }
    }

    updateHealthDetails(health) {
        const healthContainer = document.getElementById('system-health');
        if (!healthContainer) return;

        // Clear existing health items
        healthContainer.innerHTML = '';

        // Add database status
        const dbStatus = health.components?.db?.status || 'UNKNOWN';
        this.addHealthItem(healthContainer, 'Database', dbStatus);

        // Add disk space status
        const diskStatus = health.components?.diskSpace?.status || 'UNKNOWN';
        this.addHealthItem(healthContainer, 'Disk Space', diskStatus);

        // Add system health service status
        const systemHealthStatus = health.components?.systemHealthService?.status || 'UNKNOWN';
        this.addHealthItem(healthContainer, 'System Services', systemHealthStatus);

        // Add memory usage if available
        if (health.components?.systemHealthService?.details?.components?.system?.details) {
            const memoryUsage = health.components.systemHealthService.details.components.system.details.memoryUsagePercent;
            this.addHealthItem(healthContainer, 'Memory Usage', memoryUsage);
        }
    }

    addHealthItem(container, label, status) {
        const item = document.createElement('div');
        item.className = 'health-item';
        
        const labelSpan = document.createElement('span');
        labelSpan.textContent = label;
        
        const statusSpan = document.createElement('span');
        statusSpan.textContent = status;
        statusSpan.className = 'status-badge';
        
        // Determine status class
        if (status === 'UP' || status === 'OK') {
            statusSpan.classList.add('status-up');
        } else if (status.includes('%')) {
            const percentage = parseFloat(status);
            if (percentage > 80) {
                statusSpan.classList.add('status-warning');
            } else {
                statusSpan.classList.add('status-up');
            }
        } else if (status === 'DOWN' || status === 'UNKNOWN') {
            statusSpan.classList.add('status-down');
        } else {
            statusSpan.classList.add('status-up');
        }
        
        item.appendChild(labelSpan);
        item.appendChild(statusSpan);
        container.appendChild(item);
    }

    loadRecentActivities() {
        // Mock recent activities for now
        const activities = [
            { icon: 'fas fa-user-plus', text: 'New user registered', time: '2 minutes ago' },
            { icon: 'fas fa-calendar-plus', text: 'New visit scheduled', time: '5 minutes ago' },
            { icon: 'fas fa-cog', text: 'Equipment maintenance completed', time: '1 hour ago' },
            { icon: 'fas fa-chart-line', text: 'Monthly report generated', time: '2 hours ago' }
        ];

        const container = document.getElementById('recent-activities');
        if (container) {
            container.innerHTML = activities.map(activity => `
                <div class="activity-item">
                    <i class="${activity.icon}"></i>
                    <div class="activity-content">
                        <p>${activity.text}</p>
                        <small>${activity.time}</small>
                    </div>
                </div>
            `).join('');
        }
    }

    async loadPageData(page) {
        switch (page) {
            case 'overview':
                await this.loadDashboardData();
                break;
            case 'equipment':
                await this.loadEquipmentData();
                break;
            case 'inventory':
                await this.loadInventoryData();
                break;
            case 'test-templates':
                await this.loadTestTemplates();
                break;
            case 'visits':
                await this.loadVisitsData();
                break;
            // Add other page data loading methods
        }
    }

    async loadEquipmentData() {
        // Implementation for equipment page
        console.log('Loading equipment data...');
    }

    async loadInventoryData() {
        // Implementation for inventory page
        console.log('Loading inventory data...');
    }

    async loadVisitsData() {
        // Implementation for visits page
        console.log('Loading visits data...');
    }

    startHealthMonitoring() {
        // Refresh health status every 30 seconds
        setInterval(() => {
            this.loadSystemHealth();
        }, 30000);
    }

    refreshCurrentPage() {
        const refreshBtn = document.getElementById('refresh-btn');
        const icon = refreshBtn.querySelector('i');
        
        // Add spinning animation
        icon.style.animation = 'spin 1s linear infinite';
        
        // Reload current page data
        this.loadPageData(this.currentPage);
        
        // Remove spinning animation after 1 second
        setTimeout(() => {
            icon.style.animation = '';
        }, 1000);
    }

    showNotification(message, type = 'info') {
        // Create notification element
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.textContent = message;
        notification.style.cssText = `
            position: fixed;
            top: 1rem;
            right: 1rem;
            padding: 1rem;
            border-radius: 6px;
            color: white;
            z-index: 1000;
            animation: slideIn 0.3s ease;
        `;
        
        // Set background color based on type
        const colors = {
            success: 'var(--success-color)',
            error: 'var(--danger-color)',
            warning: 'var(--warning-color)',
            info: 'var(--info-color)'
        };
        notification.style.background = colors[type] || colors.info;
        
        document.body.appendChild(notification);
        
        // Remove notification after 3 seconds
        setTimeout(() => {
            notification.remove();
        }, 3000);
    }

    // Test Template Management Methods
    async loadTestTemplates() {
        try {
            const response = await fetch('/test-templates');
            if (!response.ok) throw new Error('Failed to load test templates');

            const templates = await response.json();
            this.displayTestTemplates(templates);
        } catch (error) {
            console.error('Error loading test templates:', error);
            this.showNotification('Error loading test templates', 'error');
        }
    }

    displayTestTemplates(templates) {
        const tbody = document.querySelector('#test-templates-table tbody');
        if (!tbody) return;

        if (templates.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center">No test templates found</td></tr>';
            return;
        }

        tbody.innerHTML = templates.map(template => `
            <tr>
                <td>${template.templateId}</td>
                <td>${template.name}</td>
                <td>${template.description}</td>
                <td>
                    <span class="parameter-count">${Object.keys(template.parameters || {}).length} parameters</span>
                </td>
                <td>₹${template.basePrice.toFixed(2)}</td>
                <td>${new Date(template.createdAt).toLocaleDateString()}</td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="adminApp.editTestTemplate(${template.templateId})">
                        <i class="fas fa-edit"></i> Edit
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="adminApp.deleteTestTemplate(${template.templateId})">
                        <i class="fas fa-trash"></i> Delete
                    </button>
                </td>
            </tr>
        `).join('');
    }

    showAddTestTemplateModal() {
        document.getElementById('test-template-modal-title').textContent = 'Add New Test Template';
        document.getElementById('test-template-form').reset();
        document.getElementById('template-id').value = '';
        document.getElementById('parameters-container').innerHTML = '';
        this.addParameter(); // Add one default parameter
        document.getElementById('test-template-modal').style.display = 'block';
    }

    async editTestTemplate(templateId) {
        try {
            const response = await fetch(`/test-templates/${templateId}`);
            if (!response.ok) throw new Error('Failed to load test template');

            const template = await response.json();

            document.getElementById('test-template-modal-title').textContent = 'Edit Test Template';
            document.getElementById('template-id').value = template.templateId;
            document.getElementById('template-name').value = template.name;
            document.getElementById('template-description').value = template.description;
            document.getElementById('template-price').value = template.basePrice;

            // Load parameters
            const container = document.getElementById('parameters-container');
            container.innerHTML = '';

            if (template.parameters) {
                Object.entries(template.parameters).forEach(([key, value]) => {
                    this.addParameter(key, value.unit, value.normalRange);
                });
            }

            if (container.children.length === 0) {
                this.addParameter(); // Add one default parameter if none exist
            }

            document.getElementById('test-template-modal').style.display = 'block';
        } catch (error) {
            console.error('Error loading test template:', error);
            this.showNotification('Error loading test template', 'error');
        }
    }

    addParameter(name = '', unit = '', normalRange = '') {
        const container = document.getElementById('parameters-container');
        const parameterDiv = document.createElement('div');
        parameterDiv.className = 'parameter-group';

        parameterDiv.innerHTML = `
            <div class="parameter-row">
                <input type="text" placeholder="Parameter Name (e.g., hemoglobin)" value="${name}" class="param-name">
                <input type="text" placeholder="Unit (e.g., g/dL)" value="${unit}" class="param-unit">
                <input type="text" placeholder="Normal Range (e.g., 12.0-15.5)" value="${normalRange}" class="param-range">
                <button type="button" class="btn btn-sm btn-danger" onclick="this.parentElement.parentElement.remove()">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        `;

        container.appendChild(parameterDiv);
    }

    async saveTestTemplate() {
        try {
            const form = document.getElementById('test-template-form');
            const formData = new FormData(form);

            // Collect parameters
            const parameters = {};
            const parameterGroups = document.querySelectorAll('.parameter-group');

            parameterGroups.forEach(group => {
                const name = group.querySelector('.param-name').value.trim();
                const unit = group.querySelector('.param-unit').value.trim();
                const normalRange = group.querySelector('.param-range').value.trim();

                if (name) {
                    parameters[name] = {
                        unit: unit || '',
                        normalRange: normalRange || ''
                    };
                }
            });

            const templateData = {
                name: formData.get('name'),
                description: formData.get('description'),
                basePrice: parseFloat(formData.get('basePrice')),
                parameters: parameters
            };

            const templateId = formData.get('templateId');
            const isEdit = templateId && templateId !== '';

            const response = await fetch(isEdit ? `/test-templates/${templateId}` : '/test-templates', {
                method: isEdit ? 'PUT' : 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(templateData)
            });

            if (!response.ok) throw new Error('Failed to save test template');

            this.showNotification(`Test template ${isEdit ? 'updated' : 'created'} successfully`, 'success');
            this.closeTestTemplateModal();
            this.loadTestTemplates();

        } catch (error) {
            console.error('Error saving test template:', error);
            this.showNotification('Error saving test template', 'error');
        }
    }

    closeTestTemplateModal() {
        document.getElementById('test-template-modal').style.display = 'none';
    }

    async deleteTestTemplate(templateId) {
        if (!confirm('Are you sure you want to delete this test template?')) return;

        try {
            const response = await fetch(`/test-templates/${templateId}`, {
                method: 'DELETE'
            });

            if (!response.ok) throw new Error('Failed to delete test template');

            this.showNotification('Test template deleted successfully', 'success');
            this.loadTestTemplates();

        } catch (error) {
            console.error('Error deleting test template:', error);
            this.showNotification('Error deleting test template', 'error');
        }
    }
}

// Add CSS for animations
const style = document.createElement('style');
style.textContent = `
    @keyframes spin {
        from { transform: rotate(0deg); }
        to { transform: rotate(360deg); }
    }
    
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
`;
document.head.appendChild(style);

// Initialize admin dashboard when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.adminDashboard = new AdminDashboard();
    window.adminApp = window.adminDashboard; // Alias for modal functions
});
