package com.bbangle.bbangle.common.domain;

import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@MappedSuperclass
public class BaseEntity extends CreatedAtBaseEntity {

    @UpdateTimestamp
    private LocalDateTime modifiedAt;

}

