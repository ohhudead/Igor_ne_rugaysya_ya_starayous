create type order_status as enum ('pending', 'paid', 'shipped', 'delivered', 'cancelled');

alter type order_status owner to postgres;

create type payment_method as enum ('card', 'paypal', 'bank_transfer', 'cash');

alter type payment_method owner to postgres;

create table customers
(
    customer_id bigserial
        primary key,
    first_name  text                                   not null,
    last_name   text                                   not null,
    email       text                                   not null
        unique,
    city        text                                   not null,
    country     text                                   not null,
    created_at  timestamp with time zone default now() not null
);

alter table customers
    owner to postgres;

create table categories
(
    category_id   bigserial
        primary key,
    category_name text not null
        unique
);

alter table categories
    owner to postgres;

create table products
(
    product_id   bigserial
        primary key,
    category_id  bigint                                 not null
        references categories,
    product_name text                                   not null,
    price        numeric(10, 2)                         not null
        constraint products_price_check
            check (price >= (0)::numeric),
    in_stock     integer                                not null
        constraint products_in_stock_check
            check (in_stock >= 0),
    created_at   timestamp with time zone default now() not null
);

alter table products
    owner to postgres;

create index idx_products_category
    on products (category_id);

create table orders
(
    order_id         bigserial
        primary key,
    customer_id      bigint                   not null
        references customers,
    order_date       timestamp with time zone not null,
    status           training_ec.order_status not null,
    shipping_city    text                     not null,
    shipping_country text                     not null
);

alter table orders
    owner to postgres;

create index idx_orders_customer_date
    on orders (customer_id, order_date);

create table order_items
(
    order_item_id bigserial
        primary key,
    order_id      bigint         not null
        references orders
            on delete cascade,
    product_id    bigint         not null
        references products,
    quantity      integer        not null
        constraint order_items_quantity_check
            check (quantity > 0),
    unit_price    numeric(10, 2) not null
        constraint order_items_unit_price_check
            check (unit_price >= (0)::numeric)
    );

alter table order_items
    owner to postgres;

create index idx_order_items_order
    on order_items (order_id);

create index idx_order_items_product
    on order_items (product_id);

create table payments
(
    payment_id bigserial
        primary key,
    order_id   bigint                     not null
        unique
        references orders
            on delete cascade,
    method     training_ec.payment_method not null,
    amount     numeric(10, 2)             not null
        constraint payments_amount_check
            check (amount >= (0)::numeric),
    paid_at    timestamp with time zone   not null
);

alter table payments
    owner to postgres;

