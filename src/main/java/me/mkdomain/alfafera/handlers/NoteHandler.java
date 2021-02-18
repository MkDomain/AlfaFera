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
        String in = new String(Files.readAllBytes(Paths.get("html/jegyzet.html")));
        in = Placeholder.replace(in);

        //Jegyzet megkeresése
        Optional<Note> optionalJegyzet = Main.getNotes().stream().filter(e -> e.getFile().getFileName().toString().equalsIgnoreCase(ctx.pathParam("name"))).findFirst();

        if (!optionalJegyzet.isPresent()) {
            //A jegyzet nem létezik (404-es kód)
            ctx.status(404);
            return;
        }

        Note note = optionalJegyzet.get();

        //Jegyzet adatainak betöltése
        in = in.replaceAll("%name%", note.name());
        in = in.replaceAll("%categories%", note.categories());
        in = in.replaceAll("%local%", note.getFile().getFileName().toString());

        //Népszerűség: pont hozzáadása
        NoteStatistics.addPoints(note.name(), Utils.getIp(ctx));

        //Statisztikák betöltése
        in = in.replace("%megtekintesek%", String.valueOf(NoteStatistics.getViews(note.name())));
        in = in.replace("%letoltesek%", String.valueOf(NoteStatistics.getDownloads(note.name())));
        in = in.replace("%osszpont%", String.valueOf(NoteStatistics.getPoints(note.name())));
        in = in.replace("%nsza_pont%", String.valueOf(Popularity.getPont(note.name())));


        //Hasonló jegyzetek mutatása
        List<NearestNeighborsResult<Note>> nearest = Popularity.getNearestNeighbors(note);

        in = in.replace("%local_1%", nearest.get(0).getResult().getFile().getFileName().toString());
        in = in.replace("%local_2%", nearest.get(1).getResult().getFile().getFileName().toString());
        in = in.replace("%local_3%", nearest.get(2).getResult().getFile().getFileName().toString());

        in = in.replace("%note_1%", nearest.get(0).getResult().name());
        in = in.replace("%note_2%", nearest.get(1).getResult().name());
        in = in.replace("%note_3%", nearest.get(2).getResult().name());

        in = in.replace("%distance_1%", Double.toString(nearest.get(0).getDistance()));
        in = in.replace("%distance_2%", Double.toString(nearest.get(1).getDistance()));
        in = in.replace("%distance_3%", Double.toString(nearest.get(2).getDistance()));

        ctx.contentType("text/html; charset=utf-8");
        ctx.result(in);
    }
}
