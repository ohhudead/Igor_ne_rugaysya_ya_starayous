package ohhudead.reservationsystem.repository;

import ohhudead.reservationsystem.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order,Long>{
}
