package com.bbangle.bbangle.preference.customer.dto;

import com.bbangle.bbangle.preference.domain.PreferenceType;

public record PreferenceUpdateRequest(
    PreferenceType preferenceType
) {

}
