package com.muscleforger.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "template_exercises")
@Getter
@Setter
@NoArgsConstructor
public class TemplateExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    private TemplateDay day;

    @Column(name = "exercise_id", nullable = false, length = 50)
    private String exerciseId;

    @Column(nullable = false)
    private Short position;

    @Column(nullable = false)
    private Short sets;

    @Column(nullable = false)
    private Short reps;

    @Column(precision = 3, scale = 1)
    private BigDecimal rpe;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
