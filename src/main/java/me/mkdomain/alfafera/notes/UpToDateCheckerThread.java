package me.mkdomain.alfafera.notes;

import me.mkdomain.alfafera.Main;
import me.mkdomain.alfafera.stats.NoteStatistics;
import me.mkdomain.alfafera.utils.Utils;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * Biztosítja, hogy a jegyzetek naprakészek
 */
public class UpToDateCheckerThread extends Thread {

    @Override
    public void run() {
        try {
            for (Note note : Main.getNotes()) {
                byte[] local = Files.readAllBytes(note.getFile());
                byte[] remote = Utils.downloadFromURL(new URL(note.getLink()));
                String localHash = Utils.md5(local);
                String remoteHash = Utils.md5(remote);
                //Ha nem egyezik a hash akkor van új jegyzet
                if (!localHash.equals(remoteHash)) {
                    System.out.println("A szerveren új jegyzet található! (" + note.name() + ")");
                    Files.write(note.getFile(), remote, StandardOpenOption.WRITE);
                    note.updateImage();
                    NoteStatistics.similarity(note, true);
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
