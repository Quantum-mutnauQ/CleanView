package quantum.cleanview;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = CleanView.MODID)
public class Config {
    public static enum ScreenEffeckt {
        none, vignette, on;
    }

    public static class VignetteColor {
        public float red, green, blue;
    }

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    static ModConfigSpec.BooleanValue CleanView = BUILDER
            .comment("This this enables or disables this mod.")
            .define("CleanView", true);
    static ModConfigSpec.BooleanValue BoarderVignette = BUILDER
            .comment("Adds the red vignette aroud your screen if you are near to the boarder.")
            .define("BoarderVignette", true);

    static ModConfigSpec.BooleanValue CustomVignetteColorEnable = BUILDER
            .comment("Enables custom vIgnette color")
            .define("CustomVignetteColorEnable", false);

    static ModConfigSpec.DoubleValue CustomVignetteRed = BUILDER
            .comment("custom vignette red")
            .defineInRange("CustomVignetteRed", 0.0, 0.0, 1.0);
    static ModConfigSpec.DoubleValue CustomVignetteGreen = BUILDER
            .comment("custom vignette green")
            .defineInRange("CustomVignetteGreen", 0.0, 0.0, 1.0);
    static ModConfigSpec.DoubleValue CustomVignetteBlue = BUILDER
            .comment("custom vignette blue")
            .defineInRange("CustomVignetteBlue", 0.0, 0.0, 1.0);
    static ModConfigSpec.EnumValue<ScreenEffeckt> SpyglassEffekt = BUILDER
            .comment("Sets the screenefeckt when using a spy glas")
            .defineEnum("SpyglassEffekt", ScreenEffeckt.none);
    static ModConfigSpec.EnumValue<ScreenEffeckt> EquipmentEffekt = BUILDER
            .comment("The screen screenefeckt when warin items (Carved pumpkin).")
            .defineEnum("EquipmentEffekt", ScreenEffeckt.none);
    static ModConfigSpec.EnumValue<ScreenEffeckt> FreezingEffekt = BUILDER
            .comment("The freezing effekt on the screen")
            .defineEnum("FreezingEffekt", ScreenEffeckt.on);
    static ModConfigSpec.EnumValue<ScreenEffeckt> PortalEffekt = BUILDER
            .comment("The effekt when entering a Portal")
            .defineEnum("PortalEffekt", ScreenEffeckt.on);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean CleanViewValue = true;
    public static boolean BoarderVignetteValue = true;
    public static boolean CustomVignetteColorEnableValue = false;
    public static VignetteColor CustomVignetteColorValue = new VignetteColor();
    public static ScreenEffeckt SpyglassEffektValue = ScreenEffeckt.none;
    public static ScreenEffeckt EquipmentEffektValue = ScreenEffeckt.none;
    public static ScreenEffeckt FreezingEffektValue = ScreenEffeckt.on;
    public static ScreenEffeckt PortalEffektValue = ScreenEffeckt.on;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        CleanViewValue = CleanView.get();
        BoarderVignetteValue = BoarderVignette.get();
        CustomVignetteColorEnableValue = CustomVignetteColorEnable.get();
        CustomVignetteColorValue.red = CustomVignetteRed.get().floatValue();
        CustomVignetteColorValue.green = CustomVignetteGreen.get().floatValue();
        CustomVignetteColorValue.blue = CustomVignetteBlue.get().floatValue();
        SpyglassEffektValue = SpyglassEffekt.get();
        EquipmentEffektValue = EquipmentEffekt.get();
        FreezingEffektValue = FreezingEffekt.get();
        PortalEffektValue = PortalEffekt.get();
    }
}
