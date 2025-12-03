package ohhudead.reservationsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ohhudead.reservationsystem.entity.Category;
import ohhudead.reservationsystem.dto.CategoryRequest;
import ohhudead.reservationsystem.dto.CategoryResponse;
import ohhudead.reservationsystem.mapper.CategoryMapper;
import ohhudead.reservationsystem.exception.CategoryAlreadyExistsException;
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

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;


    public List<CategoryResponse> getAll() {
        log.info("Getting all categories");
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }


    public CategoryResponse getById(Long id) {
        log.info("Get category by id={}", id);
        Category category = findById(id);
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        log.info("Creating category with name={}", request.getName());

        if (categoryRepository.existsByNameIgnoreCase(request.getName())){
            throw new CategoryAlreadyExistsException(request.getName());
        }
        Category category = categoryMapper.toEntity(request);
        category = categoryRepository.save(category);
        return categoryMapper.toResponse(category);

    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        log.info("Update category id{}, request={}", id, request);

        Category existing = findById(id);

        categoryMapper.updateFromRequest(request, existing);

        existing = categoryRepository.save(existing);
        return categoryMapper.toResponse(existing);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Delete category id={}", id);

        Category category = findById(id);

        Long productsCount = productRepository.countByCategoryId(id);
        if(productsCount > 0){
            throw new CategoryDeleteException(id);
        }

        categoryRepository.delete(category);
    }

    private Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
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