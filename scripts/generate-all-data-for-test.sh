#!/bin/bash

# Скрипт генерации тестовых данных для социальной сети

echo "=== Генерация тестовых данных для социальной сети ==="

# Запускаем генерацию данных внутри контейнера
docker exec pgmaster bash -c "
psql -U postgres -d social_network << 'SQL'

-- Включаем вывод уведомлений
\\set ECHO queries

-- ОТКЛЮЧАЕМ ИНДЕКСЫ И ТРИГГЕРЫ ДЛЯ УСКОРЕНИЯ
SELECT 'Отключение индексов и триггеров...' as status;
ALTER TABLE users DISABLE TRIGGER ALL;
ALTER TABLE posts DISABLE TRIGGER ALL;
ALTER TABLE friendship DISABLE TRIGGER ALL;

-- 1.
SELECT 'Создание 1 млн пользователей...' as status;
INSERT INTO \"users\" (id, first_name, last_name, email, birthdate, biography, city, password, created_at)
SELECT
    gen_random_uuid(),

    -- Имя с реалистичным распределением
    (ARRAY[
        'Александр','Сергей','Дмитрий','Андрей','Алексей','Максим','Евгений','Михаил','Иван','Роман',
        'Владимир','Игорь','Павел','Николай','Константин','Олег','Артем','Юрий','Василий','Виктор',
        'Елена','Наталья','Ольга','Ирина','Татьяна','Мария','Светлана','Анна','Екатерина','Юлия',
        'Анастасия','Дарья','Виктория','Александра','Марина','Полина','Ксения','Валентина','Людмила','Галина'
    ])[mod(i, 40) + 1],

    -- Фамилия (учет пола)
    CASE
        WHEN mod(i, 2) = 0 THEN  -- Мужские фамилии
            (ARRAY[
                'Иванов','Смирнов','Кузнецов','Попов','Васильев','Петров','Соколов','Михайлов','Новиков','Федоров',
                'Морозов','Волков','Алексеев','Лебедев','Семенов','Егоров','Павлов','Козлов','Степанов','Николаев',
                'Орлов','Андреев','Макаров','Никитин','Захаров'
            ])[mod(i, 25) + 1]
        ELSE  -- Женские фамилии
            (ARRAY[
                'Иванова','Смирнова','Кузнецова','Попова','Васильева','Петрова','Соколова','Михайлова','Новикова','Федорова',
                'Морозова','Волкова','Алексеева','Лебедева','Семенова'
            ])[mod(i, 15) + 1]
    END,

    -- Email
    'user' || i || '@test' || mod(i, 10) || '.com',

    -- Дата рождения (20-60 лет)
    DATE '1970-01-01' + (mod(i, 14600))::integer,

    -- Биография
    CASE
        WHEN mod(i, 10) < 3 THEN NULL
        WHEN mod(i, 10) < 8 THEN 'Краткая биография.'
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
    '\$2a\$10\$GN2/X.I.EjGH0f5P6cFUBul07xUnGDbe8pK6XZiDix3rY9mX0KpJm',

    -- Дата создания (последние 5 лет)
    CURRENT_TIMESTAMP - (mod(i, 1825) || ' days')::interval
    FROM generate_series(1, 1000000) i;

SELECT '✓ Создано 1 млн пользователей' as result;

-- Сначала проверим текущие данные
SELECT 'Текущая статистика:' as title;
SELECT
    (SELECT COUNT(*) FROM users) as users,
    (SELECT COUNT(*) FROM posts) as posts,
    (SELECT COUNT(*) FROM friendship) as friendships;

-- 2. Создаем тяжелых пользователей с >1000 постов
SELECT 'Создание тяжелых пользователей...' as status;

INSERT INTO users (id, first_name, last_name, email, birthdate, biography, city, password, created_at)
SELECT
    gen_random_uuid(),
    'Тяжелый',
    'Постер' || s,
    'heavy.poster' || s || '@example.com',
    DATE '1990-01-01',
    'Пользователь с большим количеством постов для тестирования лимита в 1000',
    'Москва',
    '\$2a\$10\$GN2/X.I.EjGH0f5P6cFUBul07xUnGDbe8pK6XZiDix3rY9mX0KpJm',
    CURRENT_TIMESTAMP
FROM generate_series(1, 3) s
RETURNING id, email;

-- Создаем посты для тяжелых пользователей
WITH heavy_users AS (
    SELECT id FROM users WHERE email LIKE 'heavy.poster%'
),
post_numbers AS (
    SELECT generate_series(1, 1500) as post_num
)
INSERT INTO posts (id, text, author_id, created_at)
SELECT
    gen_random_uuid(),
    'Пост #' || p.post_num || ' от тяжелого пользователя.',
    hu.id,
    CURRENT_TIMESTAMP - (p.post_num * '1 minute'::interval)
FROM heavy_users hu
CROSS JOIN post_numbers p;

SELECT '✓ Тяжелые пользователи созданы' as result;

-- 3. Создаем тестового пользователя
SELECT 'Создание тестового пользователя...' as status;

-- Создаем тестового пользователя и сразу используем его
WITH test_user AS (
    INSERT INTO users (id, first_name, last_name, email, birthdate, biography, city, password, created_at)
    VALUES (
        gen_random_uuid(),
        'Тест',
        'Юзер',
        'test.user@example.com',
        DATE '1995-01-01',
        'Тестовый пользователь для проверки кеширования ленты. Пароль: password',
        'Москва',
        '\$2a\$10\$GN2/X.I.EjGH0f5P6cFUBul07xUnGDbe8pK6XZiDix3rY9mX0KpJm',
        CURRENT_TIMESTAMP
    )
    RETURNING id
)
-- Добавляем друзей: ТОЛЬКО тяжелых пользователей
INSERT INTO friendship (user_id, friend_id)
SELECT tu.id, u.id
FROM test_user tu
CROSS JOIN users u
WHERE u.email LIKE 'heavy.poster%';

-- Создаем несколько постов для тестового пользователя
INSERT INTO posts (id, text, author_id, created_at)
SELECT
    gen_random_uuid(),
    'Тестовый пост #' || i,
    (SELECT id FROM test_user),
    CURRENT_TIMESTAMP - (i || ' hours')::interval
FROM generate_series(1, 10) i;

SELECT '✓ Тестовый пользователь создан' as result;

-- 4. Создаем несколько постов для обычных пользователей
SELECT 'Добавление минимальных постов для обычных пользователей...' as status;

INSERT INTO posts (id, text, author_id, created_at)
SELECT
    gen_random_uuid(),
    'Пост пользователя ' || i,
    (SELECT id FROM users WHERE email NOT LIKE 'heavy.poster%' AND email != 'test.user@example.com' LIMIT 1 OFFSET mod(i, 1000)),
    CURRENT_TIMESTAMP - (mod(i, 365) || ' days')::interval
FROM generate_series(1, 100) i;

SELECT '✓ Минимальные посты созданы' as result;

-- ВКЛЮЧАЕМ ИНДЕКСЫ И ТРИГГЕРЫ ОБРАТНО
SELECT 'Включение индексов и триггеров...' as status;
ALTER TABLE users ENABLE TRIGGER ALL;
ALTER TABLE posts ENABLE TRIGGER ALL;
ALTER TABLE friendship ENABLE TRIGGER ALL;

-- Собираем статистику для оптимизатора запросов
ANALYZE users;
ANALYZE posts;
ANALYZE friendship;

SELECT '✓ Индексы включены и статистика собрана' as result;

-- 5. Статистика после генерации
SELECT '=== ФИНАЛЬНАЯ СТАТИСТИКА ===' as title;

SELECT 'Общая статистика:' as section;
SELECT
    (SELECT COUNT(*) FROM users) as total_users,
    (SELECT COUNT(*) FROM posts) as total_posts,
    (SELECT COUNT(*) FROM friendship) as total_friendships;

SELECT 'Тяжелые пользователи (для теста лимита):' as section;
SELECT
    u.email,
    u.first_name || ' ' || u.last_name as name,
    COUNT(p.id) as post_count,
    (SELECT COUNT(*) FROM friendship WHERE friend_id = u.id) as followers_count
FROM users u
JOIN posts p ON u.id = p.author_id
WHERE u.email LIKE 'heavy.poster%'
GROUP BY u.id, u.email, u.first_name, u.last_name
ORDER BY post_count DESC;

SELECT 'Тестовый пользователь (для проверки):' as section;
SELECT
    u.email,
    u.first_name || ' ' || u.last_name as name,
    (SELECT COUNT(*) FROM friendship WHERE user_id = u.id) as friends_count,
    (SELECT COUNT(*) FROM posts WHERE author_id = u.id) as posts_count
FROM users u
WHERE u.email = 'test.user@example.com';

SELECT 'Проверка лимита в 1000 постов:' as section;
WITH friend_posts AS (
    SELECT COUNT(*) as total_friend_posts
    FROM posts p
    WHERE p.author_id IN (
        SELECT friend_id
        FROM friendship
        WHERE user_id = (SELECT id FROM users WHERE email = 'test.user@example.com')
    )
),
limited_posts AS (
    SELECT COUNT(*) as limited_to_1000
    FROM (
        SELECT p.id
        FROM posts p
        WHERE p.author_id IN (
            SELECT friend_id
            FROM friendship
            WHERE user_id = (SELECT id FROM users WHERE email = 'test.user@example.com')
        )
        ORDER BY p.created_at DESC
        LIMIT 1000
    ) t
)
SELECT
    fp.total_friend_posts as всего_постов_у_друзей,
    lp.limited_to_1000 as возвращается_в_ленте,
    CASE
        WHEN fp.total_friend_posts > 1000 THEN 'ДА (лимит работает ✓)'
        ELSE 'НЕТ (мало данных)'
    END as ограничение_1000_работает
FROM friend_posts fp, limited_posts lp;

SELECT '=== ГЕНЕРАЦИЯ ДАННЫХ ЗАВЕРШЕНА ===' as title;
SELECT 'Для тестирования используйте:' as instruction;
SELECT '1. Логин: test.user@example.com / password' as step;
SELECT '2. Проверьте что /post/feed возвращает не более 1000 постов' as step;
SELECT '3. У тяжелых пользователей суммарно 4500 постов, возвращается только 1000' as step;

SQL
"

echo ""
echo "=== Генерация данных завершена ==="
echo "Данные успешно сгенерированы в контейнере pgmaster"
echo ""
echo "Для проверки выполните:"
echo "1. Запустите приложение: docker-compose up app"
echo "2. Авторизуйтесь как test.user@example.com / password"
echo "3. Проверьте эндпоинт GET /v0/posts/feed"
echo "4. Убедитесь что возвращается не более 1000 постов"
echo "5. Проверьте что второй запрос быстрее (работает кеш)"