package com.muscleforger.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "exercises")
@Getter
@Setter
@NoArgsConstructor
public class Exercise {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String bodyPart;

    @Column(nullable = false)
    private String target;

    @Column(nullable = false)
    private String equipment;

    // Stored as JSON array string e.g. ["obliques","hip flexors"]
    @Column(columnDefinition = "TEXT")
    private String secondaryMuscles;

    // Stored as JSON array string
    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String difficulty;
    private String category;
    private String gifUrl;
}
