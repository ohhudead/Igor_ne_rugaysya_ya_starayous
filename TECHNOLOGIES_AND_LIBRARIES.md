# Технологии и библиотеки для проекта

Этот документ объясняет, какие технологии и библиотеки нужно добавить в проект, зачем они нужны и на что обратить внимание при изучении.

## 1. MapStruct - Маппинг между объектами

### Зачем нужен?

**Проблема:** В приложении есть Entity (JPA сущности для работы с БД) и DTO (объекты для API). Нужно постоянно конвертировать одно в другое.

**Плохое решение:**
```java
public ProductResponse toResponse(Product product) {
    ProductResponse response = new ProductResponse();
    response.setId(product.getId());
    response.setName(product.getName());
    response.setPrice(product.getPrice());
    // ... еще 10 строк однотипного кода
    return response;
}
```

**Проблемы ручного маппинга:**
- Много boilerplate кода
- Легко забыть замапить поле
- При добавлении нового поля - нужно обновлять все маппинги
- Сложно поддерживать

**Решение:** MapStruct генерирует код маппинга на этапе компиляции.

### Как подключить?

**build.gradle:**
```gradle
dependencies {
    implementation 'org.mapstruct:mapstruct:1.5.5.Final'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
    
    // Для работы с Lombok (если используется вместе)
    annotationProcessor 'org.projectlombok:lombok-mapstruct-binding:0.2.0'
}
```

### На что обратить внимание?

1. **componentModel = "spring"** - MapStruct создает Spring beans, можно инжектить через constructor
2. **Генерация кода** - посмотри в `build/generated/sources/annotationProcessor` - там реальный код!
3. **Производительность** - нет рефлексии, только прямые вызовы setters/getters
4. **Сложные маппинги** - можно использовать `@Mapping`, `@AfterMapping`, expression

### Что почитать?

- [Официальная документация MapStruct](https://mapstruct.org/documentation/stable/reference/html/)
- [Baeldung: Quick Guide to MapStruct](https://www.baeldung.com/mapstruct)
- Ключевые разделы: Basic mappings, Custom mappings, Mapping nested beans

---

## 2. Jakarta Bean Validation (Hibernate Validator)

### Зачем нужен?

**Проблема:** Нужно валидировать входящие данные от пользователя (проверять, что email корректный, цена положительная и т.д.).

**Плохое решение:**
```java
public void createProduct(ProductRequest request) {
    if (request.getName() == null || request.getName().isEmpty()) {
        throw new IllegalArgumentException("Name is required");
    }
    if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("Price must be positive");
    }
    // ... еще 20 строк проверок
}
```

**Проблемы:**
- Код валидации размазан по сервисам
- Трудно поддерживать
- Нет единообразия в сообщениях об ошибках
- Сложно тестировать

**Решение:** Декларативная валидация через аннотации.

### Как подключить?

**build.gradle:**
```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-validation'
}
```

Spring Boot стартер уже включает Hibernate Validator (реализацию Jakarta Bean Validation).

### На что обратить внимание?

1. **Аннотации на DTO, не на Entity!** - Entity отражает БД, DTO - бизнес-правила
2. **@Valid в контроллере** - без этой аннотации валидация не работает
3. **Группы валидации** - разные правила для create/update
4. **Кастомные валидаторы** - для бизнес-правил (например, проверка уникальности email)
5. **MethodArgumentNotValidException** - обработать через @RestControllerAdvice

### Основные аннотации

| Аннотация | Назначение | Пример |
|-----------|-----------|---------|
| @NotNull | Значение не null | @NotNull private Long id; |
| @NotEmpty | Не пустая строка/коллекция | @NotEmpty private String name; |
| @NotBlank | Не пустая строка (без пробелов) | @NotBlank private String email; |
| @Size | Размер строки/коллекции | @Size(min=3, max=100) |
| @Min, @Max | Числовые границы | @Min(0) private Integer stock; |
| @Email | Валидация email | @Email private String email; |
| @Pattern | Регулярное выражение | @Pattern(regexp="...") |
| @Positive | Положительное число | @Positive private BigDecimal price; |
| @DecimalMin/Max | Границы для decimal | @DecimalMin("0.01") |
| @Past, @Future | Дата в прошлом/будущем | @Past private LocalDate birthDate; |
| @Valid | Валидация вложенных объектов | @Valid private Address address; |

### Что почитать?

- [Jakarta Bean Validation specification](https://beanvalidation.org/2.0/spec/)
- [Baeldung: Bean Validation](https://www.baeldung.com/javax-validation)
- [Spring Boot Validation](https://spring.io/guides/gs/validating-form-input/)
- Ключевые темы: Built-in constraints, Custom validators, Validation groups

---

## 3. Spring Scheduler (@Scheduled)

### Зачем нужен?

**Проблема:** Нужно выполнять задачи по расписанию (очистка старых данных, отправка уведомлений, синхронизация и т.д.).

**Решение:** Spring предоставляет встроенный планировщик задач.

### Как подключить?

Уже включено в Spring Boot! Нужно только активировать:

```java
@SpringBootApplication
@EnableScheduling  // Включаем поддержку планировщика
public class Application {
    // ...
}
```

### Способы настройки расписания

1. **fixedRate** - фиксированная частота (в миллисекундах)
   ```java
   @Scheduled(fixedRate = 60000)  // Каждую минуту
   ```

2. **fixedDelay** - задержка после завершения предыдущего выполнения
   ```java
   @Scheduled(fixedDelay = 30000)  // Через 30 сек после завершения
   ```

3. **cron** - cron-выражения (самый гибкий)
   ```java
   @Scheduled(cron = "0 0 9 * * *")  // Каждый день в 9:00
   ```

### Cron выражения - важно понять!

Формат: `секунда минута час день_месяца месяц день_недели`

Примеры:
- `0 0 * * * *` - каждый час
- `0 */15 * * * *` - каждые 15 минут
- `0 0 9 * * MON-FRI` - по будням в 9:00
- `0 0 0 1 * *` - первого числа каждого месяца в полночь

**Онлайн-генераторы cron:**
- https://crontab.guru/ (Unix cron)
- Spring cron немного отличается (поддерживает секунды)

### На что обратить внимание?

1. **Thread pool** - по умолчанию один поток! Если задачи долгие - настроить пул
2. **Exception handling** - исключение в одной задаче не должно ломать другие
3. **Логирование** - обязательно логировать начало/конец/результаты задач
4. **Timezone** - cron использует серверное время, можно указать zone

### Что почитать?

- [Spring @Scheduled Documentation](https://docs.spring.io/spring-framework/reference/integration/scheduling.html)
- [Baeldung: Spring @Scheduled](https://www.baeldung.com/spring-scheduled-tasks)
- Понять разницу между fixedRate и fixedDelay

---

## 4. ShedLock - Распределенная блокировка для Scheduler

### Зачем нужен?

**Проблема:** При запуске нескольких инстансов приложения (например, в кластере или для high availability), каждый инстанс будет выполнять scheduled задачи.

**Сценарий:**
- Задача: "Отменить просроченные заказы" выполняется каждое утро в 9:00
- Запущено 3 инстанса приложения
- Результат: задача выполнится 3 раза! (может быть проблемой)

**Решение:** ShedLock обеспечивает, что задача выполнится только на одном инстансе.

### Как работает?

1. Перед выполнением задача пытается получить блокировку в БД (вставка записи в таблицу shedlock)
2. Если блокировка получена - задача выполняется
3. Если блокировка занята другим инстансом - задача пропускается
4. После выполнения блокировка автоматически освобождается

### Как подключить?

**build.gradle:**
```gradle
dependencies {
    implementation 'net.javacrumbs.shedlock:shedlock-spring:5.10.0'
    implementation 'net.javacrumbs.shedlock:shedlock-provider-jdbc-template:5.10.0'
}
```

**Таблица в БД (уже есть в миграции 002-shedlock-table.sql):**
```sql
CREATE TABLE shedlock (
    name VARCHAR(64) NOT NULL PRIMARY KEY,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL
);
```

**Конфигурация:**
```java
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class SchedulerConfig {
    
    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(dataSource);
    }
}
```

### Ключевые параметры

**@SchedulerLock:**
```java
@SchedulerLock(
    name = "uniqueTaskName",     // Уникальное имя задачи
    lockAtMostFor = "10m",       // Максимальное время блокировки
    lockAtLeastFor = "5m"        // Минимальное время блокировки
)
```

**lockAtMostFor:**
- Защита от "зависших" задач
- Если инстанс упал и не освободил блокировку - она освободится автоматически через это время
- Должно быть больше, чем реальное время выполнения задачи

**lockAtLeastFor:**
- Предотвращает слишком частое выполнение
- Даже если задача завершилась быстро, блокировка не освободится раньше этого времени
- Защита от багов в cron-выражениях

### Форматы времени

- `PT30S` - 30 секунд
- `PT5M` - 5 минут
- `PT2H` - 2 часа
- `P1D` - 1 день
- Или сокращенно: `"30s"`, `"5m"`, `"2h"`, `"1d"`

### На что обратить внимание?

1. **Уникальность name** - каждая задача должна иметь уникальное имя
2. **Время блокировки** - подбирать исходя из реального времени выполнения
3. **Мониторинг** - логировать, какой инстанс выполнил задачу
4. **Таблица shedlock** - периодически чистить старые записи (или использовать TTL)
5. **Тестирование** - сложно протестировать локально, нужны интеграционные тесты

### Альтернативы

- **Quartz Scheduler** - более мощный, но сложнее (поддерживает кластеризацию из коробки)
- **Spring Cloud Task** - для cloud-native приложений
- ShedLock - золотая середина: простой, эффективный, легкий

### Что почитать?

- [ShedLock GitHub](https://github.com/lukas-krecan/ShedLock)
- [Baeldung: ShedLock](https://www.baeldung.com/shedlock-spring)
- Понять разницу между lockAtMostFor и lockAtLeastFor

---

## 5. Spring Data JPA - Работа с базой данных

### Зачем нужен?

**Проблема:** Писать JDBC код для работы с БД долго и утомительно (connection, prepared statements, result sets, закрытие ресурсов).

**Решение:** Spring Data JPA абстрагирует работу с БД, генерирует запросы автоматически.

### Уже подключено!

```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
```

### Ключевые концепции

**JpaRepository:**
- Предоставляет CRUD операции из коробки (save, findById, findAll, delete...)
- Можно добавлять кастомные методы по naming convention
- Поддерживает @Query для сложных запросов

**Naming conventions:**
```java
// Spring Data парсит имя метода и генерирует запрос!
findByName(String name)
findByPriceGreaterThan(BigDecimal price)
findByCategoryNameAndPriceLessThan(String category, BigDecimal price)
countByStatus(OrderStatus status)
deleteByCreatedAtBefore(LocalDateTime date)
```

**Ключевые слова:**
- `findBy`, `getBy`, `queryBy` - поиск
- `countBy` - подсчет
- `deleteBy`, `removeBy` - удаление
- `And`, `Or` - логические операторы
- `GreaterThan`, `LessThan`, `Between` - сравнения
- `Like`, `Containing`, `StartingWith`, `EndingWith` - поиск по подстроке
- `OrderBy` - сортировка
- `First`, `Top` - ограничение результатов

### Fetching стратегии - критически важно!

**LAZY (по умолчанию для коллекций):**
```java
@OneToMany(fetch = FetchType.LAZY)
private List<OrderItem> items;
```
- Связанные данные загружаются только при обращении
- Проблема N+1: в цикле будет N запросов к БД!

**EAGER (по умолчанию для @ManyToOne, @OneToOne):**
```java
@ManyToOne(fetch = FetchType.EAGER)
private Category category;
```
- Связанные данные загружаются сразу
- Может быть неэффективно при больших объемах

**Решение N+1 проблемы:**
```java
// JOIN FETCH в JPQL
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
Order findByIdWithItems(@Param("id") Long id);

// Entity Graph
@EntityGraph(attributePaths = {"items", "customer"})
Order findById(Long id);
```

### На что обратить внимание?

1. **Транзакции** - все операции изменения должны быть в @Transactional
2. **N+1 проблема** - самая частая ошибка производительности!
3. **Проекции** - выбирать только нужные поля для оптимизации
4. **Pagination** - использовать Pageable для больших списков
5. **Lazy initialization** - не обращаться к LAZY полям вне транзакции

### Что почитать?

- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/reference/)
- [Baeldung: Spring Data JPA](https://www.baeldung.com/the-persistence-layer-with-spring-data-jpa)
- [Vlad Mihalcea: High-Performance Java Persistence](https://vladmihalcea.com/) - лучший ресурс по JPA/Hibernate
- Ключевые темы: Repository patterns, Query methods, N+1 problem, Fetch strategies

---

## 6. Lombok - Уменьшение boilerplate кода

### Зачем нужен?

**Проблема:** Java многословна - нужно писать getters, setters, constructors, toString, equals, hashCode...

**Решение:** Lombok генерирует этот код через аннотации на этапе компиляции.

### Уже подключено!

```gradle
compileOnly 'org.projectlombok:lombok'
annotationProcessor 'org.projectlombok:lombok'
```

### Основные аннотации

| Аннотация | Что генерирует | Использование |
|-----------|----------------|---------------|
| @Getter/@Setter | Getters/Setters | На классе или поле |
| @ToString | toString() | На классе |
| @EqualsAndHashCode | equals() и hashCode() | На классе |
| @NoArgsConstructor | Конструктор без параметров | На классе |
| @AllArgsConstructor | Конструктор со всеми полями | На классе |
| @RequiredArgsConstructor | Конструктор с final полями | На классе |
| @Data | @Getter + @Setter + @ToString + @EqualsAndHashCode + @RequiredArgsConstructor | На классе |
| @Builder | Паттерн Builder | На классе |
| @Slf4j | Logger logger = LoggerFactory.getLogger(...) | На классе |
| @Value | Immutable объект | На классе |

### Когда использовать

**Entity:**
```java
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
```

**DTO:**
```java
@Data  // или @Getter + @Setter
@NoArgsConstructor
@AllArgsConstructor
```

**Service:**
```java
@Service
@RequiredArgsConstructor  // Constructor injection для final полей
@Slf4j  // Для логирования
```

### Осторожно!

1. **@Data на Entity** - может вызвать проблемы с Hibernate (циклические зависимости в toString/equals)
2. **@ToString на Entity с коллекциями** - может вызвать N+1 проблему
3. **@EqualsAndHashCode на Entity** - использовать только ID для сравнения
4. **@Builder и JPA** - Builder не работает с JPA из коробки, нужен также @NoArgsConstructor

### Что почитать?

- [Lombok Documentation](https://projectlombok.org/features/)
- [Baeldung: Lombok](https://www.baeldung.com/intro-to-project-lombok)
- Понять: Annotation Processing, Generated code location

---

## 7. PostgreSQL JDBC Driver + Liquibase

### PostgreSQL Driver

Уже подключен! Позволяет Java приложению общаться с PostgreSQL.

```gradle
implementation 'org.postgresql:postgresql:42.7.3'
```

### Liquibase - Миграции БД

**Зачем нужен?**
- Версионирование схемы БД (как Git для базы данных)
- Автоматическое применение изменений при запуске приложения
- Rollback изменений
- Работа в команде (все имеют одинаковую схему)

**Уже подключено:**
```gradle
implementation 'org.liquibase:liquibase-core'
```

**Структура:**
```
db/
  changelog/
    db.changelog-master.yaml  <- Главный файл
    001-init-schema.sql       <- Первая миграция
    002-shedlock-table.sql    <- Вторая миграция
```

**Как добавить новую миграцию:**
1. Создай файл `003-add-new-table.sql`
2. Добавь в `db.changelog-master.yaml`:
   ```yaml
   - include:
       file: db/changelog/003-add-new-table.sql
   ```
3. При запуске приложения Liquibase применит изменения автоматически

### На что обратить внимание?

1. **Не изменяй старые миграции!** - создавай новые для исправления
2. **Таблицы Liquibase** - `databasechangelog` и `databasechangeloglock`
3. **Rollback** - можно откатить изменения (для SQL нужно писать rollback вручную)

### Что почитать?

- [Liquibase Documentation](https://docs.liquibase.com/)
- [Baeldung: Liquibase](https://www.baeldung.com/liquibase-refactor-schema-of-java-app)

---

## 8. JUnit 5 + Mockito - Тестирование

### Зачем нужно?

**Проблема:** Как убедиться, что код работает корректно? Как не сломать существующую функциональность при изменениях?

**Решение:** Автоматизированное тестирование.

### Уже подключено!

```gradle
testImplementation 'org.springframework.boot:spring-boot-starter-test'
```

Включает: JUnit 5, Mockito, AssertJ, Hamcrest, Spring Test

### Типы тестов

**Unit тесты:**
- Тестируют один класс изолированно
- Все зависимости мокируются
- Быстрые, не требуют БД
```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;
    
    @InjectMocks
    private OrderService orderService;
}
```

**Integration тесты:**
- Тестируют взаимодействие компонентов
- Используют реальную БД (TestContainers)
- Медленнее, но ближе к реальности
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
}
```

### JUnit 5 основные аннотации

- `@Test` - метод теста
- `@BeforeEach` - выполняется перед каждым тестом
- `@AfterEach` - после каждого теста
- `@BeforeAll` - один раз перед всеми тестами (static)
- `@AfterAll` - один раз после всех тестов (static)
- `@Disabled` - отключить тест
- `@DisplayName` - читаемое имя теста
- `@ParameterizedTest` - параметризованный тест

### Mockito основы

**Создание моков:**
```java
@Mock
private ProductRepository repository;
```

**Stubbing (настройка поведения):**
```java
when(repository.findById(1L))
    .thenReturn(Optional.of(product));
```

**Verification (проверка вызовов):**
```java
verify(repository, times(1)).save(any(Product.class));
verify(repository, never()).delete(any());
```

### Структура теста (AAA pattern)

```java
@Test
void shouldCreateOrder() {
    // Arrange (подготовка)
    OrderCreateRequest request = new OrderCreateRequest();
    // ... setup
    
    // Act (действие)
    OrderResponse response = orderService.createOrder(request);
    
    // Assert (проверка)
    assertThat(response.getId()).isNotNull();
    assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
}
```

### На что обратить внимание?

1. **Не тестировать getters/setters** - это тестирование компилятора
2. **Тестировать поведение, а не реализацию** - что делает, а не как
3. **Один assert на тест** - идеально, но не всегда практично
4. **Читаемые названия тестов** - `shouldReturnErrorWhenStockInsufficient`
5. **Не мокировать всё подряд** - integration тесты для сложных сценариев

### Что почитать?

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Baeldung: Testing in Spring Boot](https://www.baeldung.com/spring-boot-testing)
- [Martin Fowler: Test Pyramid](https://martinfowler.com/articles/practical-test-pyramid.html)

---

## 9. Дополнительные библиотеки (опционально)

### SpringDoc OpenAPI (Swagger)

**Зачем:** Автоматическая документация API.

```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
```

После запуска: http://localhost:8080/swagger-ui.html

### TestContainers

**Зачем:** Запуск реальной PostgreSQL в Docker для интеграционных тестов.

```gradle
testImplementation 'org.testcontainers:testcontainers:1.19.3'
testImplementation 'org.testcontainers:postgresql:1.19.3'
testImplementation 'org.testcontainers:junit-jupiter:1.19.3'
```

### QueryDSL

**Зачем:** Type-safe queries (альтернатива @Query для сложных запросов).

Сложнее в setup, но очень мощный для динамических фильтров.

---

## Рекомендуемый порядок изучения

1. **Начать с основ:**
   - Spring Data JPA (entities, repositories)
   - Lombok (для уменьшения кода)

2. **Перейти к архитектуре:**
   - DTO паттерн (зачем нужен)
   - MapStruct (автоматизация маппинга)

3. **Валидация и обработка ошибок:**
   - Jakarta Validation (проверка входных данных)
   - @RestControllerAdvice (централизованная обработка ошибок)

4. **Транзакции:**
   - @Transactional (критически важно!)
   - ACID properties

5. **Планировщик:**
   - @Scheduled (базовые задачи)
   - ShedLock (для кластера)

6. **Тестирование:**
   - JUnit 5 (unit тесты)
   - Mockito (мокирование)
   - Integration tests (полный цикл)

---

## Полезные ресурсы

**Официальная документация:**
- [Spring Boot Docs](https://docs.spring.io/spring-boot/index.html)
- [Spring Data JPA Docs](https://docs.spring.io/spring-data/jpa/reference/)

**Tutorials:**
- [Baeldung](https://www.baeldung.com/) - лучший ресурс по Spring
- [Spring Guides](https://spring.io/guides) - официальные туториалы

**Книги:**
- "Spring in Action" by Craig Walls
- "High-Performance Java Persistence" by Vlad Mihalcea (для работы с JPA/Hibernate)

**YouTube каналы:**
- Spring I/O - конференции
- Java Brains - tutorials
- Amigoscode - практические примеры

---

**Важный совет:** Не пытайся выучить всё сразу! Изучай последовательно, по мере работы над задачами из ТЗ. Экспериментируй, ломай, исправляй - так учишься лучше всего.

