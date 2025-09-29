package ru.ani.islab1.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ani.islab1.models.Coordinates;

import java.util.Optional;

@Repository
public interface CoordinatesRepository extends JpaRepository<Coordinates, Integer> {
    Optional<Coordinates> findByXAndY(Double x, Double y);
}
