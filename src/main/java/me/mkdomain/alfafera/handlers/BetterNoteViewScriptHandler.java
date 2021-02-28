package me.mkdomain.alfafera.handlers;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.mkdomain.alfafera.Main;
import me.mkdomain.alfafera.notes.Note;
import me.mkdomain.alfafera.placeholders.Placeholder;
import me.mkdomain.alfafera.stats.NoteStatistics;
import me.mkdomain.alfafera.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Az újfajta jegyzetnézegető scriptje
 */
public class BetterNoteViewScriptHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        ctx.contentType("text/javascript; charset=utf-8");
        final Note note = Main.getNotes().stream().filter(e -> e.getFile().getFileName().toString().equalsIgnoreCase(ctx.pathParam("name"))).findFirst().get();
        NoteStatistics.addView(note.name(), Utils.getIp(ctx));
        final String def = Placeholder.replace(new String(Files.readAllBytes(Paths.get("html/viewing/viewer.js"))))
                .replaceAll("%local%", note.getFile().getFileName().toString());
        ctx.result(def);
    }
}
