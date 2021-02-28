package me.mkdomain.alfafera.configuration;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Konfiguráció miatt van rá szükség
 */
public interface Configuration {

    void set(String key, String value);
    String get(String key);
    boolean contains(String key);

    void save(Path path) throws IOException;
    void load(Path path) throws IOException;

}
