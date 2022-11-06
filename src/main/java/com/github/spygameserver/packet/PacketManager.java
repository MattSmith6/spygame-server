package com.github.spygameserver.packet;

import java.util.HashMap;
import java.util.Map;

public class PacketManager {

    private final Map<Integer, AbstractPacket> packetIdToPacketMap;

    public PacketManager() {
        this.packetIdToPacketMap = new HashMap<>();
    }

    private void registerPacket(AbstractPacket abstractPacket) {
        packetIdToPacketMap.put(abstractPacket.getPacketId(), abstractPacket);
    }

}
