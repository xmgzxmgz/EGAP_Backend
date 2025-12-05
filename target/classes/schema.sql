CREATE TABLE IF NOT EXISTS tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(1024),
    val INT
);

CREATE TABLE IF NOT EXISTS enterprise_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    region VARCHAR(255) NOT NULL,
    risk DOUBLE NOT NULL
);

CREATE TABLE IF NOT EXISTS enterprise_tags (
    enterprise_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP,
    PRIMARY KEY (enterprise_id, tag_id),
    CONSTRAINT fk_et_enterprise FOREIGN KEY (enterprise_id) REFERENCES enterprise_info(id) ON DELETE CASCADE,
    CONSTRAINT fk_et_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tuning_models (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    creator VARCHAR(255),
    created_at TIMESTAMP,
    status VARCHAR(64),
    meta TEXT
);
