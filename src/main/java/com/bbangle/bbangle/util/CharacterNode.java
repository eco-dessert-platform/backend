package com.bbangle.bbangle.util;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public class CharacterNode {

    private String value;
    private Map<Character, CharacterNode> children = new HashMap<>();
    private boolean isWordExists = false;

    public CharacterNode(String value) {
        this.value = value;
    }

}
