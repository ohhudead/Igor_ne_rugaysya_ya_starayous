package ohhudead.reservationsystem.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    private String name;
    private BigDecimal price;
    private Integer inStock;
    private Long categoryId;
}
