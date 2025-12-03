package ohhudead.reservationsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.ArrayList;

// TODO [PHASE 1]: Создать остальные JPA Entity классы
// - Customer (customers) - клиенты магазина
// - Product (products) - товары с привязкой к Category
// - Order (orders) - заказы клиентов
// - OrderItem (order_items) - позиции в заказе (связь Order-Product)
// - Payment (payments) - информация об оплате заказа
//
// ВАЖНО изучить:
// 1. Как работают связи @OneToMany, @ManyToOne, @OneToOne
// 2. Разницу между FetchType.LAZY и EAGER (почему LAZY лучше по умолчанию?)
// 3. Что такое CascadeType и когда его использовать
// 4. Как правильно использовать @Enumerated(EnumType.STRING) для OrderStatus и PaymentMethod
// 5. Bidirectional vs Unidirectional relationships
//
// Читать: TECHNICAL_REQUIREMENTS.md раздел 3.1, CODE_EXAMPLES_AND_PATTERNS.md раздел 1

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(name = "category_name", nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    // TODO [PHASE 1]: Добавить связь @OneToMany с Product
    // Вопросы для понимания:
    // - Почему нужен параметр mappedBy?
    // - Почему стоит использовать FetchType.LAZY?
    // - Что произойдет при удалении Category, если есть связанные Products?
    // - Нужен ли cascade в этом случае?
}
