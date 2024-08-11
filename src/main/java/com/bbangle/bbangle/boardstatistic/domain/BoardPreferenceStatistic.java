package com.bbangle.bbangle.boardstatistic.domain;

import com.bbangle.bbangle.preference.domain.PreferenceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Builder
@Getter
@Table(name = "board_preference_statistic")
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

    @Column(name = "basic_score", columnDefinition = "double")
    @ColumnDefault("0")
    private Double basicScore;

    @Column(name = "preference_weight", columnDefinition = "int")
    @ColumnDefault("1")
    private Integer preferenceWeight;

    @Column(name = "preference_score")
    @ColumnDefault("0")
    private Double preferenceScore;

    public void updateWeightWhileInitializing(int weight){
        this.preferenceWeight = weight;
    }

    @PrePersist
    public void prePersist(){
        if(this.basicScore == null){
            this.basicScore = 0.0;
        }

        if(this.preferenceWeight == null){
            this.preferenceWeight = 1;
        }

        if(this.preferenceScore == null){
            this.preferenceScore = 0.0;
        }
    }

    public void updateToBasicBoardScore(Double basicScore) {
        this.basicScore = basicScore;
    }

    public void updatePreferenceScore() {
        this.preferenceScore = basicScore * preferenceWeight;
    }

}
