package ohhudead.reservationsystem.mapper;

import ohhudead.reservationsystem.dto.CategoryRequest;
import ohhudead.reservationsystem.dto.CategoryResponse;
import ohhudead.reservationsystem.entity.Category;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.Named;
import org.mapstruct.*;


@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)

public interface CategoryMapper {

    CategoryResponse toResponse(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "name", source = "name", qualifiedByName = "normalizeName")
    @Mapping(target = "description", source = "description", qualifiedByName = "normalizeDescription")
    Category toEntity(CategoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "name", source = "name", qualifiedByName = "normalizeName")
    @Mapping(target = "description", source = "description", qualifiedByName = "normalizeDescription")
    void updateFromRequest(CategoryRequest request, @MappingTarget Category category);

    @Named("normalizeName")
    @SuppressWarnings("unused")
    default String normalizeName(String name) {
        if(name == null) {
            return null;
        }
        String trimmed = name.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Named("normalizeDescription")
    @SuppressWarnings("unused")
    default String normalizeDescription(String description) {
        if(description == null) {
            return null;
        }
        String normalized = description.trim();

        normalized = normalized.replaceAll("\\s+", " ");

        if(normalized.isEmpty()) {
            return null;
        }

        int maxLength = 255;
        if(normalized.length() > maxLength) {
            normalized = normalized.substring(0, maxLength);
        }
        return normalized;
    }
}

