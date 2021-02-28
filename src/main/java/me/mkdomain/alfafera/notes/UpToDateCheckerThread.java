package me.mkdomain.alfafera.notes;

import me.mkdomain.alfafera.Main;
import me.mkdomain.alfafera.stats.NoteStatistics;
import me.mkdomain.alfafera.utils.Utils;

import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Biztosítja, hogy a jegyzetek naprakészek
 */
public class UpToDateCheckerThread extends Thread {

    @Override
    public void run() {
        try {
            for (Note note : Main.getNotes()) {
                final byte[] local = NoteHolder.getNote(note.getId(), note.getFile().getFileName().toString());
                final byte[] remote = Utils.downloadFromURL(new URL(note.getLink()));
                final String localHash = Utils.sha512(local);
                final String remoteHash = Utils.sha512(remote);
                //Ha nem egyezik a hash akkor van új jegyzet
                if (!localHash.equals(remoteHash)) {
                    System.out.println("A szerveren új jegyzet található! (" + note.name() + ")");
                    NoteHolder.addNote(note.getId(), remote, note.getFile().getFileName().toString());
                    note.updateImage();
                    NoteStatistics.similarity(note, true, new AtomicInteger());
                    System.out.println("Jegyzet frissítve!");
                }
                Thread.sleep(900);
            }
            Thread.sleep(3600000);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
