package com.historicconquest.historicconquest.network;

public interface RoomEventListener {
    void onPlayerJoin();
    void onPlayerQuit();
    void onPlayerColorChange();
    void onPlayerPseudoChange();
    void onPlayerPings();

    void onRoomDeleted();
}
