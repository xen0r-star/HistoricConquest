package com.historicconquest.historicconquest.model.map;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZonePathfinderTest {

    @Test
    void findPathReturnsBoatWhenRouteUsesBoatAdjacency() {
        Zone start = new Zone("Start", "Bloc", 0, 0, 0, Color.WHITE, Color.BLACK);
        Zone middle = new Zone("Middle", "Bloc", 0, 0, 0, Color.WHITE, Color.BLACK);
        Zone end = new Zone("End", "Bloc", 0, 0, 0, Color.WHITE, Color.BLACK);

        start.addAdjacentBoatZone(middle);
        middle.addAdjacentBoatZone(end);

        ZonePathfinder.PathResult result = ZonePathfinder.findPath(start, end);

        assertEquals(ZonePathfinder.PathType.BOAT, result.type());
        assertEquals(List.of(start, middle, end), result.zones());
    }

    @Test
    void worldMapLoadsOceanAdjacencySeparatelyFromLandAdjacency() {
        WorldMap worldMap = new WorldMap();
        Zone greenland = findZone(worldMap.getAllZones(), "Greenland");
        Zone canada = findZone(worldMap.getAllZones(), "Canada");

        assertNotNull(greenland);
        assertNotNull(canada);
        assertTrue(greenland.getAdjacentZones().isEmpty(), "Greenland should not have land adjacency");
        assertTrue(greenland.getAdjacentBoatZones().contains(canada), "Greenland should be boat-adjacent to Canada through the Atlantic Ocean");
    }

    private static Zone findZone(List<Zone> zones, String name) {
        for (Zone zone : zones) {
            if (name.equals(zone.getName())) {
                return zone;
            }
        }
        return null;
    }
}

