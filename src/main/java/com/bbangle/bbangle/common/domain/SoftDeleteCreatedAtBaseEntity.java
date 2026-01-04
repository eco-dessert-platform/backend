package com.bbangle.bbangle.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@Getter
@MappedSuperclass
public abstract class SoftDeleteCreatedAtBaseEntity extends CreatedAtBaseEntity {

    @Column(name = "is_deleted", nullable = false, columnDefinition = "tinyint")
    private boolean isDeleted = false;

    public void restore() {
        this.isDeleted = false;
    }

    public void delete() {
        this.isDeleted = true;
    }

}
