package com.bbangle.bbangle.preference.dto;

import com.bbangle.bbangle.preference.domain.PreferenceType;

public record PreferenceSelectRequest (
    PreferenceType preferenceType
){

}
