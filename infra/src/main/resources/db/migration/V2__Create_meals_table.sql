-- 식단 테이블 생성
CREATE TABLE meals (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,
    dining_time VARCHAR(20) NOT NULL,
    place VARCHAR(100) NOT NULL,
    price VARCHAR(20) NOT NULL,
    kcal VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 식단 메뉴 아이템 테이블 생성 (정규화된 구조)
CREATE TABLE meal_menu_items (
    meal_id BIGINT NOT NULL,
    menu_item VARCHAR(200) NOT NULL,
    FOREIGN KEY (meal_id) REFERENCES meals(id) ON DELETE CASCADE
);

-- 날짜와 식사 시간 조합에 대한 인덱스 (조회 최적화)
CREATE INDEX idx_meals_date_dining_time ON meals(date, dining_time);

-- 날짜별 조회를 위한 인덱스
CREATE INDEX idx_meals_date ON meals(date);

-- 식당별 조회를 위한 인덱스
CREATE INDEX idx_meals_place ON meals(place);

-- 메뉴 아이템 조회를 위한 인덱스
CREATE INDEX idx_meal_menu_items_meal_id ON meal_menu_items(meal_id);

-- 테이블 코멘트
COMMENT ON TABLE meals IS '식단 정보를 저장하는 테이블';
COMMENT ON COLUMN meals.id IS '식단 고유 ID';
COMMENT ON COLUMN meals.date IS '식단 날짜';
COMMENT ON COLUMN meals.dining_time IS '식사 시간 (BREAKFAST, LUNCH, DINNER)';
COMMENT ON COLUMN meals.place IS '식당 이름';
COMMENT ON COLUMN meals.price IS '가격 (문자열 형식)';
COMMENT ON COLUMN meals.kcal IS '칼로리 (문자열 형식)';
COMMENT ON COLUMN meals.created_at IS '식단 등록 일시';
COMMENT ON COLUMN meals.updated_at IS '식단 정보 수정 일시';

COMMENT ON TABLE meal_menu_items IS '식단 메뉴 아이템을 저장하는 테이블';
COMMENT ON COLUMN meal_menu_items.meal_id IS '식단 ID (외래키)';
COMMENT ON COLUMN meal_menu_items.menu_item IS '메뉴 항목명';

-- 식사 시간 제약 조건 (BREAKFAST, LUNCH, DINNER 만 허용)
ALTER TABLE meals ADD CONSTRAINT chk_dining_time
    CHECK (dining_time IN ('BREAKFAST', 'LUNCH', 'DINNER'));
