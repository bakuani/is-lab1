package ru.ani.islab1.models;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "coordinates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coordinates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Double x;

    @Max(697)
    private Double y;


}