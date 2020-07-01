package uk.tethys.totemfill.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.tethys.totemfill.integration.TotemFillModMenuImpl;

@Environment(EnvType.CLIENT)
@Mixin(value = ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Shadow
    private ClientWorld world;

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "onEntityStatus", at = @At("RETURN"))
    public void onEntityStatus(EntityStatusS2CPacket entityStatusS2CPacket, CallbackInfo callbackInfo) {
        // Check the packet is for totems
        if (entityStatusS2CPacket.getStatus() == 35) {
            Entity entity = entityStatusS2CPacket.getEntity(world);
            // Mod should only work for players
            if (entity instanceof PlayerEntity) {
                int slot = -1;
                int totemCount = 0;
                for (int i = 0; i < ((PlayerEntity) entity).inventory.size() - 1; i++) {
                    if (((PlayerEntity) entity).inventory.getStack(i).getItem() == Items.TOTEM_OF_UNDYING
                            && ((PlayerEntity) entity).inventory.selectedSlot != i) {
                        if (slot < 0)
                            slot = i;
                        totemCount++;
                    }
                }
                if (slot < 0) {
                    ((PlayerEntity) entity).sendMessage(new LiteralText(TotemFillModMenuImpl.getConfig().getNomoretotems())
                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(15406100))), false);
                } else {
                    ClientSidePacketRegistry.INSTANCE.sendToServer(new PickFromInventoryC2SPacket(slot));
                    ClientSidePacketRegistry.INSTANCE.sendToServer(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                            entity.getBlockPos(), entity.getHorizontalFacing()));
                    ClientSidePacketRegistry.INSTANCE.sendToServer(new PickFromInventoryC2SPacket(slot));

                    ((PlayerEntity) entity).sendMessage(new LiteralText(TotemFillModMenuImpl.getConfig().getTotemused())
                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16110658))), false);
                    ((PlayerEntity) entity).sendMessage(new LiteralText(TotemFillModMenuImpl.getConfig().getTotemcount()
                            .replace("%", String.valueOf(totemCount)))
                            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16110658))), false);
                }
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "onHealthUpdate", at = @At("RETURN"))
    public void onHealthUpdate(HealthUpdateS2CPacket healthUpdateS2CPacket, CallbackInfo callbackInfo) {
        if (healthUpdateS2CPacket.getHealth() < TotemFillModMenuImpl.getConfig().getMinhealth()) {
            ClientPlayerEntity entity = MinecraftClient.getInstance().player;
            if (entity.inventory.getStack(entity.inventory.selectedSlot).getItem() == Items.TOTEM_OF_UNDYING ||
                    entity.inventory.offHand.get(0).getItem() == Items.TOTEM_OF_UNDYING)
                return;
            int slot = -1;
            int totemCount = 0;
            for (int i = 0; i < entity.inventory.size() - 1; i++) {
                if (entity.inventory.getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                    if (slot < 0)
                        slot = i;
                    totemCount++;
                }
            }
            if (slot < 0) {
                entity.sendMessage(new LiteralText(TotemFillModMenuImpl.getConfig().getNomoretotems())
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(15406100))), false);
            } else {
                ClientSidePacketRegistry.INSTANCE.sendToServer(new PickFromInventoryC2SPacket(slot));
                ClientSidePacketRegistry.INSTANCE.sendToServer(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                        entity.getBlockPos(), entity.getHorizontalFacing()));
                ClientSidePacketRegistry.INSTANCE.sendToServer(new PickFromInventoryC2SPacket(slot));

                entity.sendMessage(new LiteralText(TotemFillModMenuImpl.getConfig().getTotemarmed())
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16110658))), false);
                entity.sendMessage(new LiteralText(TotemFillModMenuImpl.getConfig().getTotemcount()
                        .replace("%", String.valueOf(totemCount)))
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16110658))), false);
            }
        }
    }

}
