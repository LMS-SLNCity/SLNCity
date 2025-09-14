/**
 * Phlebotomy Dashboard JavaScript Application
 */
class PhlebotomyApp {
    constructor() {
        this.currentSection = 'dashboard';
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.loadDashboardData();
        this.loadCollectionSchedule();
    }

    setupEventListeners() {
        // Navigation
        document.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const section = e.target.closest('.nav-link').dataset.section;
                this.showSection(section);
            });
        });

        // Sample collection form
        const collectionForm = document.getElementById('sample-collection-form');
        if (collectionForm) {
            collectionForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.collectSample();
            });
        }

        // Auto-refresh data every 30 seconds
        setInterval(() => {
            if (this.currentSection === 'dashboard') {
                this.loadDashboardData();
                this.loadCollectionSchedule();
            }
        }, 30000);
    }

    showSection(sectionName) {
        // Hide all sections
        document.querySelectorAll('.content-section').forEach(section => {
            section.style.display = 'none';
        });

        // Remove active class from all nav links
        document.querySelectorAll('.nav-link').forEach(link => {
            link.classList.remove('active');
        });

        // Show selected section
        const targetSection = document.getElementById(`${sectionName}-section`);
        if (targetSection) {
            targetSection.style.display = 'block';
        }

        // Add active class to selected nav link
        const activeLink = document.querySelector(`[data-section="${sectionName}"]`);
        if (activeLink) {
            activeLink.classList.add('active');
        }

        this.currentSection = sectionName;

        // Load section-specific data
        switch (sectionName) {
            case 'collection-queue':
                this.loadCollectionQueue();
                break;
            case 'collection-history':
                this.loadCollectionHistory();
                break;
            case 'supplies':
                this.loadSupplies();
                break;
        }
    }

    async loadDashboardData() {
        try {
            // Load statistics
            const [visits, samples] = await Promise.all([
                this.fetchData('/visits'),
                this.fetchData('/samples')
            ]);

            // Calculate statistics
            const pendingCollections = visits.filter(v => v.status === 'PENDING').length;
            const todayCollections = this.getTodayCount(samples);
            const samplesProcessing = samples.filter(s => s.status === 'PROCESSING').length;
            const efficiency = this.calculateEfficiency(samples);

            // Update dashboard cards
            document.getElementById('pending-collections').textContent = pendingCollections;
            document.getElementById('today-collections').textContent = todayCollections;
            document.getElementById('samples-processing').textContent = samplesProcessing;
            document.getElementById('collection-efficiency').textContent = `${efficiency}%`;

        } catch (error) {
            console.error('Error loading dashboard data:', error);
            this.showNotification('Error loading dashboard data', 'error');
        }
    }

    async loadCollectionSchedule() {
        try {
            // Load pending samples that need collection
            const pendingSamples = await this.fetchData('/sample-collection/pending');

            const tableBody = document.querySelector('#collection-schedule-table tbody');
            if (!tableBody) return;

            if (pendingSamples.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="6" class="text-center">No samples pending collection</td></tr>';
                return;
            }

            tableBody.innerHTML = pendingSamples.map(sample => `
                <tr>
                    <td>${this.formatTime(new Date())}</td>
                    <td>${sample.patientName || 'N/A'}</td>
                    <td>${sample.testName || 'Unknown Test'}</td>
                    <td>${this.getSampleTypeForTest(sample.testName)}</td>
                    <td><span class="status-badge status-pending">PENDING</span></td>
                    <td>
                        <button class="btn btn-sm btn-primary" onclick="phlebotomyApp.collectSample(${sample.testId}, '${sample.testName}')">
                            <i class="fas fa-vial"></i> Collect
                        </button>
                    </td>
                </tr>
            `).join('');

        } catch (error) {
            console.error('Error loading collection schedule:', error);
        }
    }

    async loadCollectionQueue() {
        try {
            const visits = await this.fetchData('/visits');
            const queueVisits = visits.filter(v => v.status === 'PENDING');
            
            const tableBody = document.querySelector('#collection-queue-table tbody');
            if (!tableBody) return;

            if (queueVisits.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="7" class="text-center">No patients in queue</td></tr>';
                return;
            }

            tableBody.innerHTML = queueVisits.map((visit, index) => `
                <tr>
                    <td>${index + 1}</td>
                    <td>${visit.patientDetails?.name || 'N/A'}</td>
                    <td>${visit.visitId}</td>
                    <td>${this.getTestNames(visit.labTests)}</td>
                    <td><span class="priority-badge ${this.getPriority(visit.labTests)}">${this.getPriorityText(visit.labTests)}</span></td>
                    <td>${this.calculateWaitTime(visit.createdAt)}</td>
                    <td>
                        <button class="btn btn-sm btn-success" onclick="phlebotomyApp.callPatient(${visit.visitId})">
                            <i class="fas fa-user"></i> Call
                        </button>
                    </td>
                </tr>
            `).join('');

        } catch (error) {
            console.error('Error loading collection queue:', error);
        }
    }

    async loadCollectionHistory() {
        try {
            const samples = await this.fetchData('/samples');
            
            const tableBody = document.querySelector('#collection-history-table tbody');
            if (!tableBody) return;

            if (samples.length === 0) {
                tableBody.innerHTML = '<tr><td colspan="6" class="text-center">No collection history</td></tr>';
                return;
            }

            tableBody.innerHTML = samples.slice(0, 20).map(sample => `
                <tr>
                    <td>${this.formatDate(sample.collectedAt)}</td>
                    <td>${sample.sampleNumber}</td>
                    <td>Patient ${sample.visitId}</td>
                    <td>${sample.sampleType}</td>
                    <td>${sample.collectedBy}</td>
                    <td><span class="status-badge status-${sample.status.toLowerCase()}">${sample.status}</span></td>
                </tr>
            `).join('');

        } catch (error) {
            console.error('Error loading collection history:', error);
        }
    }

    async collectSample() {
        try {
            const form = document.getElementById('sample-collection-form');
            const formData = new FormData(form);
            
            const sampleData = {
                visitId: parseInt(formData.get('visitId')),
                sampleType: formData.get('sampleType'),
                collectedBy: formData.get('collectedBy'),
                collectionSite: formData.get('collectionSite'),
                collectionConditions: {
                    notes: formData.get('notes')
                }
            };

            const response = await fetch('/samples/collect', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(sampleData)
            });

            if (response.ok) {
                const result = await response.json();
                this.showNotification(`Sample collected successfully! Sample #: ${result.sampleNumber}`, 'success');
                form.reset();
                this.loadDashboardData();
            } else {
                throw new Error('Failed to collect sample');
            }

        } catch (error) {
            console.error('Error collecting sample:', error);
            this.showNotification('Error collecting sample', 'error');
        }
    }

    async searchSample() {
        const searchTerm = document.getElementById('search-sample').value.trim();
        if (!searchTerm) {
            this.showNotification('Please enter a sample number or visit ID', 'warning');
            return;
        }

        try {
            // Try to search by sample number first, then by visit ID
            let sample = null;
            
            if (searchTerm.includes('-')) {
                // Looks like a sample number
                sample = await this.fetchData(`/samples/${searchTerm}`);
            } else {
                // Looks like a visit ID
                const samples = await this.fetchData(`/samples/visit/${searchTerm}`);
                sample = samples[0]; // Get first sample for the visit
            }

            const resultsDiv = document.getElementById('sample-tracking-results');
            if (sample) {
                resultsDiv.innerHTML = `
                    <div class="sample-details">
                        <h4>Sample Details</h4>
                        <div class="detail-grid">
                            <div><strong>Sample Number:</strong> ${sample.sampleNumber}</div>
                            <div><strong>Visit ID:</strong> ${sample.visitId}</div>
                            <div><strong>Sample Type:</strong> ${sample.sampleType}</div>
                            <div><strong>Status:</strong> <span class="status-badge status-${sample.status.toLowerCase()}">${sample.status}</span></div>
                            <div><strong>Collected By:</strong> ${sample.collectedBy}</div>
                            <div><strong>Collection Date:</strong> ${this.formatDateTime(sample.collectedAt)}</div>
                        </div>
                    </div>
                `;
            } else {
                resultsDiv.innerHTML = '<p class="text-center">Sample not found</p>';
            }

        } catch (error) {
            console.error('Error searching sample:', error);
            document.getElementById('sample-tracking-results').innerHTML = '<p class="text-center">Error searching sample</p>';
        }
    }

    startCollection(visitId) {
        this.showSection('sample-collection');
        document.getElementById('visit-id').value = visitId;
    }

    callPatient(visitId) {
        this.showNotification(`Patient for Visit #${visitId} has been called`, 'info');
        // In a real application, this might trigger a notification system
    }

    // Utility methods
    async fetchData(endpoint) {
        try {
            const response = await fetch(endpoint);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error(`Error fetching ${endpoint}:`, error);
            return [];
        }
    }

    getTodayCount(items) {
        const today = new Date().toDateString();
        return items.filter(item => 
            item.collectedAt && new Date(item.collectedAt).toDateString() === today
        ).length;
    }

    calculateEfficiency(samples) {
        if (samples.length === 0) return 0;
        const successful = samples.filter(s => s.status === 'COMPLETED' || s.status === 'RECEIVED').length;
        return Math.round((successful / samples.length) * 100);
    }

    calculateWaitTime(createdAt) {
        const now = new Date();
        const created = new Date(createdAt);
        const diffMinutes = Math.floor((now - created) / (1000 * 60));
        
        if (diffMinutes < 60) {
            return `${diffMinutes} min`;
        } else {
            const hours = Math.floor(diffMinutes / 60);
            const minutes = diffMinutes % 60;
            return `${hours}h ${minutes}m`;
        }
    }

    formatTime(dateString) {
        return new Date(dateString).toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    formatDate(dateString) {
        return new Date(dateString).toLocaleDateString('en-US');
    }

    formatDateTime(dateString) {
        return new Date(dateString).toLocaleString('en-US');
    }

    showNotification(message, type = 'info') {
        // Create notification element
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.innerHTML = `
            <i class="fas fa-${this.getNotificationIcon(type)}"></i>
            <span>${message}</span>
            <button class="notification-close" onclick="this.parentElement.remove()">×</button>
        `;

        // Add to page
        document.body.appendChild(notification);

        // Auto-remove after 5 seconds
        setTimeout(() => {
            if (notification.parentElement) {
                notification.remove();
            }
        }, 5000);
    }

    getNotificationIcon(type) {
        const icons = {
            success: 'check-circle',
            error: 'exclamation-circle',
            warning: 'exclamation-triangle',
            info: 'info-circle'
        };
        return icons[type] || 'info-circle';
    }

    // Helper methods for test information
    getTestNames(labTests) {
        if (!labTests || labTests.length === 0) {
            return 'No tests ordered';
        }
        return labTests.map(test => test.testTemplate?.name || 'Unknown Test').join(', ');
    }

    getSampleTypes(labTests) {
        if (!labTests || labTests.length === 0) {
            return 'N/A';
        }

        // Map test names to sample types
        const sampleTypeMap = {
            'Complete Blood Count (CBC)': 'WHOLE_BLOOD',
            'Blood Sugar (Fasting)': 'SERUM',
            'Lipid Profile': 'SERUM',
            'Liver Function Test (LFT)': 'SERUM',
            'Kidney Function Test': 'SERUM',
            'Thyroid Function Test': 'SERUM',
            'Urine Analysis': 'RANDOM_URINE',
            'Stool Analysis': 'STOOL'
        };

        const sampleTypes = labTests.map(test => {
            const testName = test.testTemplate?.name || 'Unknown Test';
            return sampleTypeMap[testName] || 'WHOLE_BLOOD';
        });

        // Remove duplicates and join
        return [...new Set(sampleTypes)].join(', ');
    }

    getPriority(labTests) {
        if (!labTests || labTests.length === 0) {
            return 'normal';
        }

        // Determine priority based on test types
        const urgentTests = ['Cardiac Markers', 'Troponin', 'Emergency Panel'];
        const hasUrgentTest = labTests.some(test =>
            urgentTests.some(urgent => test.testTemplate?.name?.includes(urgent))
        );

        return hasUrgentTest ? 'urgent' : 'normal';
    }

    getPriorityText(labTests) {
        const priority = this.getPriority(labTests);
        return priority === 'urgent' ? 'Urgent' : 'Normal';
    }

    // Sample collection methods
    async collectSample(testId, testName) {
        try {
            // Show sample collection modal
            this.showSampleCollectionModal(testId, testName);
        } catch (error) {
            console.error('Error initiating sample collection:', error);
            this.showNotification('Error initiating sample collection', 'error');
        }
    }

    showSampleCollectionModal(testId, testName) {
        // Create modal HTML
        const modalHtml = `
            <div id="sample-collection-modal" class="modal">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3>Collect Sample - ${testName}</h3>
                        <span class="close" onclick="phlebotomyApp.closeSampleModal()">&times;</span>
                    </div>
                    <div class="modal-body">
                        <form id="sample-collection-form">
                            <div class="form-group">
                                <label for="sample-type">Sample Type *</label>
                                <select id="sample-type" name="sampleType" required>
                                    <option value="">Select sample type</option>
                                    <option value="WHOLE_BLOOD">Whole Blood</option>
                                    <option value="SERUM">Serum</option>
                                    <option value="PLASMA">Plasma</option>
                                    <option value="RANDOM_URINE">Random Urine</option>
                                    <option value="STOOL">Stool</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label for="collected-by">Collected By *</label>
                                <input type="text" id="collected-by" name="collectedBy" value="phlebotomy" required>
                            </div>
                            <div class="form-group">
                                <label for="collection-site">Collection Site</label>
                                <input type="text" id="collection-site" name="collectionSite" placeholder="e.g., Left arm">
                            </div>
                            <div class="form-group">
                                <label for="container-type">Container Type</label>
                                <input type="text" id="container-type" name="containerType" placeholder="e.g., EDTA tube">
                            </div>
                            <div class="form-group">
                                <label for="volume">Volume (ml)</label>
                                <input type="number" id="volume" name="volumeReceived" step="0.1" min="0">
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" onclick="phlebotomyApp.closeSampleModal()">Cancel</button>
                        <button type="button" class="btn btn-primary" onclick="phlebotomyApp.submitSampleCollection(${testId})">Collect Sample</button>
                    </div>
                </div>
            </div>
        `;

        // Add modal to page
        document.body.insertAdjacentHTML('beforeend', modalHtml);

        // Set default sample type based on test
        const sampleTypeSelect = document.getElementById('sample-type');
        const defaultType = this.getSampleTypeForTest(testName);
        sampleTypeSelect.value = defaultType;

        // Show modal
        document.getElementById('sample-collection-modal').style.display = 'block';
    }

    async submitSampleCollection(testId) {
        try {
            const form = document.getElementById('sample-collection-form');
            const formData = new FormData(form);

            const sampleData = {
                sampleType: formData.get('sampleType'),
                collectedBy: formData.get('collectedBy'),
                collectionSite: formData.get('collectionSite'),
                containerType: formData.get('containerType'),
                volumeReceived: formData.get('volumeReceived') ? parseFloat(formData.get('volumeReceived')) : null
            };

            const response = await fetch(`/sample-collection/collect/${testId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(sampleData)
            });

            if (!response.ok) {
                throw new Error('Failed to collect sample');
            }

            this.showNotification('Sample collected successfully', 'success');
            this.closeSampleModal();
            this.loadCollectionSchedule();
            this.loadCollectionQueue();

        } catch (error) {
            console.error('Error collecting sample:', error);
            this.showNotification('Error collecting sample', 'error');
        }
    }

    closeSampleModal() {
        const modal = document.getElementById('sample-collection-modal');
        if (modal) {
            modal.remove();
        }
    }

    getSampleTypeForTest(testName) {
        if (!testName) return 'WHOLE_BLOOD';

        const sampleTypeMap = {
            'Complete Blood Count (CBC)': 'WHOLE_BLOOD',
            'Blood Sugar (Fasting)': 'SERUM',
            'Lipid Profile': 'SERUM',
            'Liver Function Test (LFT)': 'SERUM',
            'Kidney Function Test': 'SERUM',
            'Thyroid Function Test': 'SERUM',
            'Urine Analysis': 'RANDOM_URINE',
            'Stool Analysis': 'STOOL'
        };

        return sampleTypeMap[testName] || 'WHOLE_BLOOD';
    }
}

// Logout function
function logout() {
    if (confirm('Are you sure you want to logout?')) {
        window.location.href = '/logout';
    }
}

// Initialize the application
const phlebotomyApp = new PhlebotomyApp();
