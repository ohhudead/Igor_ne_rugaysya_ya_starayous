package ohhudead.reservationsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.Setter;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;



@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ProductResponse {

    private Long id;
    private String name;
    private BigDecimal price;
    private Integer inStock;

    private Long categoryId;
    private String categoryName;

    private OffsetDateTime createdAt;
}
