package ru.ani.islab1.models;

import jakarta.validation.Valid;
import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import ru.ani.islab1.models.enums.Color;
import ru.ani.islab1.models.enums.Country;
import org.hibernate.annotations.Cache;

@Entity
@Table(name = "person")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
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
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id")
    private Location location;

    @Min(value = 1, message = "Weight должно быть ≥ 1")
    private Float weight;

    @Enumerated(EnumType.STRING)
    private Country nationality;
}
