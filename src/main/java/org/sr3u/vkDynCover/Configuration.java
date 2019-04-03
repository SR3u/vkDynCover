package org.sr3u.vkDynCover;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Getter
public class Configuration {
    public static final String DUMMY = "#!/usr/bin/env vkDynCover\n" +
            "##################################################################################################################\n" +
            "# vkDynCover configuration file\n" +
            "# Syntax is: each line starting with # is a comment\n" +
            "# Each other line is a group id, token and refresh interval (in seconds) separated by any non-word character (\\W or [^a-zA-Z0-9_] regexp)\n" +
            "# If refresh rate is unspecified, default value of 60 seconds is used\n" +
            "# If line has more than 3 words, only first 3 are used.\n" +
            "# If line has less than 2 words, it's invalid and ignored.\n" +
            "# Then add desired covers in images/<groupId> folder, and enjoy.\n" +
            "# Empty lines are ignored\n" +
            "##################################################################################################################\n" +
            "# Start by adding your groups here.\n" +
            "# GROUP_ID TOKEN                                                                                 REFRESH_DELAY_SEC\n";
    private Collection<Group> groups = new ArrayList<>();

    public Configuration(String path) throws IOException {
        this.parse(path);
    }

    private void parse(String path) throws IOException {
        AtomicInteger index = new AtomicInteger();
        groups = Files.lines(Paths.get(path))
                .map(line -> Pair.of(index.incrementAndGet(), line))
                .filter(pair -> !pair.getSecond().isEmpty())
                .filter(pair -> !pair.getSecond().startsWith("#"))
                .map(Group::new)
                .collect(Collectors.toList());
    }

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class Group {
        public static final String IMAGES_DIR = System.getProperty("user.dir") + "/images/";
        private int refreshInterval = 60;
        private String id;
        private String token;

        public Group(Pair<Integer, String> pair) {
            this(pair.second, pair.first);
        }

        public Group(String line, int lineNumber) {
            List<String> split = Arrays.stream(line.split("\\W+")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            if (split.size() < 2) {
                System.out.println("Invalid line " + lineNumber + ": " + line);
                System.out.println("Check configuration.txt file");
                System.exit(-2);
            }
            if (split.size() >= 3) {
                try {
                    this.refreshInterval = Integer.parseInt(split.get(2));
                } catch (Throwable t) {
                    System.out.println("Invalid line " + lineNumber + ": " + line);
                    System.out.println("Check configuration.txt file");
                }
            }
            id = split.get(0);
            token = split.get(1);
        }

        public String getImagesDirectory() {
            return IMAGES_DIR + id;
        }
    }

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    private static class Pair<F, S> {
        private F first;
        private S second;

        public static <F, S> Pair<F, S> of(F first, S second) {
            return new Pair<>(first, second);
        }
    }
}
