package ru.ani.islab1.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ani.islab1.models.Location;
import ru.ani.islab1.models.Person;
import ru.ani.islab1.models.enums.Color;
import ru.ani.islab1.models.enums.Country;

import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Integer> {
    Optional<Person> findByNameAndWeightAndEyeColorAndHairColorAndNationalityAndLocationXAndLocationYAndLocationName(
            String name, Float weight, Color eyeColor, Color hairColor, Country nationality, Long lx, Double ly, String lname);
    boolean existsByLocationAndIdNot(Location location, Integer id);
}
