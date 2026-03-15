package com.recipefinder.backend.repository;

import com.recipefinder.backend.domain.Recipe;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecipeRepository extends CrudRepository<Recipe, Long> {
    List<Recipe> findAll();
    boolean existsByExternalId(String externalId);
}