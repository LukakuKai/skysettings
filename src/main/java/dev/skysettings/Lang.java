package dev.skysettings;

import java.util.HashMap;
import java.util.Map;

/** Einfache Uebersetzung: Deutsche Texte sind die Schluessel, Englisch kommt aus der Tabelle. */
public final class Lang {
	private static final Map<String, String> EN = new HashMap<>();

	private static void p(String de, String en) {
		EN.put(de, en);
	}

	static {
		// Tabs
		p("Allg.", "Gen.");
		p("Zeit", "Time");
		p("Himmel", "Sky");
		p("Wolken", "Clouds");
		p("Nebel", "Fog");
		// Allgemein
		p("Sky Settings aktiv", "Sky Settings enabled");
		p("Helligkeit", "Brightness");
		p("Sättigung", "Saturation");
		p("Sterne", "Stars");
		p("Eigene Uhrzeit", "Custom time");
		p("Tipp", "Tip");
		p("Fertige Himmel findest du unter \"Looks\".", "Ready-made skies are under \"Looks\".");
		p("Alles ist nur bei dir sichtbar.", "Everything is only visible to you.");
		// Zeit
		p("Uhrzeit", "Time of day");
		p("Morgen 06:00", "Morning 06:00");
		p("Mittag 12:00", "Noon 12:00");
		p("Abend 18:00", "Evening 18:00");
		p("Mitternacht 00:00", "Midnight 00:00");
		p("Tempo (0 = Uhrzeit steht still)", "Speed (0 = time stands still)");
		p("fest", "fixed");
		p("Mond", "Moon");
		p("Mondphase", "Moon phase");
		p("Vollmond", "Full moon");
		p("Abnehm. 3/4", "Waning 3/4");
		p("Letztes Viertel", "Last quarter");
		p("Abnehm. Sichel", "Waning crescent");
		p("Neumond", "New moon");
		p("Zunehm. Sichel", "Waxing crescent");
		p("Erstes Viertel", "First quarter");
		p("Zunehm. 3/4", "Waxing 3/4");
		// Farben
		p("Himmelsfarbe", "Sky color");
		p("Dämmerung (Sonnenauf-/untergang)", "Twilight (sunrise/sunset)");
		p("Wolkenfarbe", "Cloud color");
		p("Nebelfarbe", "Fog color");
		p("Stärke", "Strength");
		p("Rot", "Red");
		p("Grün", "Green");
		p("Blau", "Blue");
		p("Regenbogen", "Rainbow");
		p("Regenbogen-Tempo", "Rainbow speed");
		p("Deckkraft", "Opacity");
		p("Höhe", "Height");
		p("Dichte (0 = kein Nebel)", "Density (0 = no fog)");
		p("Eigener Nebel-Beginn", "Custom fog start");
		p("Nebel beginnt ab", "Fog starts at");
		p("Blöcke", "blocks");
		// Looks
		p("Vorlagen", "Presets");
		p("Meine Profile", "My profiles");
		p("Fertige Himmel – Klick zum Anwenden", "Ready-made skies – click to apply");
		p("Aktuellen Himmel speichern", "Save current sky");
		p("Profilname eingeben ...", "Enter profile name ...");
		p("Speichern", "Save");
		p("Gespeichert – Klick zum Laden", "Saved – click to load");
		p("Noch keine Profile gespeichert.", "No profiles saved yet.");
		p("Löschen", "Delete");
		p("Goldene Stunde", "Golden hour");
		p("Sonnenuntergang", "Sunset");
		p("Vollmondnacht", "Full moon night");
		p("Mitternachtsblau", "Midnight blue");
		p("Blutmond", "Blood moon");
		p("Alien-Lila", "Alien purple");
		p("Rosa Traum", "Pink dream");
		p("Nebelmorgen", "Foggy morning");
		p("Eiswelt", "Ice world");
		p("Giftgrün", "Toxic green");
		p("Wüstenglut", "Desert heat");
		p("Tiefsee", "Deep sea");
		p("Schwarz-Weiß", "Black & white");
		p("Regenbogen-Nacht", "Rainbow night");
		p("Galaxie", "Galaxy");
		p("Weltuntergang", "Doomsday");
		p("Zuckerwatte", "Cotton candy");
		p("Sonnenaufgang", "Sunrise");
		p("Gewitter", "Thunderstorm");
		p("Mars", "Mars");
		p("Ozeanabend", "Ocean evening");
		p("Neon-Nacht", "Neon night");
		// Design
		p("Akzentfarbe", "Accent color");
		p("Menü-Deckkraft", "Menu opacity");
		p("Menü-Helligkeit", "Menu brightness");
		p("Stil", "Style");
		p("Klassisch", "Classic");
		p("Holz", "Wood");
		p("Pergament", "Parchment");
		p("Ende", "End");
		p("Dunkel", "Dark");
		p("Hell", "Light");
		p("Glas", "Glass");
		p("Runde Ecken", "Rounded corners");
		p("Animationen", "Animations");
		p("Animations-Tempo", "Animation speed");
		p("Holzsorte", "Wood type");
		p("Eiche", "Oak");
		p("Fichte", "Spruce");
		p("Birke", "Birch");
		p("Dschungel", "Jungle");
		p("Akazie", "Acacia");
		p("Schwarzeiche", "Dark oak");
		p("Kirsche", "Cherry");
		p("Bambus", "Bamboo");
		p("Karmesin", "Crimson");
		p("Wirrwarr", "Warped");
		p("Sprache", "Language");
		// Fusszeile
		p("Design zurücksetzen", "Reset design");
		p("Himmel zurücksetzen", "Reset sky");
		p("Fertig", "Done");
	}

	private Lang() {
	}

	public static boolean english() {
		return "en".equals(SkyConfig.get().lang);
	}

	public static String tr(String de) {
		if (!english() || de == null) {
			return de;
		}
		String en = EN.get(de);
		return en != null ? en : de;
	}
}
