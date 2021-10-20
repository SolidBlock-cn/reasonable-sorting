package pers.solid.mod;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 与 ExtShape 模组联络的渠道。
 */
public final class ExtShapeBridge {
    private static boolean modLoaded = false;
    private static Function<String, Optional<Text>> checkIfValid = null;
    private static Consumer<String> updateShapeList = null;
    private static Supplier<String> getValidShapeNames = null;

    static {
        for (BooleanSupplier entrypoint : FabricLoader.getInstance().getEntrypoints("extshape:_modLoaded", BooleanSupplier.class)) {
            modLoaded = modLoaded || entrypoint.getAsBoolean();
        }
        for (Function<String, Optional<Text>> entrypoint : FabricLoader.getInstance().getEntrypoints("extshape:_checkIfValid", (Class<Function<String, Optional<Text>>>) (Class) Function.class)) {
            if (checkIfValid != null) throw new IllegalStateException();
            checkIfValid = entrypoint;
        }
        for (Consumer<String> entrypoint : FabricLoader.getInstance().getEntrypoints("extshape:_updateShapeList", (Class<Consumer<String>>) (Class) Consumer.class)) {
            if (updateShapeList != null) throw new IllegalStateException();
            updateShapeList = entrypoint;
        }
        for (Supplier<String> entrypoint : FabricLoader.getInstance().getEntrypoints("extshape:_getValidShapeNames", (Class<Supplier<String>>) (Class) Supplier.class)) {
            if (getValidShapeNames != null) throw new IllegalStateException();
            getValidShapeNames = entrypoint;
        }
    }

    public static boolean modLoaded() {
        return modLoaded;
    }

    public static Optional<Text> checkIfValid(String s) {
        if (checkIfValid == null) throw new IllegalStateException();
        return checkIfValid.apply(s);
    }

    public static void updateShapeList(String s) {
        if (updateShapeList == null) throw new IllegalStateException();
        updateShapeList.accept(s);
    }

    public static String getValidShapeNames() {
        return getValidShapeNames.get();
    }
}
