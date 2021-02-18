package me.mkdomain.alfafera.handlers;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.mkdomain.alfafera.Main;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Az "assets" mappában található fájlokat tölti be
 * Maximális cache-t használ, a kliensnek is egy órás
 * cache header-t küld
 */
public class AssetsHandler implements Handler {

    private static final HashMap<String, Wrapper> cache = new HashMap<>();

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        if (cache.containsKey(ctx.splat(0))) {
            ctx.contentType(cache.get(ctx.splat(0)).type + "; charset=utf-8");
            final byte[] res = cache.get(ctx.splat(0)).value;
            Main.getManager().incrementCachedData(res.length);
            ctx.result(res);
            return;
        }
        Path path = Paths.get("html/assets/" + ctx.splat(0));
        ctx.contentType(Files.probeContentType(path) + "; charset=utf-8");
        final byte[] res = Files.readAllBytes(path);
        cache.put(ctx.splat(0), new Wrapper(res, Files.probeContentType(path)));
        ctx.header("Cache-Control", "public");
        ctx.header("Cache-Control", "immutable");
        ctx.header("Cache-Control", "max-age=3600");
        ctx.result(res);
    }

    /**
     * A megadott asset-et, és annak típusát tároló osztály
     */
    static class Wrapper {

        byte[] value;
        String type;

        public Wrapper(byte[] value, String type) {
            this.value = value;
            this.type = type;
        }
    }

}
