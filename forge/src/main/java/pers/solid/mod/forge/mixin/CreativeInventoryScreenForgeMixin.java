package pers.solid.mod.forge.mixin;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import pers.solid.mod.SortingRule;

import java.util.Iterator;

@OnlyIn(Dist.CLIENT)
@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenForgeMixin {

  @Redirect(method = "search", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;iterator()Ljava/util/Iterator;"))
  public Iterator<Item> modifiedIteratorInSearch(DefaultedRegistry<Item> instance) {
    return SortingRule.modifyIteratorInInventory(instance.iterator(), ItemGroup.SEARCH.getName());
  }
}
