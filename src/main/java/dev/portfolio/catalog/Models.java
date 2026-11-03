package dev.portfolio.catalog;

import java.time.Instant;
import java.util.List;

public final class Models {
  private Models() {}

  public record Image(String src, String alt) {}

  public record Attribute(String name, String value) {}

  public record Variant(String sku, String slug, String label, String color) {}

  public record Product(
      String sku,
      String slug,
      String name,
      String description,
      String brand,
      String category,
      List<Image> images,
      List<Attribute> attributes,
      List<Variant> variants) {}

  public record Offer(
      String status,
      String currency,
      String listPrice,
      String price,
      String channel,
      String locationId,
      Instant updatedAt) {}

  public record Availability(String status, int quantity, Instant updatedAt) {}

  public record Detail(Product product, Offer offer, Availability availability) {}

  public record Location(String id, String name, String channel) {}
}
