package ohhudead.reservationsystem.repository;

import ohhudead.reservationsystem.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>{

    long countByCategoryId(Long id);

    boolean existByCategoryId(Long categoryId);

    List<Product> findByCategoryId(Long categoryId);
}
