package ohhudead.reservationsystem.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Value;


@Value
@Data
@Builder

public class CategoryResponse {
    private Long id;
    private String name;
}
