package com.github.spygameserver.packet;

import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;

import java.util.HashMap;
import java.util.Map;

public class PacketManager {

    private final GameDatabase gameDatabase;
    private final AuthenticationDatabase authenticationDatabase;

    private final Map<Integer, AbstractPacket> packetIdToPacketMap;

    public PacketManager(GameDatabase gameDatabase, AuthenticationDatabase authenticationDatabase) {
        this.gameDatabase = gameDatabase;
        this.authenticationDatabase = authenticationDatabase;

        this.packetIdToPacketMap = new HashMap<>();
    }

    private void registerPacket(AbstractPacket abstractPacket) {
        packetIdToPacketMap.put(abstractPacket.getPacketId(), abstractPacket);
    }

    public AbstractPacket getPacket(int packetNumber) {
        return packetIdToPacketMap.get(packetNumber);
    }

    public GameDatabase getGameDatabase() {
        return this.gameDatabase;
    }

    public AuthenticationDatabase getAuthenticationDatabase() {
        return this.authenticationDatabase;
    }

}
