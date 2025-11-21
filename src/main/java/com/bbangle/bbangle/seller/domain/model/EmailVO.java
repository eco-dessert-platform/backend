package com.bbangle.bbangle.seller.domain.model;

import com.bbangle.bbangle.exception.BbangleErrorCode;
import com.bbangle.bbangle.exception.BbangleException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVO {
    private static final Pattern PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Column(name = "email",  columnDefinition = "VARCHAR(100)")
    private  String email;

    private EmailVO(String email) {
        validate(email);
        this.email = email;
    }

    public static EmailVO of(String email) {
        return new EmailVO(email);
    }

    private void validate(String email) {
        if (email == null || !PATTERN.matcher(email).matches()) {
            throw new BbangleException(BbangleErrorCode.INVALID_EMAIL);
        }
    }

}
