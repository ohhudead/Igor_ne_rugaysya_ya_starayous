package ohhudead.reservationsystem.repository;

import ohhudead.reservationsystem.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

// TODO [PHASE 1]: Изучить возможности Spring Data JPA
// JpaRepository уже предоставляет из коробки:
// - save(entity) - создание/обновление
// - findById(id) - поиск по ID
// - findAll() - получить все записи
// - deleteById(id) - удаление по ID
// - existsById(id) - проверка существования
// - count() - подсчет записей
//
// TODO [PHASE 8]: Добавить кастомные методы используя naming conventions
// Примеры методов для CategoryRepository:
// - Optional<Category> findByName(String name) - поиск по имени
// - boolean existsByName(String name) - проверка существования по имени
// - List<Category> findByNameContainingIgnoreCase(String name) - поиск по части имени
//
// TODO [PHASE 8]: Для других репозиториев добавить @Query с JPQL или нативным SQL
// Примеры сложных запросов для ProductRepository:
// 1. Найти топ товаров по продажам с JOIN к order_items
// 2. Найти товары с остатком меньше порога
// 3. Найти товары по категории и диапазону цен
//
// Примеры для OrderRepository:
// 1. Найти заказы клиента за период с фильтром по статусу
// 2. Рассчитать общую сумму заказов клиента
// 3. Найти просроченные заказы (status=pending и дата > 24 часа назад)
//
// Читать: TECHNICAL_REQUIREMENTS.md раздел 3.2, CODE_EXAMPLES_AND_PATTERNS.md раздел 2
// Изучить: Query methods, @Query annotation, Projections

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

}
