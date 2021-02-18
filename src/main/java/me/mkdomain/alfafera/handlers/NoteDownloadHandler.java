package me.mkdomain.alfafera.handlers;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.mkdomain.alfafera.Main;
import me.mkdomain.alfafera.notes.Note;
import me.mkdomain.alfafera.stats.NoteStatistics;
import me.mkdomain.alfafera.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;

/**
 * A jegyzetek letöltését biztosítja, nem használ cache-t
 */
public class NoteDownloadHandler implements Handler {
    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        Note note = Main.getNotes().stream().filter(e -> e.getFile().getFileName().toString().equalsIgnoreCase(ctx.pathParam("name"))).findFirst().get();
        ctx.header("Content-Description", " File Transfer");
        ctx.header("Content-Disposition", " attachment; filename=" + note.getFile().getFileName().toString());
        ctx.header("Content-Transfer-Encoding", " binary");
        ctx.header("Expires", " 0");
        ctx.header("Cache-Control", " must-revalidate'");
        ctx.header("Pragma", " public");
        ctx.header("Content-Type", " application/octet-stream");
        NoteStatistics.addDownload(note.name(), Utils.getIp(ctx));
        ctx.result(Files.readAllBytes(note.getFile()));
    }
}
