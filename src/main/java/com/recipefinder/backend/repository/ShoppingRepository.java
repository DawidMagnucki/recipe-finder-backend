package com.recipefinder.backend.repository;

import com.recipefinder.backend.domain.ShoppingItem;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShoppingRepository extends CrudRepository<ShoppingItem, Long> {
    List<ShoppingItem> findAll();
}