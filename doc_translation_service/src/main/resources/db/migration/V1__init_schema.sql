-- V1: Initial schema for GitHub Doc Translation MVP

CREATE TABLE IF NOT EXISTS users
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    github_id
    BIGINT
    NOT
    NULL
    UNIQUE,
    username
    VARCHAR
(
    100
) NOT NULL,
    avatar_url VARCHAR
(
    500
),
    email VARCHAR
(
    200
),
    github_access_token TEXT,
    openrouter_api_key TEXT,
    free_quota BIGINT NOT NULL DEFAULT 100000,
    used_quota BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW
(
),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW
(
)
    );

CREATE INDEX IF NOT EXISTS idx_users_github_id ON users(github_id);

CREATE TABLE IF NOT EXISTS repositories
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    user_id
    BIGINT
    NOT
    NULL
    REFERENCES
    users
(
    id
) ON DELETE CASCADE,
    github_repo_id BIGINT NOT NULL,
    full_name VARCHAR
(
    300
) NOT NULL,
    default_branch VARCHAR
(
    100
) NOT NULL DEFAULT 'main',
    installation_id BIGINT,
    base_language VARCHAR
(
    10
) NOT NULL DEFAULT 'zh',
    target_languages JSONB NOT NULL DEFAULT '[]',
    ai_model VARCHAR
(
    200
),
    status VARCHAR
(
    20
) NOT NULL DEFAULT 'active',
    last_sync_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW
(
),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW
(
),
    description VARCHAR
(
    500
),
    UNIQUE
(
    user_id,
    github_repo_id
)
    );

CREATE INDEX IF NOT EXISTS idx_repositories_user_id ON repositories(user_id);
CREATE INDEX IF NOT EXISTS idx_repositories_full_name ON repositories(full_name);
CREATE INDEX IF NOT EXISTS idx_repositories_installation_id ON repositories(installation_id);

CREATE TABLE IF NOT EXISTS translation_tasks
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    repository_id
    BIGINT
    NOT
    NULL
    REFERENCES
    repositories
(
    id
) ON DELETE CASCADE,
    trigger_type VARCHAR
(
    20
) NOT NULL,
    trigger_commit_sha VARCHAR
(
    64
),
    status VARCHAR
(
    20
) NOT NULL DEFAULT 'queued',
    target_languages JSONB NOT NULL DEFAULT '[]',
    total_files INT NOT NULL DEFAULT 0,
    completed_files INT NOT NULL DEFAULT 0,
    failed_files INT NOT NULL DEFAULT 0,
    tokens_used BIGINT NOT NULL DEFAULT 0,
    estimated_cost DECIMAL
(
    10,
    6
) DEFAULT 0,
    pr_number INT,
    pr_url VARCHAR
(
    500
),
    pr_branch VARCHAR
(
    200
),
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW
(
),
    before_commit_sha VARCHAR
(
    64
),
    pr_status VARCHAR
(
    20
)
    );

CREATE INDEX IF NOT EXISTS idx_tasks_repository_id ON translation_tasks(repository_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status ON translation_tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_created_at ON translation_tasks(created_at);

CREATE TABLE IF NOT EXISTS translation_files
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    task_id
    BIGINT
    NOT
    NULL
    REFERENCES
    translation_tasks
(
    id
) ON DELETE CASCADE,
    repository_id BIGINT NOT NULL REFERENCES repositories
(
    id
)
  ON DELETE CASCADE,
    source_path VARCHAR
(
    500
) NOT NULL,
    target_language VARCHAR
(
    10
) NOT NULL,
    target_path VARCHAR
(
    500
) NOT NULL,
    status VARCHAR
(
    20
) NOT NULL DEFAULT 'pending',
    source_hash VARCHAR
(
    64
),
    tokens_used BIGINT NOT NULL DEFAULT 0,
    error_message TEXT,
    translated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW
(
),
    patch TEXT
    );

CREATE INDEX IF NOT EXISTS idx_files_task_id ON translation_files(task_id);
CREATE INDEX IF NOT EXISTS idx_files_repository_id ON translation_files(repository_id);
CREATE INDEX IF NOT EXISTS idx_files_source_hash ON translation_files(source_hash);

CREATE TABLE IF NOT EXISTS operation_logs
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    user_id
    BIGINT
    REFERENCES
    users
(
    id
) ON DELETE SET NULL,
    repository_id BIGINT REFERENCES repositories
(
    id
)
  ON DELETE SET NULL,
    action VARCHAR
(
    100
) NOT NULL,
    detail TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW
(
)
    );

CREATE INDEX IF NOT EXISTS idx_logs_user_id ON operation_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_logs_repository_id ON operation_logs(repository_id);
CREATE INDEX IF NOT EXISTS idx_logs_created_at ON operation_logs(created_at);

CREATE TABLE IF NOT EXISTS repository_branches
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    repository_id
    BIGINT
    NOT
    NULL
    REFERENCES
    repositories
(
    id
) ON DELETE CASCADE,
    branch_name VARCHAR
(
    200
) NOT NULL,
    selected_paths JSONB NOT NULL DEFAULT '[]',
    ignore_patterns JSONB NOT NULL DEFAULT '[]',
    output_path_pattern VARCHAR
(
    200
) NOT NULL DEFAULT 'translations/{lang}/',
    webhook_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW
(
),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW
(
),
    auto_merge_pr BOOLEAN NOT NULL DEFAULT false,
    UNIQUE
(
    repository_id,
    branch_name
)
    );

CREATE INDEX IF NOT EXISTS idx_branches_repository_id ON repository_branches(repository_id);
CREATE INDEX IF NOT EXISTS idx_branches_repo_branch ON repository_branches(repository_id, branch_name);

ALTER TABLE translation_tasks
    ADD COLUMN IF NOT EXISTS branch_name VARCHAR (200);
