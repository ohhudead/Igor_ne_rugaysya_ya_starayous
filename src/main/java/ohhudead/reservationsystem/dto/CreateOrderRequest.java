package ohhudead.reservationsystem.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull Long customerId,
        @NotNull List<CreateOrderItemRequest> items
) {
}
