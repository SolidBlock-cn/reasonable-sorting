package pers.solid.mod.forge;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pers.solid.mod.ConfigScreen;
import pers.solid.mod.Configs;
import pers.solid.mod.SortingRules;
import pers.solid.mod.TransferRules;

@Mod("reasonable_sorting")
public class ReasonableSortingForge {
  public static final Logger LOGGER = LogManager.getLogger(ReasonableSortingForge.class);
  private static final ConfigScreen CONFIG_SCREEN = new ConfigScreen();

  static {
    Configs.instance = new Configs();
  }

  public ReasonableSortingForge() {
    FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Item.class, EventPriority.LOWEST, (RegistryEvent.Register<Item> event) -> {
      SortingRules.initialize();
      TransferRules.initialize();
      Configs.loadAndUpdate();
    });

    ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (client, screen) -> CONFIG_SCREEN.createScreen(screen));
  }
}
