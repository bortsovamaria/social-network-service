CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(100) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    second_name VARCHAR(100) NOT NULL,
    birthdate DATE,
    biography TEXT,
    city VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (id, first_name, second_name, birthdate, biography, city) VALUES
('9649643f-55cf-4084-a5f5-8c3523a5a0ca', 'Иван', 'Иванов', '1990-01-15', 'Люблю путешествия и программирование', 'Москва'),
('082f909d-ef13-423c-b9d7-b2b0b6ba2a74', 'Петр', 'Петров', '1985-05-20', 'Увлекаюсь спортом и чтением', 'Санкт-Петербург'),
('afcc48b2-8b80-4257-9471-d4b826a159d7', 'Мария', 'Сидорова', '1992-08-10', 'Фотограф, художник', 'Казань');