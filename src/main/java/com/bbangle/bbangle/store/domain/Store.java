package com.bbangle.bbangle.store.domain;

import com.bbangle.bbangle.board.domain.Board;
import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "store")
@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identifier")
    private String identifier;

    @Column(name = "name")
    private String name;

    @Column(name = "introduce")
    private String introduce;

    @Column(name = "profile")
    private String profile;

    @Column(name = "is_deleted", columnDefinition = "tinyint")
    private boolean isDeleted;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private StoreStatus status;

    @OneToMany(mappedBy = "store", fetch = FetchType.LAZY)
    List<Board> boards = new ArrayList<>();

    public static Store createForSeller(String name) {
       return new Store(name,"00000",false, StoreStatus.NONE);
    }

    private Store (String name, String identifier,boolean isDeleted, StoreStatus status) {
        validateField(name);
        this.name = name;
        this.identifier = identifier;
        this.isDeleted = isDeleted;
        this.status = status;
    }

    public void changeStatus(StoreStatus status) {
        this.status = status;
    }

    private void validateField(String name){
        if (name == null || name.isEmpty()) {
            throw new BbangleException(BbangleErrorCode.INVALID_STORE_NAME);
        }
    }

}
