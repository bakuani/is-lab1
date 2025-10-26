package ru.ani.islab1.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ani.islab1.models.Person;
import ru.ani.islab1.services.PersonService;

import java.util.List;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
@Validated
public class PersonController {

    private final PersonService personService;

    @GetMapping
    public ResponseEntity<List<Person>> getAll() {
        return ResponseEntity.ok(personService.getAll());
    }
}
