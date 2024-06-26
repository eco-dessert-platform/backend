package com.bbangle.bbangle.search.service.utils;

import com.bbangle.bbangle.util.CharacterNode;
import com.bbangle.bbangle.util.CustomQueue;
import java.util.LinkedList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AutoCompleteUtil {

    private final CharacterNode root = new CharacterNode("");

    public void insert(String string) {
        CharacterNode currentNode = this.root;

        for (char character : string.toCharArray()) {
            if (!currentNode.getChildren().containsKey(character)) {
                currentNode.getChildren()
                    .put(character, new CharacterNode(currentNode.getValue() + character));
            }

            currentNode = currentNode.getChildren().get(character);
        }

        currentNode.setWordExists(true);
    }

    public void insertAll(List<String> titles) {
        for (String title : titles) {
            insert(title);
        }
    }

    public CharacterNode find(String string) {
        CharacterNode currentNode = this.root;

        for (char character : string.toCharArray()) {
            if (!currentNode.getChildren().containsKey(character)) {
                return null;
            }
            currentNode = currentNode.getChildren().get(character);
        }

        return currentNode;
    }

    public List<String> autoComplete(String prefix, int limit) {
        CharacterNode prefixNode = find(prefix);
        if (prefixNode == null || prefixNode.getChildren().isEmpty()) {
            return List.of();
        }

        CustomQueue queue = new CustomQueue();
        queue.enqueue(prefixNode);

        LinkedList<String> words = new LinkedList<>();
        while (!queue.isEmpty()) {
            CharacterNode currentNode = (CharacterNode) queue.dequeue();
            if (currentNode.isWordExists()) {
                limit--;

                words.add(currentNode.getValue());

                if (limit == 0) {
                    return words;
                }
            }

            for (CharacterNode child : currentNode.getChildren()
                .values()) {
                queue.enqueue(child);
            }
        }

        return words;
    }

}
