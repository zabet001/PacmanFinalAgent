package de.fh.stud.Suchen;

import de.fh.kiServer.util.Vector2;
import de.fh.pacman.enums.PacmanTileType;
import de.fh.stud.Knoten;
import de.fh.stud.MyUtil;
import de.fh.stud.Suchen.Suchfunktionen.CallbackFunktionen;
import de.fh.stud.Suchen.Suchfunktionen.Zugangsfilter;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;

public class Sackgassen {

    // TODO: Einzelne Sackgassen abspeichern mit ihren Dots:
    //  Waehrend des Spiels Sackgassen komplett schliessen, wenn keine Dots mehr in ihr liegen -> Performancesteigerung

    // Hinweis: Wenn die Map ohne Zyklen ist, wird eine Sackgasse nicht als solche erkannt
    public static byte[][] deadEndDepth;

    public static void initDeadEndDepth(PacmanTileType[][] world) {
        deadEndDepth = new byte[world.length][world[0].length];

        // Schritt 1: Alle Sackgassenenden ausfindig machen
        /** Tupel: (Sackgassenende,Vorgaenger), um spaterer das erste Feld in der Sackgasse wiederzufinden*/
        List<AbstractMap.SimpleEntry<Vector2, Vector2>> oneWays = oneWayEndsFirstOrder(world);

        /* Schritt 2: Fuer alle Sackgassen: Anfangspos suchen und fuer Endpos ersetzen, dabei CALLBACK: ALLE
        besuchten Felder markieren mit cost -1
         -> Sackgassen letzter Stufe werden "temporaer geschlossen": mehrstufige sackgassen werden einstufig*/
        for (int i = 0; i < oneWays.size(); i++) {
            AbstractMap.SimpleEntry<Vector2, Vector2> oneWayStartTuple = locateStartOfOneWay(world,
                    oneWays.get(i).getKey().x, oneWays.get(i).getKey().y);
            if (oneWayStartTuple != null) {
                oneWays.add(i, oneWayStartTuple);
                oneWays.remove(i + 1);
            } else
                oneWays.remove(i--);
        }

        /* Schritt 3: Suche starten bei Vorgaenger der Sackgassenenden, die immer noch 0 als Kosten besitzen
        (alle Sackgassen mit Kosten -1 sind Teil einer tieferen Sackgasse: deren Tiefe wird automatisch aktualisiert)
        ACCESSIBILITY_CHECKER: das jeweilige Feld VOR Sackgasse als Wand betrachten
        CALLBACK: Kosten fuer das Feld abspeichern*/
        for (AbstractMap.SimpleEntry<Vector2, Vector2> oneWayEntry : oneWays) {
            // Wenn deadEndDepth[sackgassenende.x][sackgassenende.y] != 0: Muss nicht beruecksichtigt werden
            if (deadEndDepth[oneWayEntry.getKey().x][oneWayEntry.getKey().y] == 0) {
                writeOneWayDepth(world, oneWayEntry.getValue(), oneWayEntry.getKey());
            }
        }
    }

    public static void printOneWayDepthMap(PacmanTileType[][] world) {
        System.out.println();

        for (int i = 0; i < deadEndDepth[0].length; i++) {
            for (int j = 0; j < deadEndDepth.length; j++) {
                System.out.printf("%2s ", world[j][i] == PacmanTileType.WALL ? "[]" : deadEndDepth[j][i]);
            }
            System.out.println();
        }

        System.out.println();
    }

    /**
     @return - Tupel(Sackgassenende, Vorgaenger Sackgassenende) -> value Vorgaenger Sackgassenende = null, da Sackgasse
     erster Ordnung
     */
    private static List<AbstractMap.SimpleEntry<Vector2, Vector2>> oneWayEndsFirstOrder(PacmanTileType[][] world) {
        List<AbstractMap.SimpleEntry<Vector2, Vector2>> ret = new LinkedList<>();
        for (int i = 0; i < world.length; i++) {
            for (int j = 0; j < world[0].length; j++) {
                if (world[i][j] != PacmanTileType.WALL && MyUtil.adjacentFreeFieldsCnt(Zugangsfilter.noWall(), world,
                        i, j) < 2) {
                    ret.add(new AbstractMap.SimpleEntry<>(new Vector2(i, j), null));
                }
            }
        }
        return ret;
    }

    /**
     @return - Tupel(Sackgassenende, Sackgassenanfang von Sackgasse naechster Tiefe)
     */
    private static AbstractMap.SimpleEntry<Vector2, Vector2> locateStartOfOneWay(PacmanTileType[][] world, int posX,
                                                                                 int posY) {

        Suche oneWayStartSearch = new Suche(Suchszenario.locateDeadEndExit(deadEndDepth),
                CallbackFunktionen.setVisitedValue(deadEndDepth, (byte) -1));
        oneWayStartSearch.setNoWaitAction(true);

        Knoten oneWayStart = oneWayStartSearch.start(world, posX, posY, Suche.SearchStrategy.DEPTH_FIRST, false);

        if (oneWayStart == null)
            return null;
        return new AbstractMap.SimpleEntry<>(oneWayStart.getPosition(), oneWayStart.getPred().getPosition());
    }

    /**
     @param oneWayEntry - Eintrittspunkt - Beginn der Suche
     @param oneWayGate - Wird als Wand betrachtet, nicht hier expandieren
     */
    private static void writeOneWayDepth(PacmanTileType[][] world, Vector2 oneWayEntry, Vector2 oneWayGate) {
        Suche writeDepths = new Suche(false, Zugangsfilter.merge(Zugangsfilter.noWall(),
                Zugangsfilter.excludePositions(oneWayGate)), true, null, null,
                CallbackFunktionen.saveStepCost(deadEndDepth));
        writeDepths.start(world, oneWayEntry.x, oneWayEntry.y, Suche.SearchStrategy.DEPTH_FIRST, false);

    }
}
