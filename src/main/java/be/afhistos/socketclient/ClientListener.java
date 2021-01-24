package be.afhistos.socketclient;

import be.afhistos.socketserver.ServerPacket;

public interface ClientListener {

    default void onConnection(ServerClient client){};
    default void onError(ServerClient client, Exception e){};
    default void onLogout(ServerClient client){};
    default void onPacketReceived(ServerClient client, ServerPacket packet){};
    default void onPacketSent(ServerClient client, ServerPacket packet){};


}
