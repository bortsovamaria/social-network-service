#!/bin/bash
set -e

echo "⚡ Generate users, create index..."

docker exec pgmaster bash -c "
 psql -U postgres << 'SQL'
 \c social_network;
 CREATE TABLE IF NOT EXISTS users (
     id VARCHAR(255) PRIMARY KEY,
     first_name VARCHAR(50) NOT NULL,
     last_name VARCHAR(50) NOT NULL,
     email VARCHAR(250) UNIQUE NOT NULL,
     birthdate TIMESTAMP,
     biography TEXT,
     city VARCHAR(100),
     password VARCHAR(255) NOT NULL,
     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
 );
INSERT INTO "users" (id, first_name, last_name, email, birthdate, biography, city, password, created_at)
SELECT
    gen_random_uuid(),

    -- Имя с реалистичным распределением
    (ARRAY[
        'Александр','Сергей','Дмитрий','Андрей','Алексей','Максим','Евгений','Михаил','Иван','Роман',
        'Владимир','Игорь','Павел','Николай','Константин','Олег','Артем','Юрий','Василий','Виктор',
        'Елена','Наталья','Ольга','Ирина','Татьяна','Мария','Светлана','Анна','Екатерина','Юлия',
        'Анастасия','Дарья','Виктория','Александра','Марина','Полина','Ксения','Валентина','Людмила','Галина'
    ])[floor(random() * 40 + 1)],

    -- Фамилия (учет пола)
    CASE
        WHEN random() < 0.5 THEN  -- Мужские фамилии
            (ARRAY[
                'Иванов','Смирнов','Кузнецов','Попов','Васильев','Петров','Соколов','Михайлов','Новиков','Федоров',
                'Морозов','Волков','Алексеев','Лебедев','Семенов','Егоров','Павлов','Козлов','Степанов','Николаев',
                'Орлов','Андреев','Макаров','Никитин','Захаров'
            ])[floor(random() * 25 + 1)]
        ELSE  -- Женские фамилии
            (ARRAY[
                'Иванова','Смирнова','Кузнецова','Попова','Васильева','Петрова','Соколова','Михайлова','Новикова','Федорова',
                'Морозова','Волкова','Алексеева','Лебедева','Семенова'
            ])[floor(random() * 15 + 1)]
    END,

    -- Email
    'user' || i || '_' || substr(md5(random()::text), 1, 6) || '@' ||
    (ARRAY['gmail.com','yandex.ru','mail.ru','example.com'])[floor(random() * 4 + 1)],

    -- Дата рождения (20-60 лет)
    DATE '1970-01-01' + (random() * 14600)::integer,

    -- Биография
    CASE
        WHEN random() < 0.3 THEN NULL
        WHEN random() < 0.8 THEN 'Краткая биография.'
        ELSE 'Длинная биография с деталями.'
    END,

    -- Город
    (ARRAY[
        'Москва','Санкт-Петербург','Новосибирск','Екатеринбург','Казань',
        'Нижний Новгород','Челябинск','Самара','Омск','Ростов-на-Дону',
        'Уфа','Красноярск','Пермь','Воронеж','Волгоград','Краснодар',
        'Саратов','Тюмень','Тольятти','Ижевск','Барнаул','Ульяновск',
        'Иркутск','Хабаровск','Ярославль','Владивосток','Махачкала',
        'Томск','Оренбург','Кемерово'
    ])[floor(random() * 30 + 1)],

    -- Пароль
    '$2a$10$kkIQgNSUMooZ9HVqQf0H7u1J03ieGS8j2p7hVscvtlsMFv.OP/oOu',

-- Дата создания (последние 5 лет)
CURRENT_TIMESTAMP - (random() * 1825 || ' days')::interval
FROM generate_series(1, 1000000) i;

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_gin_trgm_names ON users USING gin (
    first_name gin_trgm_ops,
    last_name gin_trgm_ops
);
SQL
"

echo "✅ Data added"