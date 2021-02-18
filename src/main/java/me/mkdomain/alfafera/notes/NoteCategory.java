package me.mkdomain.alfafera.notes;

import java.util.Arrays;

/**
 * A jegyzet kategóriájához szükséges enum
 */
public enum NoteCategory {

    ALAP_SZINT("Alapszint"),
    EMELT_SZINT("Emeltszint"),
    VIRUSOK_PROKARIOTAK("Vírusok, prokarióták", "1.1 Bevezetés, vírusok, prokarióták"),
    GOMBAK_ZUZMOK("Gombák, zúzmók", "1.2 Gombák, zuzmók"),
    NOVENYTAN("Növénytan", "1.3 Növénytan"),
    ALLATTAN("Állattan", "1.4 Állattan"),
    ETOLOGIA("Etológia", "2. ETOLÓGIA"),
    OKOLOGIA("Ökológia", "3. ÖKOLÓGIA"),
    SEJTBIOLOGIA("Sejtbiológia", "4.1 A sejtek kémiai felépítése", "4.2 A sejtek anyagcsere-folyamatai", "4.3 A sejtek felépítése, működése"),
    EMBER_ONFENNTARTO("Az ember önfenntartó működései", "6.1 Az ember emésztőszervrendszere", "6.2 Az ember légzőszervrendszere", "6.3 Az emberi keringés szervrendszere", "6.4 Az ember kiválasztó-szervrendszere", "6.5 A mozgás szervrendszere", "6.6 Az emberi bőr"),
    EMBER_ONSZABALYZO("Az ember önszabályzó működései", "7. AZ EMBER ÖNSZABÁLYOZÓ MŰKÖDÉSEI", "NINCS", "Idegi szabályozás", "Az érzékszervek felépítése és működése", "Genetikai szabályozás"),
    ONREPRODUKCIO("Önreprudukció", "8. ÖNREPRODUKCIÓ"),
    EVOLUCIO("Evolúció", "9. AZ EVOLÚCIÓ"),
    ALLATI_ES_EMBERI_SZOVETEK("Állati és emberi szövetek", "5. AZ ÁLLATI ÉS EMBERI SZÖVETEK"),

    BIOLOGIA("A biológiai kutatás céljai és módszerei", "1. A biológiai kutatás céljai és módszerei"),
    ELET_EREDET_SZERVEZODES("Az élet eredete és szerveződése", "2. Az élet eredete és szerveződése"),
    OROKLODES_EVOLUCIO("Öröklődés és evolúció, a biotechnológia módszerei és alkalmazása", "3. Öröklődés és evolúció, a biotechnológia módszerei és alkalmazása"),
    SZERVEZET_EGESZSEG("Az ember szervezete és egészsége", "4. Az ember szervezete és egészsége");

    private final String name;
    private final String[] siteNames;

    /**
     * @param name      Emberek által olvasható név
     * @param siteNames Az eredeti oldalon található név, az azonosítás miatt van rá szükség
     */
    NoteCategory(String name, String... siteNames) {
        this.name = name;
        this.siteNames = siteNames;
    }

    public static NoteCategory getBySiteName(String name) {
        return Arrays.stream(values()).filter(e -> Arrays.stream(e.getSiteName()).anyMatch(e1 -> e1.equalsIgnoreCase(name))).findFirst().get();
    }

    public String[] getSiteName() {
        return siteNames;
    }

    public String getName() {
        return name;
    }
}
