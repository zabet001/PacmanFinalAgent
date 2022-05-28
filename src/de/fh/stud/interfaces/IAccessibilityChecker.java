package de.fh.stud.interfaces;

import de.fh.stud.Suchen.Suchkomponenten.Knoten;

public interface IAccessibilityChecker {
    boolean isAccessible(Knoten node, byte newPosX, byte newPosY);
}
