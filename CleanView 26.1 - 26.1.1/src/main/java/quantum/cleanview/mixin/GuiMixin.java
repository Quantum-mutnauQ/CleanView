package quantum.cleanview.mixin;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import quantum.cleanview.Config;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow @Final
    private Minecraft minecraft;
    @Shadow
    public float vignetteBrightness=1F;
    @Shadow @Final
    private static Identifier VIGNETTE_LOCATION;


    @Inject(method = "extractVignette",at = @At("HEAD"),cancellable = true)
    private void cleanView(GuiGraphicsExtractor graphics, Entity camera, CallbackInfo ci){
        if(!Config.CleanViewValue)
            return;
        if(!Config.BoarderVignetteValue){
            ci.cancel();
            return;
        }

        WorldBorder worldBorder = this.minecraft.level.getWorldBorder();
        float borderWarningStrength = 0.0F;
        if (camera != null) {
            float distToBorder = (float)worldBorder.getDistanceToBorder(camera);
            double movingBlocksThreshold = Math.min(worldBorder.getLerpSpeed() * (double)worldBorder.getWarningTime(), Math.abs(worldBorder.getLerpTarget() - worldBorder.getSize()));
            double warningDistance = Math.max((double)worldBorder.getWarningBlocks(), movingBlocksThreshold);
            if ((double)distToBorder < warningDistance) {
                borderWarningStrength = 1.0F - (float)((double)distToBorder / warningDistance);
            }
        }

        float brightness = Mth.clamp(this.vignetteBrightness, 0.0F, 1.0F);
        int color = 0;
        if (borderWarningStrength > 0.0F) {
            borderWarningStrength = Mth.clamp(borderWarningStrength, 0.0F, 1.0F);
            float red = brightness * (1.0F - borderWarningStrength);
            float greenBlue = brightness + (1.0F - brightness) * borderWarningStrength;
            color = ARGB.colorFromFloat(1.0F, red, greenBlue, greenBlue);
        } else if (!Config.CleanViewValue){
            color = ARGB.colorFromFloat(1.0F, brightness, brightness, brightness);
        }

        graphics.blit(RenderPipelines.VIGNETTE, VIGNETTE_LOCATION, 0, 0, 0.0F, 0.0F, graphics.guiWidth(), graphics.guiHeight(), graphics.guiWidth(), graphics.guiHeight(), color);

    }

}
