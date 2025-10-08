package quantum.cleanview.mixin;


//mport com.mojang.blaze3d.platform.GlStateManager;
//import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
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
    private static ResourceLocation VIGNETTE_LOCATION;


    @Inject(method = "Lnet/minecraft/client/gui/Gui;renderVignette(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/Entity;)V",at = @At("HEAD"),cancellable = true)
    private void cleanView(GuiGraphics p_283063_, Entity p_283439_, CallbackInfo ci){
        if(!Config.CleanViewValue)
            return;
        if(!Config.BoarderVignetteValue){
            ci.cancel();
            return;
        }

        WorldBorder worldborder = this.minecraft.level.getWorldBorder();
        float f = 0.0F;
        if (p_283439_ != null) {
            float f1 = (float)worldborder.getDistanceToBorder(p_283439_);
            double d0 = Math.min(
                    worldborder.getLerpSpeed() * (double)worldborder.getWarningTime() * 1000.0, Math.abs(worldborder.getLerpTarget() - worldborder.getSize())
            );
            double d1 = Math.max((double)worldborder.getWarningBlocks(), d0);
            if ((double)f1 < d1) {
                f = 1.0F - (float)((double)f1 / d1);
            }
        }

        int i;
        if (f > 0.0F) {
            f = Mth.clamp(f, 0.0F, 1.0F);
            i = ARGB.colorFromFloat(1.0F, 0.0F, f, f);
            p_283063_.blit(
                    RenderPipelines.VIGNETTE,
                    VIGNETTE_LOCATION,
                    0,
                    0,
                    0.0F,
                    0.0F,
                    p_283063_.guiWidth(),
                    p_283063_.guiHeight(),
                    p_283063_.guiWidth(),
                    p_283063_.guiHeight(),
                    i
            );
        } /*else {
            float f2 = this.vignetteBrightness;
            f2 = Mth.clamp(f2, 0.0F, 1.0F);
            i = ARGB.colorFromFloat(1.0F, f2, f2, f2);
        }*/


        ci.cancel();

    }

}
