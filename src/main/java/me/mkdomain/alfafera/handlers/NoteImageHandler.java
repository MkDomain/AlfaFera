package me.mkdomain.alfafera.handlers;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.mkdomain.alfafera.Main;
import org.jetbrains.annotations.NotNull;

/**
 * Jegyzetek képét tölti be, használ cache-t
 */
public class NoteImageHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) {
        ctx.contentType("image/jpeg");
        ctx.result(Main.getNotes().stream().filter(e -> e.getFile().getFileName().toString().equalsIgnoreCase(ctx.pathParam("name"))).findFirst().get().getImage());
    }
}
