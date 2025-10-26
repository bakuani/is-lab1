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

    @NotBlank(message = "Имя обязательно")
    private String name;

    @NotNull(message = "Eye color обязательно")
    @Enumerated(EnumType.STRING)
    private Color eyeColor;

    @Enumerated(EnumType.STRING)
    private Color hairColor;

    @NotNull
    @Valid
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id")
    private Location location;

    @Min(value = 1, message = "Weight ≥ 1")
    private Float weight;

    @Enumerated(EnumType.STRING)
    private Country nationality;
}
