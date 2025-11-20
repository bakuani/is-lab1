package ru.ani.islab1.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ani.islab1.models.Location;
import ru.ani.islab1.models.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, Integer> {
    boolean existsByLocationAndIdNot(Location location, Integer id);
    boolean existsByNameAndLocation_XAndLocation_YAndLocation_Name(
            String name,
            Long locationX,
            Double locationY,
            String locationName
    );

    boolean existsByNameAndLocation_XAndLocation_YAndLocation_NameAndIdNot(
            String name,
            Long locationX,
            Double locationY,
            String locationName,
            Integer id
    );
}