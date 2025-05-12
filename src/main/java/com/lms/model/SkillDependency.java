package com.lms.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "skill_dependencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prerequisite_skill_id", nullable = false)
    private Skill prerequisiteSkill;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired;
}