package com.bbangle.bbangle.member.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class RoleConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role role) {
        return role.getRole();
    }

    @Override
    public Role convertToEntityAttribute(String dbData) {
        return Role.from(dbData);
    }

}
