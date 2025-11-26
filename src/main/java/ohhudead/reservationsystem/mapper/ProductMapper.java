package ohhudead.reservationsystem.mapper;

import ohhudead.reservationsystem.dto.ProductRequest;
import ohhudead.reservationsystem.dto.ProductResponse;
import ohhudead.reservationsystem.entity.Product;
import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

//    @Mapping(source = "category.id", target = "categoryId")
//    @Mapping(source = "category.name", target = "categoryName")
    ProductResponse toResponse(Product product);

//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "category", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
    Product toEntity(ProductRequest request);
}
