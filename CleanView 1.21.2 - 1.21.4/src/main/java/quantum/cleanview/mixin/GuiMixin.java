package quantum.cleanview.mixin;


//mport com.mojang.blaze3d.platform.GlStateManager;
//import com.mojang.blaze3d.systems.RenderSystem;
import com.nimbusds.oauth2.sdk.id.Identifier;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.border.WorldBorder;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quantum.cleanview.Config;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow @Final
    private Minecraft minecraft;
    @Shadow
    public float vignetteBrightness=1F;
    @Shadow @Final
    private static ResourceLocation VIGNETTE_LOCATION;


    @Shadow
    private float scopeScale;

    @Shadow
    protected abstract void renderSpyglassOverlay(GuiGraphics p_282069_, float p_283442_);

    @Shadow
    protected abstract void renderVignette(GuiGraphics p_283063_, @Nullable Entity p_283439_);

    @Shadow
    @Final
    private static ResourceLocation POWDER_SNOW_OUTLINE_LOCATION;

    @Shadow
    protected abstract void renderPortalOverlay(GuiGraphics p_283375_, float p_283296_);

    @Shadow
    protected abstract void renderConfusionOverlay(GuiGraphics p_361440_, float p_363322_);

    @Shadow
    protected abstract void renderTextureOverlay(GuiGraphics p_282304_, ResourceLocation p_281622_, float p_281504_);

    private void cleanView(GuiGraphics graphics, Entity camera, boolean force) {
        boolean customVignetteDisabled = !Config.CustomVignetteColorEnableValue;
        boolean vanillaVignetteEnabled = !Config.CleanViewValue;

        boolean shouldRenderVignette = customVignetteDisabled && (vanillaVignetteEnabled || force);

        if (shouldRenderVignette) {
            this.renderVignette(graphics, camera);
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
            graphics.blit(RenderType::vignette, VIGNETTE_LOCATION, 0, 0, 0.0F, 0.0F, graphics.guiWidth(), graphics.guiHeight(), graphics.guiWidth(), graphics.guiHeight(), ARGB.colorFromFloat(1.0F, red, greenBlue, blue));
        }
    }

    @Inject(method = "renderCameraOverlays",at = @At("HEAD"),cancellable = true)
    private void renderCameraOverlays(GuiGraphics p_316735_, DeltaTracker p_348538_, CallbackInfo ci){
        boolean forceVignette = false;

        LocalPlayer localplayer = this.minecraft.player;
        float f = p_348538_.getGameTimeDeltaTicks();
        this.scopeScale = Mth.lerp(0.5F * f, this.scopeScale, 1.125F);
        if (this.minecraft.options.getCameraType().isFirstPerson()) {
            if (localplayer.isScoping()) {
                if (Config.SpyglassEffektValue == Config.ScreenEffeckt.on)
                    this.renderSpyglassOverlay(p_316735_, this.scopeScale);
                else if (Config.SpyglassEffektValue == Config.ScreenEffeckt.vignette)
                    forceVignette = true;
            } else {
                this.scopeScale = 0.5F;

                for(EquipmentSlot equipmentslot : EquipmentSlot.values()) {
                    ItemStack itemstack = localplayer.getItemBySlot(equipmentslot);
                    Equippable equippable = (Equippable)itemstack.get(DataComponents.EQUIPPABLE);
                    if (equippable != null && equippable.slot() == equipmentslot) {
                        if (equippable.cameraOverlay().isPresent()) {
                            if (Config.EquipmentEffektValue == Config.ScreenEffeckt.on)
                                this.renderTextureOverlay(p_316735_, equippable.cameraOverlay().get().withPath((p_380782_) -> "textures/" + p_380782_ + ".png"), 1.0F);
                            else if (Config.EquipmentEffektValue == Config.ScreenEffeckt.vignette) {
                                forceVignette = true;
                                break;
                            }
                        }
                        this.renderTextureOverlay(p_316735_, ((ResourceLocation)equippable.cameraOverlay().get()).withPath((p_380782_) -> "textures/" + p_380782_ + ".png"), 1.0F);
                    }
                }
            }
        }

        if (localplayer.getTicksFrozen() > 0) {
            if (Config.FreezingEffektValue == Config.ScreenEffeckt.on)
                this.renderTextureOverlay(p_316735_, POWDER_SNOW_OUTLINE_LOCATION, localplayer.getPercentFrozen());
            else if (Config.FreezingEffektValue == Config.ScreenEffeckt.vignette)
                forceVignette = true;
        }

        float f1 = Mth.lerp(p_348538_.getGameTimeDeltaPartialTick(false), this.minecraft.player.oSpinningEffectIntensity, this.minecraft.player.spinningEffectIntensity);
        if (Config.PortalEffektValue == Config.ScreenEffeckt.on) {
            if (f1 > 0.0F) {
                if (!this.minecraft.player.hasEffect(MobEffects.CONFUSION)) {
                    this.renderPortalOverlay(p_316735_, f1);
                } else {
                    float f2 = ((Double)this.minecraft.options.screenEffectScale().get()).floatValue();
                    if (f2 < 1.0F) {
                        float f3 = f1 * (1.0F - f2);
                        this.renderConfusionOverlay(p_316735_, f3);
                    }
                }
            }
        } else if (f1 > 0.0F && Config.PortalEffektValue == Config.ScreenEffeckt.vignette) {
            forceVignette = true;
        }

        cleanView(p_316735_, this.minecraft.getCameraEntity(), forceVignette);

        ci.cancel();
    }


}
