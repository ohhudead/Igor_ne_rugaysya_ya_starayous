package ohhudead.reservationsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ohhudead.reservationsystem.entity.Category;
import ohhudead.reservationsystem.exception.CategoryDeleteException;
import ohhudead.reservationsystem.exception.ResourceNotFoundException;
import ohhudead.reservationsystem.repository.CategoryRepository;
import ohhudead.reservationsystem.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

// TODO [PHASE 4]: Добавить транзакционность и улучшить обработку ошибок
// ПРОБЛЕМЫ текущей реализации:
// 1. Нет @Transactional на методах изменения данных
//    - При ошибке в середине операции изменения не откатятся
//    - Нет гарантии консистентности данных
//
// 2. Generic RuntimeException вместо кастомных исключений
//    - Сложно обрабатывать разные типы ошибок
//    - Нет структурированных сообщений об ошибках
//
// 3. Нет проверки бизнес-правил
//    - Можно удалить категорию с товарами?
//    - Можно создать дубликат?
//
// ЧТО НУЖНО СДЕЛАТЬ:
// 1. Создать кастомные исключения (ResourceNotFoundException и т.д.)
// 2. Добавить @Transactional на create/update/delete
// 3. Добавить проверку: нельзя удалить категорию, если есть товары
// 4. Использовать @Transactional(readOnly = true) для getAll/getById
//
// ЭКСПЕРИМЕНТ для понимания @Transactional:
// Создай метод без @Transactional, который:
// 1. Сохраняет категорию
// 2. Затем выбрасывает исключение
// Проверь, останется ли категория в БД (спойлер: да, и это плохо!)
// Затем добавь @Transactional и убедись в rollback
//
// Читать: TECHNICAL_REQUIREMENTS.md раздел 6, CODE_EXAMPLES_AND_PATTERNS.md раздел 5

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;


    public List<Category> getAll() {
        log.info("Getting all categories");
        return categoryRepository.findAll();
    }


    public Category getById(Long id) {
        log.info("Get category by id={}", id);
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    @Transactional
    public Category create(Category category) {
        log.info("Creating category with name={}", category.getName());
        return categoryRepository.save(category);

    }


    @Transactional
    public Category update(Long id, Category request) {
        log.info("Update category id{}, request={}", id, request);

        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));


                existing.setName(request.getName());
                return categoryRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Delete category id={}", id);

        if (!categoryRepository.existsById(id)){
            throw new ResourceNotFoundException("Category", id);
        }
        if (productRepository.existByCategoryId(id)) {
            throw new CategoryDeleteException(id);
        }
        categoryRepository.deleteById(id);
    }

    // TODO [PHASE 4]: Реализовать сложную бизнес-логику для других сервисов
    // Для ProductService:
    // - Управление остатками на складе (увеличение/уменьшение с проверками)
    // - Поиск товаров с фильтрацией (цена от/до, категория, в наличии)
    // - Получение популярных товаров (по количеству продаж)
    //
    // Для OrderService (самый сложный!):
    // - Создание заказа (проверка остатков, уменьшение stock, создание order_items)
    // - Изменение статуса заказа (с валидацией допустимых переходов)
    // - Отмена заказа (возврат товара на склад)
    // - Расчет общей суммы заказа
    //
    // Для CustomerService:
    // - Регистрация клиента с проверкой уникальности email
    // - Получение истории заказов клиента
    // - Поиск VIP-клиентов (общая сумма заказов > порога)
}