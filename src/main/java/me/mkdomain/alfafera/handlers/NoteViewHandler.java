package me.mkdomain.alfafera.handlers;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.mkdomain.alfafera.Main;
import me.mkdomain.alfafera.notes.Note;
import me.mkdomain.alfafera.notes.NoteHolder;
import org.jetbrains.annotations.NotNull;

/**
 * A jegyzetek megtekintésére szolgál, nem használ cache-t
 */
public class NoteViewHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        ctx.contentType("application/pdf");
        final Note note = Main.getNotes().stream().filter(e -> e.getFile().getFileName().toString().equalsIgnoreCase(ctx.pathParam("name"))).findFirst().get();
        ctx.result(NoteHolder.getNote(note.getId(),note.getFile().getFileName().toString()));
    }
}
