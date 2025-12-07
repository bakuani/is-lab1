package ru.ani.islab1.models;

import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cache;

@Entity
@Table(name = "location")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Long x;

    @NotNull
    private Double y;

    private String name;
}