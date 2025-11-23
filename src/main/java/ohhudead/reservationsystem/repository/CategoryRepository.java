package ohhudead.reservationsystem.repository;

import ohhudead.reservationsystem.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
