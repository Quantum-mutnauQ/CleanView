package quantum.cleanview.mixin;


import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.border.WorldBorder;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quantum.cleanview.Config;

@Mixin(Hud.class)
public abstract class GuiMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    public float vignetteBrightness = 1F;
    @Shadow
    @Final
    private static Identifier VIGNETTE_LOCATION;


    @Shadow
    private float scopeScale;

    @Shadow
    protected abstract void extractSpyglassOverlay(GuiGraphicsExtractor graphics, float scale);

    @Shadow
    protected abstract void extractTextureOverlay(GuiGraphicsExtractor graphics, Identifier texture, float alpha);

    @Shadow
    @Final
    private static Identifier POWDER_SNOW_OUTLINE_LOCATION;

    @Shadow
    protected abstract void extractPortalOverlay(GuiGraphicsExtractor graphics, float alpha);

    @Shadow
    protected abstract void extractConfusionOverlay(GuiGraphicsExtractor graphics, float strength);

    @Shadow
    protected abstract void extractVignette(GuiGraphicsExtractor graphics, @Nullable Entity camera);

    @Shadow
    protected abstract @Nullable Player getCameraPlayer();

    private void cleanView(GuiGraphicsExtractor graphics, Entity camera, boolean force) {
        boolean customVignetteDisabled = !Config.CustomVignetteColorEnableValue;
        boolean vanillaVignetteEnabled = this.minecraft.options.vignette().get() && !Config.CleanViewValue;

        boolean shouldRenderVignette = customVignetteDisabled && (vanillaVignetteEnabled || force);

        if (shouldRenderVignette) {
            this.extractVignette(graphics, camera);
            return;
        }

        float baseR, baseG, baseB;
        boolean customEnabled = Config.CustomVignetteColorEnableValue;

        if (customEnabled) {
            baseR = 1.0F - Mth.clamp(Config.CustomVignetteColorValue.red, 0.0F, 1.0F);
            baseG = 1.0F - Mth.clamp(Config.CustomVignetteColorValue.green, 0.0F, 1.0F);
            baseB = 1.0F - Mth.clamp(Config.CustomVignetteColorValue.blue, 0.0F, 1.0F);
        } else {
            float brightness = Mth.clamp(this.vignetteBrightness, 0.0F, 1.0F);
            baseR = baseG = baseB = brightness;
        }

        float borderWarningStrength = 0.0F;
        if (Config.BoarderVignetteValue && camera != null) {
            WorldBorder worldBorder = this.minecraft.level.getWorldBorder();
            float distToBorder = (float) worldBorder.getDistanceToBorder(camera);
            double movingBlocksThreshold = Math.min(worldBorder.getLerpSpeed() * (double) worldBorder.getWarningTime(), Math.abs(worldBorder.getLerpTarget() - worldBorder.getSize()));
            double warningDistance = Math.max(worldBorder.getWarningBlocks(), movingBlocksThreshold);

            if ((double) distToBorder < warningDistance) {
                borderWarningStrength = Mth.clamp(1.0F - (float) ((double) distToBorder / warningDistance), 0.0F, 1.0F);
            }
        }

        float red = Mth.lerp(borderWarningStrength, baseR, 0.0F);
        float greenBlue = Mth.lerp(borderWarningStrength, baseG, 1.0F);
        float blue = Mth.lerp(borderWarningStrength, baseB, 1.0F);

        if (customEnabled || borderWarningStrength > 0.0F) {
            graphics.blit(RenderPipelines.VIGNETTE, VIGNETTE_LOCATION, 0, 0, 0.0F, 0.0F, graphics.guiWidth(), graphics.guiHeight(), graphics.guiWidth(), graphics.guiHeight(), ARGB.colorFromFloat(1.0F, red, greenBlue, blue));
        }
    }

    @Inject(method = "extractCameraOverlays", at = @At("HEAD"), cancellable = true)
    private void extractCameraOverlays(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        boolean forceVignette = false;

        LocalPlayer player = this.minecraft.player;
        float gameTimeDeltaTicks = deltaTracker.getGameTimeDeltaTicks();
        this.scopeScale = Mth.lerp(0.5F * gameTimeDeltaTicks, this.scopeScale, 1.125F);

        if (this.minecraft.options.getCameraType().isFirstPerson()) {
            if (player.isScoping()) {
                if (Config.SpyglassEffektValue == Config.ScreenEffeckt.on)
                    this.extractSpyglassOverlay(graphics, this.scopeScale);
                else if (Config.SpyglassEffektValue == Config.ScreenEffeckt.vignette)
                    forceVignette = true;
            } else {
                this.scopeScale = 0.5F;

                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    ItemStack item = player.getItemBySlot(slot);
                    Equippable equippable = item.get(DataComponents.EQUIPPABLE);
                    if (equippable != null && equippable.slot() == slot) {
                        if (equippable.cameraOverlay().isPresent()) {
                            if (Config.EquipmentEffektValue == Config.ScreenEffeckt.on)
                                this.extractTextureOverlay(graphics, equippable.cameraOverlay().get().withPath(p -> "textures/" + p + ".png"), 1.0F);
                            else if (Config.EquipmentEffektValue == Config.ScreenEffeckt.vignette) {
                                forceVignette = true;
                                break;
                            }
                        }
                        net.neoforged.neoforge.client.extensions.common.IClientItemExtensions.of(item).renderFirstPersonOverlay(item, slot, this.minecraft.player, graphics, deltaTracker);
                    }
                }
            }
        }

        if (player.getTicksFrozen() > 0) {
            if (Config.FreezingEffektValue == Config.ScreenEffeckt.on)
                this.extractTextureOverlay(graphics, POWDER_SNOW_OUTLINE_LOCATION, player.getPercentFrozen());
            else if (Config.FreezingEffektValue == Config.ScreenEffeckt.vignette)
                forceVignette = true;
        }

        float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(false);
        float portalIntensity = Mth.lerp(partialTicks, player.oPortalEffectIntensity, player.portalEffectIntensity);
        float nauseaIntensity = player.getEffectBlendFactor(MobEffects.NAUSEA, partialTicks);
        if (Config.PortalEffektValue == Config.ScreenEffeckt.on) {
            if (portalIntensity > 0.0F) {
                this.extractPortalOverlay(graphics, portalIntensity);
            } else if (nauseaIntensity > 0.0F) {
                float screenEffectScale = this.minecraft.options.screenEffectScale().get().floatValue();
                if (screenEffectScale < 1.0F) {
                    float overlayStrength = nauseaIntensity * (1.0F - screenEffectScale);
                    this.extractConfusionOverlay(graphics, overlayStrength);
                }
            }
        } else if (portalIntensity > 0.0F && Config.PortalEffektValue == Config.ScreenEffeckt.vignette) {
            forceVignette = true;
        }

        cleanView(graphics, getCameraPlayer(), forceVignette);

        ci.cancel();
    }
}
