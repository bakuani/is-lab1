package ru.ani.islab1.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ani.islab1.models.Location;
import ru.ani.islab1.models.Person;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Integer> {
    Optional<Location> findByXAndYAndName(Long x, Double y, String name);
}
