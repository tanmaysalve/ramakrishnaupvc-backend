CREATE TABLE IF NOT EXISTS app_users (
  id VARCHAR(36) PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  display_name VARCHAR(150) NOT NULL,
  role VARCHAR(30) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS customers (
  id VARCHAR(36) PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  company_name VARCHAR(200),
  customer_type VARCHAR(40) NOT NULL,
  phone VARCHAR(50) NOT NULL,
  alternate_phone VARCHAR(50),
  email VARCHAR(200),
  address VARCHAR(500),
  city VARCHAR(100),
  state VARCHAR(100),
  pincode VARCHAR(20),
  created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS sites (
  id VARCHAR(36) PRIMARY KEY,
  customer_id VARCHAR(36) NOT NULL REFERENCES customers(id),
  name VARCHAR(200) NOT NULL,
  address VARCHAR(500),
  city VARCHAR(100),
  state VARCHAR(100),
  pincode VARCHAR(20),
  contact_person VARCHAR(150),
  contact_phone VARCHAR(50),
  notes VARCHAR(1000),
  created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_sites_customer ON sites(customer_id);

CREATE SEQUENCE IF NOT EXISTS request_number_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS requests (
  id VARCHAR(36) PRIMARY KEY,
  request_number VARCHAR(30) NOT NULL UNIQUE,
  customer_id VARCHAR(36) NOT NULL REFERENCES customers(id),
  site_id VARCHAR(36) REFERENCES sites(id),
  created_by VARCHAR(36) NOT NULL REFERENCES app_users(id),
  assigned_to VARCHAR(36) REFERENCES app_users(id),
  source VARCHAR(40) NOT NULL,
  request_type VARCHAR(50) NOT NULL,
  product_category VARCHAR(50),
  description TEXT NOT NULL,
  quantity INTEGER,
  priority VARCHAR(20) NOT NULL,
  status VARCHAR(30) NOT NULL,
  expected_date DATE,
  remarks TEXT,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_requests_created_by ON requests(created_by);
CREATE INDEX IF NOT EXISTS idx_requests_assigned_to ON requests(assigned_to);
CREATE INDEX IF NOT EXISTS idx_requests_status ON requests(status);
CREATE INDEX IF NOT EXISTS idx_requests_updated_at ON requests(updated_at);

CREATE TABLE IF NOT EXISTS request_activities (
  id VARCHAR(36) PRIMARY KEY,
  request_id VARCHAR(36) NOT NULL REFERENCES requests(id) ON DELETE CASCADE,
  user_id VARCHAR(36) NOT NULL REFERENCES app_users(id),
  activity_type VARCHAR(40) NOT NULL,
  description TEXT NOT NULL,
  old_value VARCHAR(500),
  new_value VARCHAR(500),
  document_id VARCHAR(36),
  created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_activities_request ON request_activities(request_id, created_at DESC);

CREATE TABLE IF NOT EXISTS documents (
  id VARCHAR(36) PRIMARY KEY,
  request_id VARCHAR(36) NOT NULL REFERENCES requests(id) ON DELETE CASCADE,
  file_name VARCHAR(255) NOT NULL,
  stored_file_name VARCHAR(255) NOT NULL,
  content_type VARCHAR(150),
  size_bytes BIGINT NOT NULL,
  storage_path VARCHAR(1000) NOT NULL,
  uploaded_by VARCHAR(36) NOT NULL REFERENCES app_users(id),
  uploaded_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_documents_request ON documents(request_id);
