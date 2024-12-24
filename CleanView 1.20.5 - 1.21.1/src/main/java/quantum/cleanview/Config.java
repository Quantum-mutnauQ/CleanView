package quantum.cleanview;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@EventBusSubscriber(modid = CleanView.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    static ModConfigSpec.BooleanValue CleanView = BUILDER
            .comment("This this enables or disables this mod.")
            .define("CleanView", true);
    static ModConfigSpec.BooleanValue BoarderVignette = BUILDER
            .comment("Adds the red vignette aroud your screen if you are near to the boarder.")
            .define("BoarderVignette", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean CleanViewValue=true;
    public static boolean BoarderVignetteValue=true;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        CleanViewValue=CleanView.get();
        BoarderVignetteValue=BoarderVignette.get();
    }
}
