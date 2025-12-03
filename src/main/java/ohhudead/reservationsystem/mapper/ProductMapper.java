package ohhudead.reservationsystem.mapper;


import org.mapstruct.BeanMapping;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ohhudead.reservationsystem.dto.ProductRequest;
import ohhudead.reservationsystem.dto.ProductResponse;
import ohhudead.reservationsystem.entity.Product;

@Mapper(componentModel = "spring",nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

public interface ProductMapper {

    @Mapping(
            target = "categoryId",
            expression = "java(product.getCategory() != null ? product.getCategory().getId() : null)"
    )
    @Mapping(
            target = "categoryName",
            expression = "java(product.getCategory() != null ? product.getCategory().getName() : null)"
    )
    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Product toEntity(ProductRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateFromRequest(ProductRequest request, @MappingTarget Product product);

}
