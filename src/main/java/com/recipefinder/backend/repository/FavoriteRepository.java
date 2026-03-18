package com.recipefinder.backend.repository;

import com.recipefinder.backend.domain.Favorite;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends CrudRepository<Favorite, Long> {
    List<Favorite> findAll();
    boolean existsByRecipeId(Long recipeId);
    Optional<Favorite> findByRecipeId(Long recipeId);
}
