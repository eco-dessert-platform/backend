package com.bbangle.bbangle.common.domain;

import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
@Getter
@MappedSuperclass
public class CreatedAtBaseEntity {

    @CreationTimestamp
    private LocalDateTime createdAt;
}

