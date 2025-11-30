package ohhudead.reservationsystem.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ohhudead.reservationsystem.dto.ProductRequest;
import ohhudead.reservationsystem.dto.ProductResponse;
import ohhudead.reservationsystem.entity.Product;
import ohhudead.reservationsystem.entity.Category;
import ohhudead.reservationsystem.mapper.ProductMapper;
import ohhudead.reservationsystem.repository.CategoryRepository;
import ohhudead.reservationsystem.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional

public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Transactional
    public List<ProductResponse> getAll(Long categoryId){
        log.info("Get products, categoryId={}", categoryId);

        List<Product> products = (categoryId == null)
                ? productRepository.findAll()
                : productRepository.findByCategoryId(categoryId);

        return products.stream()
                .map(productMapper::toResponse)
                .toList();

    }

    public ProductResponse getById(Long id) {
        log.info("Get product by id={}", id);


        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        return productMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        log.info("Create product: {}", request);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found: " + request.getCategoryId()));

        Product product = productMapper.toEntity(request);
        product.setCategory(category);

        product = productRepository.save(product);
        return productMapper.toResponse(product);
    }
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        log.info("Update product id={}, request={}", id, request);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));

        if(request.getName() != null){
            product.setName(request.getName());
        }
        if(request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if(request.getInStock() != null) {
            product.setInStock(request.getInStock());
        }
        if(request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found: " + request.getCategoryId()));
            product.setCategory(category);
        }

        product = productRepository.save(product);
        return productMapper.toResponse(product);
    }

    @Transactional
    public void delete(Long id){
        log.info("Delete product id={}", id);
        productRepository.deleteById(id);
    }

}
