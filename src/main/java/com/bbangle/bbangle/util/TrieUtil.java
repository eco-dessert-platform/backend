package com.bbangle.bbangle.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class TrieUtil {
    private mNode root;

    public TrieUtil() {
        this.root = new mNode("");
    }

    public void insert(String string) {
        mNode currentMNode = this.root;

        for (char ch : string.toCharArray()) {
            if (!currentMNode.getChildren().containsKey(ch)) {
                currentMNode.getChildren().put(ch, new mNode(currentMNode.getValue() + ch));
            }

            currentMNode = currentMNode.getChildren().get(ch);
        }

        currentMNode.setWordExists(true);
    }

    public mNode find(String string) {
        mNode currentMNode = this.root;

        for (char ch : string.toCharArray()) {
            if (!currentMNode.getChildren().containsKey(ch)) {
                return null;
            }
            currentMNode = currentMNode.getChildren().get(ch);
        }

        return currentMNode;
    }

    public boolean has(String string) {
        return find(string) != null;
    }

    public List<String> autoComplete(String prefix, int limit) {
        mNode prefixMNode = find(prefix);
        if (prefixMNode == null || prefixMNode.getChildren().isEmpty()) {
            return Arrays.asList();
        }

        mQueue queue = new mQueue();
        queue.enqueue(prefixMNode);

        LinkedList<String> words = new LinkedList<>();
        while (!queue.isEmpty()) {
            mNode currentMNode = (mNode) queue.dequeue();
            if (currentMNode.isWordExists()) {
                limit--;

                words.add(currentMNode.getValue());

                if (limit == 0){
                    return words;
                }
            }

            for (mNode child : currentMNode.getChildren().values()) {
                queue.enqueue(child);
            }
        }

        return words;
    }
}