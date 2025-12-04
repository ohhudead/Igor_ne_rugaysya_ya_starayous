package ohhudead.reservationsystem.mapper;


import ohhudead.reservationsystem.dto.ProductRequest;
import ohhudead.reservationsystem.dto.ProductResponse;
import ohhudead.reservationsystem.entity.Product;
import ohhudead.reservationsystem.entity.Category;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)

public interface ProductMapper {

    @Mapping(
            source = "category",
            target = "categoryId",
            qualifiedByName = "categoryToId"
    )
    @Mapping(
            source = "category",
            target = "categoryName",
            qualifiedByName = "CategoryToName"
    )
    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Product toEntity(ProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateFromRequest(ProductRequest request, @MappingTarget Product product);

    @Named("categoryToId")
    default Long categoryToId(Category category) {
        return category != null ? category.getId() : null;
    }

    @Named("CategoryToName")
    default String categoryToName(Category category) {
        return category != null ? category.getName() : null;
    }

}
