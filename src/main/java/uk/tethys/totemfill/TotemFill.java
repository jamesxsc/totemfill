package uk.tethys.totemfill;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;

public class TotemFill implements ModInitializer {

    public static final String MOD_ID = "totemfill";

    public static int ticks = -1;
    private static int nextSlot;
    private static PlayerEntity nextEntity;

    @Override
    public void onInitialize() {
        ClientTickCallback.EVENT.register((minecraftClient -> {
            if (ticks == 0) {
                sendPackets(nextSlot, nextEntity);
                ticks = -1;
            } else if (ticks > 0) {
                ticks--;
            }
        }));
    }

    public static void queuePackets(int slot, PlayerEntity entity) {
        ticks = 8;
        nextSlot = slot;
        nextEntity = entity;
    }

    /**
     * Sends required packets to server to put an item from slot into the offhand
     *
     * @param slot   the slot containing the target item
     * @param entity the player to perform the action on
     */
    private void sendPackets(int slot, PlayerEntity entity) {
        ClientSidePacketRegistry.INSTANCE.sendToServer(new PickFromInventoryC2SPacket(slot));
        ClientSidePacketRegistry.INSTANCE.sendToServer(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                entity.getBlockPos(), entity.getHorizontalFacing()));
        ClientSidePacketRegistry.INSTANCE.sendToServer(new PickFromInventoryC2SPacket(slot));

    }

}
