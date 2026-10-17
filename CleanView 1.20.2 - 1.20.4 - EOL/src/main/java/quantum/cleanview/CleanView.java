package quantum.cleanview;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CleanView.MODID)
public class CleanView
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "cleanview";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public CleanView()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }
}
