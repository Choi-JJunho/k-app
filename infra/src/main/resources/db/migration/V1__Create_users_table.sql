-- 사용자 테이블 생성
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    student_employee_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 이메일 조회 성능 향상을 위한 인덱스
CREATE INDEX idx_users_email ON users(email);

-- 학번/사번 조회를 위한 인덱스
CREATE INDEX idx_users_student_employee_id ON users(student_employee_id);

-- 테이블 코멘트
COMMENT ON TABLE users IS '사용자 정보를 저장하는 테이블';
COMMENT ON COLUMN users.id IS '사용자 고유 ID';
COMMENT ON COLUMN users.email IS '사용자 이메일 주소 (로그인 ID로 사용)';
COMMENT ON COLUMN users.password IS 'BCrypt로 해시된 비밀번호';
COMMENT ON COLUMN users.name IS '사용자 이름';
COMMENT ON COLUMN users.student_employee_id IS '학번 또는 사번';
COMMENT ON COLUMN users.created_at IS '계정 생성 일시';
COMMENT ON COLUMN users.updated_at IS '계정 정보 수정 일시';
