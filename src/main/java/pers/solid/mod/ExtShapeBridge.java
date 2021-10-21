package pers.solid.mod;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import net.fabricmc.loader.api.FabricLoader;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 与 ExtShape 模组联络的渠道。
 */
public final class ExtShapeBridge {
    /**
     * @see #modLoaded()
     */
    private static boolean modLoaded = false;
    /**
     * @see #updateShapeList(String)
     */
    private static Consumer<String> updateShapeList = null;
    /**
     * @see #getValidShapeNames()
     */
    private static Supplier<String> getValidShapeNames = null;
    /**
     * @see #isValidShapeName(String)
     */
    private static Object2BooleanFunction<String> isValidShapeName = null;
    /**
     * @see #updateShapeTransferRules(List)
     */
    private static Consumer<List<String>> updateShapeTransferRules = null;
    /**
     * @see #setBaseBlocksInBuildingBlocks(boolean)
     */
    private static BooleanConsumer setBaseBlocksInBuildingBlocks = null;

    static {
        for (BooleanSupplier entrypoint : FabricLoader.getInstance().getEntrypoints("extshape:_modLoaded", BooleanSupplier.class)) {
            modLoaded = modLoaded || entrypoint.getAsBoolean();
        }
        for (Consumer<String> entrypoint : FabricLoader.getInstance().getEntrypoints("extshape:_updateShapeList", (Class<Consumer<String>>) (Class) Consumer.class)) {
            if (updateShapeList != null) throw new IllegalStateException();
            updateShapeList = entrypoint;
        }
        for (Supplier<String> entrypoint : FabricLoader.getInstance().getEntrypoints("extshape:_getValidShapeNames", (Class<Supplier<String>>) (Class) Supplier.class)) {
            if (getValidShapeNames != null) throw new IllegalStateException();
            getValidShapeNames = entrypoint;
        }
        for (Object2BooleanFunction<String> entrypoint : FabricLoader.getInstance().getEntrypoints("extshape:_isValidShapeName", (Class<Object2BooleanFunction<String>>) (Class) Object2BooleanFunction.class)) {
            if (isValidShapeName != null) throw new IllegalStateException();
            isValidShapeName = entrypoint;
        }
        for (Consumer<List<String>> entrypoint : FabricLoader.getInstance().getEntrypoints("extshape:_updateShapeTransferRules", (Class<Consumer<List<String>>>) (Class) Consumer.class)) {
            if (updateShapeTransferRules != null) throw new IllegalStateException();
            updateShapeTransferRules = entrypoint;
        }
        for (BooleanConsumer entrypoint : FabricLoader.getInstance().getEntrypoints("extshape:_setBaseBlocksInBuildingBlocks", BooleanConsumer.class)) {
            if (setBaseBlocksInBuildingBlocks != null) throw new IllegalStateException();
            setBaseBlocksInBuildingBlocks = entrypoint;
        }
    }

    public static boolean modLoaded() {
        return modLoaded;
    }

    public static boolean isValidShapeName(String s) {
        if (isValidShapeName == null) return false;
        return isValidShapeName.getBoolean(s);
    }

    public static void updateShapeList(String s) {
        if (updateShapeList == null) return;
        updateShapeList.accept(s);
    }

    public static String getValidShapeNames() {
        if (getValidShapeNames == null) return null;
        return getValidShapeNames.get();
    }

    public static void updateShapeTransferRules(List<String> list) {
        if (updateShapeTransferRules == null) return;
        updateShapeTransferRules.accept(list);
    }

    public static void setBaseBlocksInBuildingBlocks(boolean b) {
        if (setBaseBlocksInBuildingBlocks == null) return;
        setBaseBlocksInBuildingBlocks.accept(b);
    }
}
