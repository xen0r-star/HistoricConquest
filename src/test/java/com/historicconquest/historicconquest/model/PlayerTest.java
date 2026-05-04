package com.historicconquest.historicconquest.model;

import com.historicconquest.historicconquest.model.map.Zone;
import com.historicconquest.historicconquest.model.player.Player;
import com.historicconquest.historicconquest.model.player.PlayerColor;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {
    private Player player;

    private static Zone makeZone(String name) {
        return new Zone(name, "BlocTest", 3, 0.0, 0.0,
                Color.web("#FFFFFF"), Color.web("#000000"));
    }

    @BeforeEach
    void setUp() {
        player = new Player(1, "Alice", PlayerColor.RED);
    }


    @Test
    void getPseudo_returnsConstructorValue() {
        assertEquals("Alice", player.getPseudo());
    }

    @Test
    void getId_returnsConstructorValue() {
        assertEquals(1, player.getId());
    }

    @Test
    void getColor_returnsConstructorValue() {
        assertEquals(PlayerColor.RED, player.getColor());
    }


    @Test
    void getConsecutiveSuccesses_initiallyZero() {
        assertEquals(0, player.getConsecutiveSuccesses());
    }

    @Test
    void setConsecutiveSuccesses_updatesValue() {
        player.setConsecutiveSuccesses(3);
        assertEquals(3, player.getConsecutiveSuccesses());
    }


    @Test
    void getConsecutiveFailures_initiallyZero() {
        assertEquals(0, player.getConsecutiveFailures());
    }

    @Test
    void setConsecutiveFailures_updatesValue() {
        player.setConsecutiveFailures(5);
        assertEquals(5, player.getConsecutiveFailures());
    }


    @Test
    void getCurrentZone_initiallyNull() {
        assertNull(player.getCurrentZone());
    }

    @Test
    void setCurrentZone_updatesValue() {
        Zone zone = makeZone("Paris");
        player.setCurrentZone(zone);
        assertSame(zone, player.getCurrentZone());
    }

    @Test
    void setCurrentZone_toNull_resetsValue() {
        player.setCurrentZone(makeZone("Paris"));
        player.setCurrentZone(null);
        assertNull(player.getCurrentZone());
    }



    @Test
    void getPawnNode_initiallyNull() {
        assertNull(player.getPawnNode());
    }


    @Test
    void getZones_initiallyEmpty() {
        assertNotNull(player.getZones());
        assertTrue(player.getZones().isEmpty());
    }

    @Test
    void addZone_addsToList() {
        Zone zone = makeZone("Rome");
        player.addZone(zone);
        assertEquals(1, player.getZones().size());
        assertTrue(player.getZones().contains(zone));
    }


    @Test
    void addZone_multipleZones_allPresent() {
        player.addZone(makeZone("Rome"));
        player.addZone(makeZone("Berlin"));
        assertEquals(2, player.getZones().size());
    }


    @Test
    void hasAlly_initiallyFalse() {
        assertFalse(player.hasAlly());
    }

    @Test
    void setAlly_thenHasAllyIsTrue() {
        player.setAlly(new Player(2, "Bob", PlayerColor.BLUE));
        assertTrue(player.hasAlly());
    }

    @Test
    void getAlly_returnsSetAlly() {
        Player ally = new Player(2, "Bob", PlayerColor.BLUE);
        player.setAlly(ally);
        assertSame(ally, player.getAlly());
    }


    @Test
    void setAlly_toNull_hasAllyReturnsFalse() {
        player.setAlly(new Player(2, "Bob", PlayerColor.BLUE));
        player.setAlly(null);
        assertFalse(player.hasAlly());
    }


    @Test
    void getPendingAllianceRequest_initiallyNull() {
        assertNull(player.getPendingAllianceRequest());
    }

    @Test
    void hasPendingRequest_initiallyFalse() {
        assertFalse(player.hasPendingRequest());
    }

    @Test
    void setPendingAllianceRequest_updatesValue() {
        Player requester = new Player(3, "Carol", PlayerColor.GREEN);
        player.setPendingAllianceRequest(requester);
        assertSame(requester, player.getPendingAllianceRequest());
    }

    @Test
    void hasPendingRequest_trueAfterSet() {
        player.setPendingAllianceRequest(new Player(3, "Carol", PlayerColor.GREEN));
        assertTrue(player.hasPendingRequest());
    }

    @Test
    void clearPendingRequest_removesRequest() {
        player.setPendingAllianceRequest(new Player(3, "Carol", PlayerColor.GREEN));
        player.clearPendingRequest();
        assertFalse(player.hasPendingRequest());
        assertNull(player.getPendingAllianceRequest());
    }


    @Test
    void getCurrentAllianceColor_initiallyNull() {
        assertNull(player.getCurrentAllianceColor());
    }

    @Test
    void setCurrentAllianceColor_updatesValue() {
        Color color = Color.GOLD;
        player.setCurrentAllianceColor(color);
        assertEquals(color, player.getCurrentAllianceColor());
    }


    @Test
    void testEquals_samePseudoDifferentCase_returnsTrue() {
        Player other = new Player(99, "Alice", PlayerColor.BLUE);
        assertEquals(player, other);
    }

    @Test
    void testEquals_samePseudoSameCase_returnsTrue() {
        Player other = new Player(99, "Alice", PlayerColor.BLUE);
        assertEquals(player, other);
    }

    @Test
    void testEquals_differentPseudo_returnsFalse() {
        Player other = new Player(1, "Bob", PlayerColor.RED);
        assertNotEquals(player, other);
    }

    @Test
    void testEquals_selfReference_returnsTrue() {
        assertEquals(player, player);
    }

    @Test
    void testEquals_null_returnsFalse() {
        assertNotEquals(null, player);
    }

    @Test
    void testEquals_differentType_returnsFalse() {
        Object notAPlayer = "Alice";
        assertNotEquals(notAPlayer, player);
    }
}