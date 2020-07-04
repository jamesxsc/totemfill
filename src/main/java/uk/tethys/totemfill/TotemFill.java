package uk.tethys.totemfill;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import org.lwjgl.glfw.GLFW;
import uk.tethys.totemfill.integration.TotemFillModMenuImpl;

import java.util.concurrent.atomic.AtomicBoolean;

public class TotemFill implements ModInitializer {

    public static final String MOD_ID = "totemfill";

    public static boolean enabled;

    public static int ticks = -1;
    private static int nextSlot;
    private static PlayerEntity nextEntity;

    private static KeyBinding toggleKeyBind;

    @Override
    public void onInitialize() {
        toggleKeyBind = new KeyBinding(
                "key.totemfill.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_EQUAL,
                "category.totemfill.controls"
        );

        KeyBindingHelper.registerKeyBinding(toggleKeyBind);

        AtomicBoolean pressed = new AtomicBoolean(false);

        ClientTickCallback.EVENT.register((minecraftClient -> {
            if (ticks == 0) {
                sendPackets(nextSlot, nextEntity);
                ticks = -1;
            } else if (ticks > 0) {
                ticks--;
            }


            if (toggleKeyBind.wasPressed()) {
                if (!pressed.get()) {
                    if (MinecraftClient.getInstance().player != null) {
                        if (enabled) {
                            enabled = false;
                            MinecraftClient.getInstance().player.sendMessage(new LiteralText("Disabled Totemfill").setStyle(
                                    Style.EMPTY.withColor(TextColor.fromRgb(15406100))), false);
                        } else {
                            enabled = true;
                            MinecraftClient.getInstance().player.sendMessage(new LiteralText("Enabled Totemfill").setStyle(
                                    Style.EMPTY.withColor(TextColor.fromRgb(4247321))), false);
                        }
                    }
                }
                pressed.set(true);
            } else {
                pressed.set(false);
            }
        }));
    }

    public static void queuePackets(int slot, PlayerEntity entity) {
        ticks = (int) (TotemFillModMenuImpl.getConfig().getLowerdelaybound() +
                Math.floor(Math.random() *
                        (TotemFillModMenuImpl.getConfig().getUpperdelaybound() - TotemFillModMenuImpl.getConfig().getLowerdelaybound())));
        System.out.printf("Queuing packets to send to server with tick delay of %d", ticks);
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
