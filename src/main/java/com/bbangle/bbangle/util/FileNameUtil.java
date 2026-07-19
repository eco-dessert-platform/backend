package com.bbangle.bbangle.util;

import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileNameUtil {

    public static String resolveUniquePath(String path, Set<String> usedPaths) {
        if (!usedPaths.contains(path)) {
            return path;
        }

        String baseName = path.substring(0, path.lastIndexOf('.'));
        String extension = path.substring(path.lastIndexOf('.'));

        int suffix = 2;
        String candidate;
        do {
            candidate = baseName + "(" + suffix + ")" + extension;
            suffix++;
        } while (usedPaths.contains(candidate));

        return candidate;
    }
}
