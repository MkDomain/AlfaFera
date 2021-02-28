package me.mkdomain.alfafera.configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Egy egyszerű konfiguráció implementáció
 * hasonlóan ment, mint a {@link java.util.Properties}
 */
public class BasicConfiguration implements Configuration{

    private final HashMap<String, String> values = new HashMap<>();

    @Override
    public void set(String key, String value) {
        values.put(key, value);
    }

    @Override
    public String get(String key) {
        return values.get(key);
    }

    @Override
    public boolean contains(String key) {
        return values.containsKey(key);
    }

    @Override
    public void save(Path path) throws IOException {
        String text = values.entrySet().stream().map(e -> e.getKey() + "= " + e.getValue()).collect(Collectors.joining("\n"));
        Files.write(path, text.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
    }

    @Override
    public void load(Path path) throws IOException {
        if (!Files.exists(path)) return;
        Files.readAllLines(path, StandardCharsets.UTF_8).forEach(e -> values.put(e.split("= ")[0], e.split("= ")[1]));
    }
}
