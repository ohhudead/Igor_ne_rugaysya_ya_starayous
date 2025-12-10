package ohhudead.reservationsystem.repository;

import ohhudead.reservationsystem.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer,Long>{
}
