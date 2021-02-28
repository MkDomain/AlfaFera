package me.mkdomain.alfafera.handlers;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import me.mkdomain.alfafera.Main;
import me.mkdomain.alfafera.notes.Note;
import me.mkdomain.alfafera.placeholders.Placeholder;
import me.mkdomain.alfafera.stats.NoteStatistics;
import me.mkdomain.alfafera.stats.popularity.NearestNeighborsResult;
import me.mkdomain.alfafera.stats.popularity.Popularity;
import me.mkdomain.alfafera.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Egy adott jegyzetet tölt be, nem használ cache-t
 */
public class NoteHandler implements Handler {

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        //Jegyzet megkeresése
        Optional<Note> optionalJegyzet = Main.getNotes().stream().filter(e -> e.getFile().getFileName().toString().equalsIgnoreCase(ctx.pathParam("name"))).findFirst();
        if (!optionalJegyzet.isPresent()) {
            //A jegyzet nem létezik (404-es kód)
            ctx.status(404);
            return;
        }
        Note note = optionalJegyzet.get();

        //Népszerűség: pont hozzáadása
        NoteStatistics.addPoints(note.name(), Utils.getIp(ctx));

        //Hasonló jegyzetek mutatása
        List<NearestNeighborsResult<Note>> nearest = Popularity.getNearestNeighbors(note);

        final String in = Placeholder.replace(new String(Files.readAllBytes(Paths.get("html/jegyzet.html"))))
                //Jegyzet adatainak betöltése
                .replaceAll("%name%", note.name())
                .replaceAll("%categories%", note.categories())
                .replaceAll("%local%", note.getFile().getFileName().toString())
                //Statisztikák betöltése
                .replace("%megtekintesek%", String.valueOf(NoteStatistics.getViews(note.name())))
                .replace("%letoltesek%", String.valueOf(NoteStatistics.getDownloads(note.name())))
                .replace("%osszpont%", String.valueOf(NoteStatistics.getPoints(note.name())))
                .replace("%nsza_pont%", String.valueOf(Popularity.getPont(note.name())))

                //Hasonló jegyzetek
                .replace("%local_1%", nearest.get(0).getResult().getFile().getFileName().toString())
                .replace("%local_2%", nearest.get(1).getResult().getFile().getFileName().toString())
                .replace("%local_3%", nearest.get(2).getResult().getFile().getFileName().toString())

                .replace("%note_1%", nearest.get(0).getResult().name())
                .replace("%note_2%", nearest.get(1).getResult().name())
                .replace("%note_3%", nearest.get(2).getResult().name())

                .replace("%distance_1%", Double.toString(nearest.get(0).getDistance()))
                .replace("%distance_2%", Double.toString(nearest.get(1).getDistance()))
                .replace("%distance_3%", Double.toString(nearest.get(2).getDistance()));

        ctx.contentType("text/html; charset=utf-8");
        ctx.result(in);
    }
}
