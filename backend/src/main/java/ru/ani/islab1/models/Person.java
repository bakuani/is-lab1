package ru.ani.islab1.models;

import jakarta.validation.Valid;
import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import ru.ani.islab1.models.enums.Color;
import ru.ani.islab1.models.enums.Country;

@Entity
@Table(name = "person")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull @NotBlank
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Color eyeColor;

    @Enumerated(EnumType.STRING)
    private Color hairColor; // nullable

    @NotNull
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id")
    private Location location;

    @Min(1)
    private Float weight;

    @Enumerated(EnumType.STRING)
    private Country nationality; // nullable
}