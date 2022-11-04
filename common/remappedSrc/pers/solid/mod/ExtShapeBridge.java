package pers.solid.mod;

import dev.architectury.injectables.annotations.ExpectPlatform;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

/**
 * 本类用于兼容不同平台，同时也在 Reasonable Sorting 与 Extended Block Shapes 模组之间搭建桥梁。
 */
public class ExtShapeBridge {
  /**
   * 当没有安装 Reasonable Sorting 的时候，这个字段是一个空的实例。装了 Reasonable Sorting 后，这个字段就会是由 Reasonable Sorting 提供的对象，其类型为 ExtShapeBridge 的子类型。注意：避免此类在过早初始化，否则可能导致 {@link #getInstance()} 无法获取到由 Reasonable Sorting 提供的对象（尤其是在 Forge 模组中）。
   */
  public static final ExtShapeBridge INSTANCE = getInstance();

  /**
   * 返回一个 ExtShapeBridge 的实例。当装有 Reasonable Sorting 时，返回的值是由 Reasonable Sorting 模组提供的一个 ExtShapeBridge 的子类型对象。
   * <p>
   * 这个方法只会在初始化类时给 {@link #INSTANCE} 复制时调用一次。
   * <p>
   * 注意，根据 Architectury Plugin，这个方法的实际方法体取决于具体平台。请参见 {@code ExtShapeBridgeImpl}。
   */
  @ExpectPlatform
  @ApiStatus.Internal
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
