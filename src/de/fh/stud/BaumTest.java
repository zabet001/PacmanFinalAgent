package de.fh.stud;

import de.fh.pacman.enums.PacmanTileType;

public class BaumTest {

    public static void main(String[] args) {
        //Anfangszustand nach Aufgabe
        PacmanTileType[][] view = {
                {PacmanTileType.WALL, PacmanTileType.WALL, PacmanTileType.WALL,PacmanTileType.WALL},
                {PacmanTileType.WALL, PacmanTileType.EMPTY, PacmanTileType.DOT, PacmanTileType.WALL},
                {PacmanTileType.WALL, PacmanTileType.DOT, PacmanTileType.WALL, PacmanTileType.WALL},
                {PacmanTileType.WALL, PacmanTileType.WALL, PacmanTileType.WALL, PacmanTileType.WALL}};

        //Startposition des Pacman
        int posX = 1, posY = 1;
        /*
         * TODO Praktikum 2 [3]: Baut hier basierend auf dem gegebenen
         * Anfangszustand (siehe view, posX und posY) den Suchbaum auf.
         */


        // Um Code laufen zu lassen: Attribute auf public umaendern
/**     Suche.ACCESS_CHECK = Zugangsfilter.noWall();
        Suche.HEURISTIC_FUNC = node -> 0;

        List<Knoten> expChildren = new LinkedList<>();
        Knoten expCand;
        expChildren.add(Knoten.generateRoot(view, posX, posY));

        for (int i = 0; i < 10; i++) {
            expCand = expChildren.remove(0);
            Util.printView(MyUtil.reformatToTileType(expCand.getView()));
            expCand.expand().forEach(child -> expChildren.add( child));
        }*/
    }
}
