package com.bbangle.bbangle.preference.config;

import com.bbangle.bbangle.preference.domain.Preference;
import com.bbangle.bbangle.preference.domain.PreferenceType;
import com.bbangle.bbangle.preference.repository.PreferenceRepository;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PreferenceConfig {

    private final PreferenceRepository preferenceRepository;

    @PostConstruct
    public void setupPreference() {
        List<PreferenceType> existPreference = preferenceRepository.findAll()
            .stream()
            .map(Preference::getPreferenceType)
            .toList();
        List<Preference> saveList = new ArrayList<>();
        Arrays.stream(PreferenceType.values())
            .filter(preference -> !existPreference.contains(preference))
            .forEach(preferenceType -> {
                saveList.add(new Preference(preferenceType));
            });
        if (!saveList.isEmpty()) {
            preferenceRepository.saveAll(saveList);
        }
    }

}
