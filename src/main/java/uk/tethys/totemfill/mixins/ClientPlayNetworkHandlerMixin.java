package uk.tethys.totemfill.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import uk.tethys.totemfill.TotemFill;
import uk.tethys.totemfill.integration.TotemFillModMenuImpl;

@Environment(EnvType.CLIENT)
@Mixin(value = ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Shadow
    private ClientWorld world;

    private double oldHealth = 20.0;

    @Inject(method = "onEntityStatus", at = @At("RETURN"))
    public void onEntityStatus(EntityStatusS2CPacket entityStatusS2CPacket, CallbackInfo callbackInfo) {
        if (!TotemFill.enabled)
            return;

        // Check the packet is for totems
        if (entityStatusS2CPacket.getStatus() == 35) {
            Entity entity = entityStatusS2CPacket.getEntity(world);
            // Mod should only work for players
            if (entity instanceof PlayerEntity) {
                if (TotemFill.ticks >= 0)
                    return;
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
                    TotemFill.queuePackets(slot, (PlayerEntity) entity);

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
        if (!TotemFill.enabled)
            return;

        if (healthUpdateS2CPacket.getHealth() < TotemFillModMenuImpl.getConfig().getMinhealth() &&
                healthUpdateS2CPacket.getHealth() < oldHealth) {
            ClientPlayerEntity entity = MinecraftClient.getInstance().player;
            if (entity.inventory.getStack(entity.inventory.selectedSlot).getItem() == Items.TOTEM_OF_UNDYING ||
                    entity.inventory.offHand.get(0).getItem() == Items.TOTEM_OF_UNDYING)
                return;
            if (TotemFill.ticks >= 0)
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
                Text msg = new LiteralText(TotemFillModMenuImpl.getConfig().getNomoretotems())
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(15406100)));
                if (!MinecraftClient.getInstance().inGameHud.getChatHud().getMessageHistory().get(0).equals(msg.asString()))
                    entity.sendMessage(msg, false);
            } else {
                TotemFill.queuePackets(slot, entity);

                entity.sendMessage(new LiteralText(TotemFillModMenuImpl.getConfig().getTotemarmed())
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16110658))), false);
                entity.sendMessage(new LiteralText(TotemFillModMenuImpl.getConfig().getTotemcount()
                        .replace("%", String.valueOf(totemCount)))
                        .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(16110658))), false);
            }
        }
        this.oldHealth = healthUpdateS2CPacket.getHealth();
    }

}
