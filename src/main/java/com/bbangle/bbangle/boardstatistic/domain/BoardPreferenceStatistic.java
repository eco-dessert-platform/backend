package com.bbangle.bbangle.boardstatistic.domain;

import com.bbangle.bbangle.preference.domain.PreferenceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BoardPreferenceStatistic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "board_id", columnDefinition = "bigint")
    private Long boardId;

    @Column(name = "preference_type", columnDefinition = "varchar(30)")
    @Enumerated(EnumType.STRING)
    private PreferenceType preferenceType;

    @Column(name = "preference_weight", columnDefinition = "int")
    @ColumnDefault("1")
    private Integer preferenceWeight;

    @Column(name = "preference_score")
    private Long preferenceScore;

}
