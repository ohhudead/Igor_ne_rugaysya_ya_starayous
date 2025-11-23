# Примеры кода и паттерны для изучения

Этот документ содержит примеры кода и паттерны, которые помогут понять ключевые концепции проекта. **Внимание:** это не готовые решения для ТЗ, а примеры для понимания!

## 1. JPA Entities и связи

### 1.1 OneToMany связь

```java
@Entity
@Table(name = "authors")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    // OneToMany - у автора может быть много книг
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Book> books = new ArrayList<>();
}

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    // ManyToOne - у книги один автор
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;
}
```

**На что обратить внимание:**
- `mappedBy` указывает на поле в дочерней сущности (Book), которое владеет связью
- `FetchType.LAZY` - связанные сущности загружаются только при обращении к ним
- `cascade = CascadeType.ALL` - операции над родителем (Author) каскадируются на детей (Books)

### 1.2 OneToOne связь

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile profile;
}

@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;
    
    private String bio;
    private String avatarUrl;
}
```

**На что обратить внимание:**
- `unique = true` в @JoinColumn обеспечивает уникальность связи
- Одна сторона владеет связью (та, где есть @JoinColumn)

### 1.3 Enum в JPA

```java
public enum OrderStatus {
    PENDING,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

@Entity
public class Order {
    @Enumerated(EnumType.STRING)  // Сохраняется как строка, а не число!
    @Column(nullable = false)
    private OrderStatus status;
}
```

**Почему STRING, а не ORDINAL:**
- ORDINAL сохраняет порядковый номер (0, 1, 2...), что ломается при изменении порядка enum
- STRING сохраняет название, что более надежно

## 2. Repository паттерны

### 2.1 Spring Data naming conventions

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Находит товары по имени категории (JOIN автоматически!)
    List<Product> findByCategoryName(String categoryName);
    
    // Находит товары с ценой в диапазоне и количеством больше указанного
    List<Product> findByPriceBetweenAndInStockGreaterThan(
        BigDecimal minPrice, 
        BigDecimal maxPrice, 
        Integer minStock
    );
    
    // Проверяет существование по имени (игнорируя регистр)
    boolean existsByNameIgnoreCase(String name);
}
```

**Магия Spring Data:**
- Парсит имя метода и генерирует запрос автоматически
- Поддерживает множество keywords: `findBy`, `existsBy`, `countBy`, `deleteBy`
- Модификаторы: `IgnoreCase`, `OrderBy`, `Top`, `First`

### 2.2 @Query с JPQL

```java
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // JPQL - работает с entities, а не с таблицами
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId " +
           "AND o.orderDate BETWEEN :startDate AND :endDate " +
           "AND o.status = :status")
    List<Order> findCustomerOrdersInPeriod(
        @Param("customerId") Long customerId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("status") OrderStatus status
    );
    
    // Проекция - выбираем только нужные поля
    @Query("SELECT new com.example.dto.OrderSummary(o.id, o.orderDate, o.status) " +
           "FROM Order o WHERE o.customer.id = :customerId")
    List<OrderSummary> findOrderSummaries(@Param("customerId") Long customerId);
}
```

**Преимущества JPQL:**
- Type-safe (проверка на этапе компиляции с QueryDSL)
- Работает с объектной моделью, а не с SQL таблицами
- Database-independent (переносимо между БД)

### 2.3 @Query с нативным SQL

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Нативный SQL для сложной агрегации
    @Query(value = "SELECT p.product_id, p.product_name, " +
                   "COALESCE(SUM(oi.quantity), 0) as total_sold " +
                   "FROM products p " +
                   "LEFT JOIN order_items oi ON p.product_id = oi.product_id " +
                   "WHERE p.category_id = :categoryId " +
                   "GROUP BY p.product_id, p.product_name " +
                   "ORDER BY total_sold DESC " +
                   "LIMIT :limit", 
           nativeQuery = true)
    List<Object[]> findTopSellingProducts(
        @Param("categoryId") Long categoryId, 
        @Param("limit") int limit
    );
}
```

**Когда использовать нативный SQL:**
- Сложные агрегации и GROUP BY
- Database-specific функции (например, PostgreSQL array functions)
- Оптимизация производительности критичных запросов

## 3. DTO и MapStruct

### 3.1 Request и Response DTO

```java
// Request - что приходит от клиента
public class ProductCreateRequest {
    private String name;
    private BigDecimal price;
    private Integer inStock;
    private Long categoryId;
}

// Response - что отправляем клиенту
public class ProductResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer inStock;
    private CategoryResponse category;  // Вложенный DTO!
    private LocalDateTime createdAt;
}

public class CategoryResponse {
    private Long id;
    private String name;
}
```

**Зачем разделять:**
- Request может содержать только изменяемые поля
- Response может включать вычисляемые поля, вложенные объекты
- Скрываем внутреннюю структуру Entity от клиентов API

### 3.2 MapStruct Mapper

```java
@Mapper(componentModel = "spring")  // Интеграция со Spring
public interface ProductMapper {
    
    // Простой маппинг
    ProductResponse toResponse(Product product);
    
    List<ProductResponse> toResponseList(List<Product> products);
    
    // Сложный маппинг с явным указанием полей
    @Mapping(target = "id", ignore = true)  // ID генерируется БД
    @Mapping(target = "category", source = "categoryId", qualifiedByName = "categoryIdToCategory")
    @Mapping(target = "createdAt", ignore = true)
    Product toEntity(ProductCreateRequest request);
    
    // Дополнительный метод для преобразования Category ID в Category entity
    @Named("categoryIdToCategory")
    default Category categoryIdToCategory(Long categoryId) {
        if (categoryId == null) return null;
        Category category = new Category();
        category.setId(categoryId);
        return category;
    }
}
```

**MapStruct генерирует:**
```java
// Примерно такой код генерируется автоматически
@Component
public class ProductMapperImpl implements ProductMapper {
    
    @Override
    public ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setPrice(product.getPrice());
        // ... и так далее
        return response;
    }
}
```

### 3.3 MapStruct с вложенными объектами

```java
@Mapper(componentModel = "spring", uses = {CategoryMapper.class, CustomerMapper.class})
public interface OrderMapper {
    
    // MapStruct автоматически использует CategoryMapper для маппинга category
    @Mapping(target = "items", source = "orderItems")
    @Mapping(target = "totalAmount", expression = "java(calculateTotal(order))")
    OrderResponse toResponse(Order order);
    
    // Кастомная логика через expression
    default BigDecimal calculateTotal(Order order) {
        return order.getOrderItems().stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

## 4. Валидация (Jakarta Bean Validation)

### 4.1 Стандартные аннотации

```java
public class ProductCreateRequest {
    
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 200, message = "Name must be between 3 and 200 characters")
    private String name;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    @Digits(integer = 8, fraction = 2, message = "Price must have max 2 decimal places")
    private BigDecimal price;
    
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer inStock;
    
    @NotNull(message = "Category is required")
    @Positive(message = "Category ID must be positive")
    private Long categoryId;
}
```

**Основные аннотации:**
- `@NotNull` - значение не может быть null
- `@NotEmpty` - коллекция/строка не пустая
- `@NotBlank` - строка не пустая и не состоит из пробелов
- `@Size` - размер строки/коллекции
- `@Min`, `@Max` - числовые границы
- `@Email` - валидация email
- `@Pattern` - регулярное выражение

### 4.2 Использование в контроллере

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
        // Если валидация не прошла, будет выброшен MethodArgumentNotValidException
        // @Valid активирует валидацию
        ProductResponse response = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    public ProductResponse update(
        @PathVariable @Positive Long id,  // Валидация path variable!
        @Valid @RequestBody ProductUpdateRequest request
    ) {
        return productService.update(id, request);
    }
}
```

### 4.3 Кастомная валидация

```java
// Аннотация
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CategoryExistsValidator.class)
public @interface CategoryExists {
    String message() default "Category does not exist";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// Валидатор
@Component
public class CategoryExistsValidator implements ConstraintValidator<CategoryExists, Long> {
    
    private final CategoryRepository categoryRepository;
    
    public CategoryExistsValidator(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    @Override
    public boolean isValid(Long categoryId, ConstraintValidatorContext context) {
        if (categoryId == null) return true;  // @NotNull проверит отдельно
        return categoryRepository.existsById(categoryId);
    }
}

// Использование
public class ProductCreateRequest {
    @NotNull
    @CategoryExists  // Наша кастомная валидация!
    private Long categoryId;
}
```

### 4.4 Валидация коллекций

```java
public class OrderCreateRequest {
    
    @NotNull
    private Long customerId;
    
    @NotEmpty(message = "Order must contain at least one item")
    @Size(min = 1, max = 100, message = "Order can contain 1-100 items")
    @Valid  // Важно! Валидирует каждый элемент списка
    private List<OrderItemRequest> items;
}

public class OrderItemRequest {
    
    @NotNull
    @Positive
    private Long productId;
    
    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 1000, message = "Quantity cannot exceed 1000")
    private Integer quantity;
}
```

## 5. Транзакционность (@Transactional)

### 5.1 Базовое использование

```java
@Service
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    
    // Метод выполняется в транзакции
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        // 1. Создаем заказ
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setStatus(OrderStatus.PENDING);
        order = orderRepository.save(order);
        
        // 2. Создаем позиции и уменьшаем остатки
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            
            // Проверяем наличие
            if (product.getInStock() < itemRequest.getQuantity()) {
                throw new InsufficientStockException("Not enough stock for " + product.getName());
            }
            
            // Уменьшаем остаток
            product.setInStock(product.getInStock() - itemRequest.getQuantity());
            productRepository.save(product);
            
            // Создаем позицию заказа
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(product.getPrice());
            order.getOrderItems().add(item);
        }
        
        // Если что-то пошло не так (exception), все откатится!
        return orderMapper.toResponse(order);
    }
    
    // Read-only операции
    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return orderMapper.toResponse(order);
    }
}
```

**На что обратить внимание:**
- `@Transactional` - все операции в одной транзакции
- При исключении происходит rollback всех изменений
- `readOnly = true` - оптимизация для операций чтения

### 5.2 Демонстрация важности транзакций

```java
// БЕЗ @Transactional - ПЛОХО!
public void createOrderWithoutTransaction(OrderCreateRequest request) {
    Order order = new Order();
    order.setCustomerId(request.getCustomerId());
    order = orderRepository.save(order);  // Сохранено в БД!
    
    // Если здесь произойдет ошибка...
    for (OrderItemRequest itemRequest : request.getItems()) {
        Product product = productRepository.findById(itemRequest.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        product.setInStock(product.getInStock() - itemRequest.getQuantity());
        productRepository.save(product);
    }
    
    // ...то order уже в БД, а items нет! Inconsistent state!
}

// С @Transactional - ХОРОШО!
@Transactional
public void createOrderWithTransaction(OrderCreateRequest request) {
    // Тот же код, но при ошибке все откатится
    // Либо все сохранится, либо ничего - атомарность!
}
```

### 5.3 Propagation и Isolation

```java
@Service
public class PaymentService {
    
    // REQUIRED (default) - использует текущую транзакцию или создает новую
    @Transactional(propagation = Propagation.REQUIRED)
    public void processPayment(Long orderId, PaymentRequest request) {
        // логика оплаты
    }
    
    // REQUIRES_NEW - всегда создает новую транзакцию
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logPaymentAttempt(Long orderId) {
        // Логируется даже если основная транзакция откатится
    }
    
    // Isolation level для предотвращения race conditions
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void transferStock(Long fromProductId, Long toProductId, int quantity) {
        // Гарантия консистентности при конкурентном доступе
    }
}
```

## 6. Exception Handling

### 6.1 Кастомные исключения

```java
// Базовое исключение
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}

// Специфичные исключения
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

public class InsufficientStockException extends BusinessException {
    private final Long productId;
    private final int requested;
    private final int available;
    
    public InsufficientStockException(Long productId, int requested, int available) {
        super(String.format("Insufficient stock for product %d: requested %d, available %d", 
                          productId, requested, available));
        this.productId = productId;
        this.requested = requested;
        this.available = available;
    }
    
    // getters
}
```

### 6.2 Global Exception Handler

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    // Обработка ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            WebRequest request) {
        
        log.error("Resource not found: {}", ex.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .path(getRequestPath(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    // Обработка валидационных ошибок
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Input validation failed")
            .path(getRequestPath(request))
            .details(fieldErrors)
            .build();
        
        return ResponseEntity.badRequest().body(error);
    }
    
    // Обработка неожиданных ошибок
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            WebRequest request) {
        
        log.error("Unexpected error", ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("An unexpected error occurred")
            .path(getRequestPath(request))
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

@Data
@Builder
class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> details;
}
```

## 7. Scheduled Tasks и ShedLock

### 7.1 Простой Scheduler

```java
@Component
@Slf4j
@EnableScheduling  // В конфигурации или главном классе
public class MaintenanceScheduler {
    
    // Каждый день в 9:00
    @Scheduled(cron = "0 0 9 * * *")
    public void checkExpiredOrders() {
        log.info("Starting expired orders check...");
        // логика проверки
        log.info("Expired orders check completed");
    }
    
    // Каждые 5 минут
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void syncInventory() {
        log.info("Syncing inventory...");
        // логика синхронизации
    }
    
    // Через 10 секунд после предыдущего завершения
    @Scheduled(fixedDelay = 10000)
    public void processQueue() {
        log.info("Processing queue...");
        // логика обработки
    }
}
```

**Cron expressions:**
```
┌───────────── секунды (0-59)
│ ┌───────────── минуты (0-59)
│ │ ┌───────────── часы (0-23)
│ │ │ ┌───────────── день месяца (1-31)
│ │ │ │ ┌───────────── месяц (1-12)
│ │ │ │ │ ┌───────────── день недели (0-7, 0 и 7 = воскресенье)
│ │ │ │ │ │
* * * * * *

Примеры:
0 0 9 * * *       - каждый день в 9:00
0 0 */2 * * *     - каждые 2 часа
0 30 8 * * MON    - каждый понедельник в 8:30
0 0 3 * * SUN     - каждое воскресенье в 3:00
```

### 7.2 ShedLock

```java
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")  // Максимальное время блокировки
public class SchedulerConfig {
    
    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(JdbcTemplateLockProvider.Configuration.builder()
            .withJdbcTemplate(new JdbcTemplate(dataSource))
            .usingDbTime()  // Использовать время БД, а не время сервера
            .build()
        );
    }
}

@Component
@Slf4j
public class DistributedScheduler {
    
    @Scheduled(cron = "0 0 9 * * *")
    @SchedulerLock(
        name = "checkExpiredOrders",  // Уникальное имя задачи
        lockAtMostFor = "9m",  // Максимум 9 минут блокировка
        lockAtLeastFor = "5m"  // Минимум 5 минут блокировка
    )
    public void checkExpiredOrders() {
        log.info("Checking expired orders (with ShedLock)...");
        // Только один инстанс приложения выполнит эту задачу!
    }
}
```

**Как работает ShedLock:**
1. Перед выполнением задача пытается получить блокировку в БД
2. Если блокировка получена - выполняется
3. Если блокировка уже занята - пропускается
4. После выполнения блокировка освобождается
5. `lockAtMostFor` защищает от "зависших" задач

---

**Напоминание:** Это примеры для понимания концепций, а не готовый код для copy-paste! Адаптируй эти паттерны под задачи из ТЗ.

