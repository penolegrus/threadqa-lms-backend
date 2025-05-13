package com.threadqa.lms.model.assessment;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "test_matching_pairs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestMatchingPair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private TestQuestion question;

    @Column(name = "left_item", columnDefinition = "TEXT", nullable = false)
    private String leftItem;

    @Column(name = "right_item", columnDefinition = "TEXT", nullable = false)
    private String rightItem;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
}