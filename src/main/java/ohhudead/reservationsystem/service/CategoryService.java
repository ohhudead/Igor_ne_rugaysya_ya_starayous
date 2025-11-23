package ohhudead.reservationsystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ohhudead.reservationsystem.entity.Category;
import ohhudead.reservationsystem.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAll() {
        log.info("Getting all categories");
        return categoryRepository.findAll();
    }

    public Category create(Category category) {
        log.info("Creating category with name={}", category.getName());
        return categoryRepository.save(category);
    }
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
    }
    public Category update(Long id, Category request) {
        Category existing = getById(id);
        existing.setName(request.getName());
        return categoryRepository.save(existing);
    }
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }
}