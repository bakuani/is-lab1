package ru.ani.islab1.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ani.islab1.models.Person;
import ru.ani.islab1.repositories.PersonRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;

    public List<Person> getAll() {
        return personRepository.findAll();
    }
}
