package me.mkdomain.alfafera.handlers;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.mkdomain.alfafera.placeholders.Placeholder;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A főoldalt tölti be, nem használ cache-t
 */
public class IndexHandler implements Handler {

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        ctx.contentType("text/html; charset=utf-8");
        final String def = Placeholder.replace(new String(Files.readAllBytes(Paths.get("html/index.html"))));
        ctx.result(def);
    }
}
