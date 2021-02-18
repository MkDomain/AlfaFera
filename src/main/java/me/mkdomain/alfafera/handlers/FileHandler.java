package me.mkdomain.alfafera.handlers;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.mkdomain.alfafera.Main;
import me.mkdomain.alfafera.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Lehetővé teszi statikus file-ok egyszerű kezelését,
 * a cache csak akkor használt ha be van kapcsolva
 * (A kliensnek is küld egy órás cache header-t)
 */
public class FileHandler implements Handler {

    private final byte[] cache;
    private final String name;
    private final boolean useCache;

    /**
     * @param name     A fájl relatív helye az alkalmazás indulásának helyéhez képest
     * @param useCache Beállítja, hogy használjon-e cache-t a válaszoknál
     */
    public FileHandler(String name, boolean useCache) {
        byte[] cache1 = new byte[0];
        this.name = name;
        this.useCache = useCache;
        if (useCache) {
            try {
                cache1 = Files.readAllBytes(Paths.get(name));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cache = cache1;
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        if (useCache) {
            ctx.contentType(Files.probeContentType(Paths.get(name)) + "; charset=utf-8");
            Main.getManager().incrementCachedData(cache.length);
            try {
                if (Files.probeContentType(Paths.get(name)).equalsIgnoreCase("text/html")) {
                    ctx.result(Placeholder.replace(new String(cache)));
                    return;
                }
            } catch (Exception ignored) {
            }
            ctx.header("Cache-Control", "public");
            ctx.header("Cache-Control", "immutable");
            ctx.header("Cache-Control", "max-age=3600");
            ctx.result(cache);
        } else {
            final byte[] file = Files.readAllBytes(Paths.get(name));
            ctx.contentType(Files.probeContentType(Paths.get(name)) + "; charset=utf-8");
            try {
                if (Files.probeContentType(Paths.get(name)).equalsIgnoreCase("text/html")) {
                    ctx.result(Placeholder.replace(new String(file)));
                    return;
                }
            } catch (Exception ignored) {
            }
            ctx.result(file);
        }
    }
}
