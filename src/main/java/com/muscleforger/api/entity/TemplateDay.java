package com.muscleforger.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "template_days")
@Getter
@Setter
@NoArgsConstructor
public class TemplateDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private WorkoutTemplate template;

    @Column(name = "day_number", nullable = false)
    private Short dayNumber;

    @Column(length = 50)
    private String label;
}
