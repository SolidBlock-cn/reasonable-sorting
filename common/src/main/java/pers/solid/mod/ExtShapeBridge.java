package pers.solid.mod;

import dev.architectury.injectables.annotations.ExpectPlatform;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

/**
 * 本类用于兼容不同平台，同时也在 Reasonable Sorting 与 Extended Block Shapes 模组之间搭建桥梁。
 */
public class ExtShapeBridge {
  public static final ExtShapeBridge INSTANCE = getInstance();

  @ExpectPlatform
  private static @NotNull ExtShapeBridge getInstance() {
    return new ExtShapeBridge();
  }

  public boolean modHasLoaded() {
    return false;
  }

  public boolean isValidShapeName(String s) {
    return false;
  }

  public Stream<String> getValidShapeNames() {
    return Stream.empty();
  }

  public void updateShapeList(String s) {
  }

  public void updateShapeTransferRules(List<String> list) {

  }
}
