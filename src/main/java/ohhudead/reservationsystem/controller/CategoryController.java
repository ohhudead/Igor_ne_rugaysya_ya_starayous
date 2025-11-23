package ohhudead.reservationsystem.controller;

import lombok.RequiredArgsConstructor;
import ohhudead.reservationsystem.entity.Category;
import ohhudead.reservationsystem.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// TODO [PHASE 5]: Переработать контроллер для использования DTO вместо Entity
// ПРОБЛЕМЫ текущей реализации:
// 1. Возвращаем Entity напрямую - это плохая практика!
//    - Утечка внутренней структуры БД наружу
//    - Клиент API видит все поля (в т.ч. служебные)
//    - Сложно добавить вычисляемые поля
//    - Риск циклических зависимостей при сериализации связей
//
// 2. Нет валидации входящих данных
//    - Можно отправить пустое имя категории
//    - Можно отправить очень длинное имя
//    - Нет проверки на уникальность
//
// 3. Нет обработки ошибок
//    - При ошибке клиент получает stack trace или generic 500
//    - Нет структурированных сообщений об ошибках
//
// 4. Неправильные HTTP статусы
//    - POST должен возвращать 201 CREATED, а не 200 OK
//    - DELETE должен возвращать 204 NO_CONTENT
//
// ЧТО НУЖНО СДЕЛАТЬ:
// 1. Создать CategoryRequestDto с валидацией (@NotBlank, @Size и т.д.)
// 2. Создать CategoryResponseDto для ответов
// 3. Использовать @Valid для активации валидации
// 4. Вернуть ResponseEntity с правильными статусами
// 5. Убрать импорт Category entity из контроллера!
//
// Читать: TECHNICAL_REQUIREMENTS.md раздел 4 и 7, CODE_EXAMPLES_AND_PATTERNS.md раздел 3 и 4
// После изучения применить тот же подход ко всем остальным контроллерам

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<Category> getAll() {
        return categoryService.getAll();
    }

    @PostMapping
    public Category create(@RequestBody Category category) {
        return categoryService.create(category);
    }

    @GetMapping("/{id}")
    public Category getById(@PathVariable Long id) {
        return categoryService.getById(id);
    }

    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @RequestBody Category category) {
        return categoryService.update(id, category);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }

    // TODO [PHASE 5]: Добавить дополнительные эндпоинты:
    // GET /api/categories/{id}/products - получить все товары в категории
    // GET /api/categories/search?name=... - поиск категорий по имени

}