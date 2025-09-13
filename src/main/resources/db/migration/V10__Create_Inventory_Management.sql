-- Create Inventory Management Tables
-- Migration V10: Inventory Items and Transactions

-- Create inventory_items table
CREATE TABLE inventory_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sku VARCHAR(100) NOT NULL UNIQUE,
    barcode VARCHAR(255) UNIQUE,
    category VARCHAR(50) NOT NULL,
    unit_of_measurement VARCHAR(50) NOT NULL,
    current_stock INTEGER NOT NULL DEFAULT 0,
    minimum_stock_level INTEGER NOT NULL DEFAULT 0,
    maximum_stock_level INTEGER NOT NULL DEFAULT 100,
    reorder_point INTEGER,
    unit_cost DECIMAL(10,2),
    supplier VARCHAR(255),
    supplier_catalog_number VARCHAR(255),
    storage_location VARCHAR(255),
    storage_conditions VARCHAR(255),
    expiry_date TIMESTAMP,
    lot_number VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create inventory_transactions table
CREATE TABLE inventory_transactions (
    id BIGSERIAL PRIMARY KEY,
    inventory_item_id BIGINT NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_cost DECIMAL(10,2),
    total_cost DECIMAL(10,2),
    reference_number VARCHAR(255),
    supplier VARCHAR(255),
    lot_number VARCHAR(255),
    expiry_date TIMESTAMP,
    performed_by VARCHAR(255),
    reason VARCHAR(255),
    notes TEXT,
    stock_before INTEGER,
    stock_after INTEGER,
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_inventory_items_sku ON inventory_items(sku);
CREATE INDEX idx_inventory_items_barcode ON inventory_items(barcode);
CREATE INDEX idx_inventory_items_category ON inventory_items(category);
CREATE INDEX idx_inventory_items_status ON inventory_items(status);
CREATE INDEX idx_inventory_items_supplier ON inventory_items(supplier);
CREATE INDEX idx_inventory_items_name ON inventory_items(name);
CREATE INDEX idx_inventory_items_current_stock ON inventory_items(current_stock);
CREATE INDEX idx_inventory_items_minimum_stock ON inventory_items(minimum_stock_level);
CREATE INDEX idx_inventory_items_expiry_date ON inventory_items(expiry_date);
CREATE INDEX idx_inventory_items_lot_number ON inventory_items(lot_number);
CREATE INDEX idx_inventory_items_storage_location ON inventory_items(storage_location);

CREATE INDEX idx_inventory_transactions_item_id ON inventory_transactions(inventory_item_id);
CREATE INDEX idx_inventory_transactions_type ON inventory_transactions(transaction_type);
CREATE INDEX idx_inventory_transactions_date ON inventory_transactions(transaction_date);
CREATE INDEX idx_inventory_transactions_performed_by ON inventory_transactions(performed_by);
CREATE INDEX idx_inventory_transactions_supplier ON inventory_transactions(supplier);
CREATE INDEX idx_inventory_transactions_lot_number ON inventory_transactions(lot_number);
CREATE INDEX idx_inventory_transactions_reference ON inventory_transactions(reference_number);

-- Add constraints
ALTER TABLE inventory_items ADD CONSTRAINT chk_inventory_category 
    CHECK (category IN ('REAGENTS', 'BUFFERS', 'STAINS', 'STANDARDS', 'CALIBRATORS',
                        'TUBES', 'PIPETTE_TIPS', 'PLATES', 'SLIDES', 'FILTERS', 'SYRINGES',
                        'COLLECTION_TUBES', 'SWABS', 'CONTAINERS', 'TRANSPORT_MEDIA',
                        'GLOVES', 'MASKS', 'GOWNS', 'EYEWEAR',
                        'CLEANING_SUPPLIES', 'MAINTENANCE_PARTS',
                        'LABELS', 'FORMS', 'STATIONERY',
                        'QC_MATERIALS', 'PROFICIENCY_TESTING', 'OTHER'));

ALTER TABLE inventory_items ADD CONSTRAINT chk_inventory_status 
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'EXPIRED', 'RECALLED', 'QUARANTINED', 'DISCONTINUED', 'DAMAGED'));

ALTER TABLE inventory_items ADD CONSTRAINT chk_stock_levels 
    CHECK (current_stock >= 0 AND minimum_stock_level >= 0 AND maximum_stock_level >= minimum_stock_level);

ALTER TABLE inventory_transactions ADD CONSTRAINT chk_transaction_type 
    CHECK (transaction_type IN ('STOCK_IN', 'STOCK_OUT', 'ADJUSTMENT', 'TRANSFER', 'RETURN', 'DISPOSAL', 'CONSUMPTION', 'LOSS'));

-- Add trigger to update updated_at timestamp
CREATE TRIGGER update_inventory_items_updated_at BEFORE UPDATE ON inventory_items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample inventory data
INSERT INTO inventory_items (name, description, sku, category, unit_of_measurement, current_stock, minimum_stock_level, maximum_stock_level, unit_cost, supplier) VALUES
-- Reagents
('Glucose Reagent', 'Glucose oxidase reagent for glucose testing', 'REG-GLU-001', 'REAGENTS', 'mL', 500, 100, 1000, 0.50, 'Roche Diagnostics'),
('Cholesterol Reagent', 'Enzymatic cholesterol reagent', 'REG-CHOL-001', 'REAGENTS', 'mL', 300, 50, 500, 0.75, 'Abbott Laboratories'),
('Hemoglobin Reagent', 'Cyanmethemoglobin reagent', 'REG-HGB-001', 'REAGENTS', 'mL', 200, 50, 400, 1.20, 'Beckman Coulter'),

-- Consumables
('EDTA Tubes', 'K2 EDTA blood collection tubes 3mL', 'TUBE-EDTA-3ML', 'COLLECTION_TUBES', 'pieces', 1000, 200, 2000, 0.25, 'BD Vacutainer'),
('Serum Tubes', 'Serum separator tubes 5mL', 'TUBE-SST-5ML', 'COLLECTION_TUBES', 'pieces', 800, 150, 1500, 0.30, 'BD Vacutainer'),
('Pipette Tips 200µL', 'Sterile pipette tips 200µL', 'TIP-200UL', 'PIPETTE_TIPS', 'pieces', 5000, 1000, 10000, 0.02, 'Eppendorf'),
('Microplates 96-well', '96-well microplates for ELISA', 'PLATE-96W', 'PLATES', 'pieces', 100, 20, 200, 2.50, 'Corning'),

-- Safety Equipment
('Nitrile Gloves M', 'Powder-free nitrile gloves Medium', 'GLOVE-NIT-M', 'GLOVES', 'pieces', 2000, 500, 5000, 0.08, 'Ansell'),
('Lab Coats L', 'Disposable lab coats Large', 'COAT-DISP-L', 'GOWNS', 'pieces', 50, 10, 100, 3.50, 'DuPont'),

-- Standards and Controls
('Glucose Control Normal', 'Normal glucose control level', 'CTRL-GLU-N', 'STANDARDS', 'vials', 20, 5, 50, 15.00, 'Bio-Rad'),
('Cholesterol Control High', 'High cholesterol control level', 'CTRL-CHOL-H', 'STANDARDS', 'vials', 15, 3, 30, 18.50, 'Bio-Rad'),

-- Cleaning Supplies
('Disinfectant', 'Laboratory surface disinfectant', 'CLEAN-DISF-001', 'CLEANING_SUPPLIES', 'L', 10, 2, 20, 12.00, 'Ecolab'),
('Lens Cleaning Solution', 'Microscope lens cleaning solution', 'CLEAN-LENS-001', 'CLEANING_SUPPLIES', 'mL', 500, 100, 1000, 0.15, 'Zeiss');

-- Insert sample transaction data
INSERT INTO inventory_transactions (inventory_item_id, transaction_type, quantity, unit_cost, total_cost, supplier, performed_by, stock_before, stock_after, reason) VALUES
-- Stock in transactions
(1, 'STOCK_IN', 500, 0.50, 250.00, 'Roche Diagnostics', 'John Smith', 0, 500, 'Initial stock'),
(2, 'STOCK_IN', 300, 0.75, 225.00, 'Abbott Laboratories', 'Jane Doe', 0, 300, 'Initial stock'),
(3, 'STOCK_IN', 200, 1.20, 240.00, 'Beckman Coulter', 'Mike Johnson', 0, 200, 'Initial stock'),
(4, 'STOCK_IN', 1000, 0.25, 250.00, 'BD Vacutainer', 'Sarah Wilson', 0, 1000, 'Initial stock'),
(5, 'STOCK_IN', 800, 0.30, 240.00, 'BD Vacutainer', 'Sarah Wilson', 0, 800, 'Initial stock'),

-- Stock out transactions (consumption)
(1, 'CONSUMPTION', 50, NULL, NULL, NULL, 'Lab Tech 1', 500, 450, 'Used for glucose testing'),
(2, 'CONSUMPTION', 25, NULL, NULL, NULL, 'Lab Tech 2', 300, 275, 'Used for cholesterol testing'),
(4, 'CONSUMPTION', 100, NULL, NULL, NULL, 'Phlebotomist', 1000, 900, 'Blood collection'),

-- Adjustments
(6, 'STOCK_IN', 5000, 0.02, 100.00, 'Eppendorf', 'Inventory Manager', 0, 5000, 'Initial stock'),
(6, 'ADJUSTMENT', -50, NULL, NULL, NULL, 'Inventory Manager', 5000, 4950, 'Damaged tips removed');

-- Update current stock to match transaction history
UPDATE inventory_items SET current_stock = 450 WHERE id = 1;
UPDATE inventory_items SET current_stock = 275 WHERE id = 2;
UPDATE inventory_items SET current_stock = 900 WHERE id = 4;
UPDATE inventory_items SET current_stock = 4950 WHERE id = 6;
