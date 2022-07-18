package pers.solid.mod;

import org.quiltmc.config.api.Config;
import org.quiltmc.config.api.Serializer;
import org.quiltmc.config.api.exceptions.ConfigParseException;
import org.quiltmc.config.api.values.*;
import org.quiltmc.json5.JsonWriter;
import org.quiltmc.loader.impl.config.Json5Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

public class JsonSerializerQuilt implements Serializer {

  @Override
  public String getFileExtension() {
    return "json";
  }


  private void serialize(JsonWriter writer, Object value) throws IOException {
    if (value instanceof Integer) {
      writer.value((Integer) value);
    } else if (value instanceof Long) {
      writer.value((Long) value);
    } else if (value instanceof Float) {
      writer.value((Float) value);
    } else if (value instanceof Double) {
      writer.value((Double) value);
    } else if (value instanceof Boolean) {
      writer.value((Boolean) value);
    } else if (value instanceof String) {
      writer.value((String) value);
    } else if (value instanceof ValueList<?>) {
      writer.beginArray();

      for (Object v : (ValueList<?>) value) {
        serialize(writer, v);
      }

      writer.endArray();
    } else if (value instanceof ValueMap<?>) {
      writer.beginObject();

      for (Map.Entry<String, ?> entry : (ValueMap<?>) value) {
        writer.name(entry.getKey());
        serialize(writer, entry.getValue());
      }

      writer.endObject();
    } else if (value instanceof ConfigSerializableObject) {
      serialize(writer, ((ConfigSerializableObject<?>) value).getRepresentation());
    } else if (value == null) {
      writer.nullValue();
    } else if (value.getClass().isEnum()) {
      writer.value(((Enum<?>) value).name());
    } else {
      throw new ConfigParseException();
    }
  }

  private void serialize(JsonWriter writer, ValueTreeNode node) throws IOException {
    if (node instanceof ValueTreeNode.Section) {
      writer.name(node.key().getLastComponent());
      writer.beginObject();

      for (ValueTreeNode child : ((ValueTreeNode.Section) node)) {
        serialize(writer, child);
      }

      writer.endObject();
    } else {
      TrackedValue<?> trackedValue = ((TrackedValue<?>) node);
      writer.name(node.key().getLastComponent());
      serialize(writer, trackedValue.getRealValue());
    }
  }

  @Override
  public void serialize(Config config, OutputStream to) throws IOException {
    JsonWriter writer = JsonWriter.json(new OutputStreamWriter(to));

    writer.beginObject();

    for (ValueTreeNode node : config.nodes()) {
      this.serialize(writer, node);
    }

    writer.endObject();
    writer.close();
  }

  @Override
  public void deserialize(Config config, InputStream from) {
    Json5Serializer.INSTANCE.deserialize(config, from);
  }
}
