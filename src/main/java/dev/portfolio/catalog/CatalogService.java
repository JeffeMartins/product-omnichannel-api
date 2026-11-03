package dev.portfolio.catalog;

import static dev.portfolio.catalog.Models.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CatalogService {
  private final JdbcTemplate db;
  private final CacheStore cache;

  public CatalogService(JdbcTemplate db, CacheStore cache) {
    this.db = db;
    this.cache = cache;
  }

  public Product product(String sku) {
    String key = "catalog:product:v1:" + sku;
    return cache
        .get(key, Product.class)
        .orElseGet(
            () -> {
              var rows =
                  db.query(
                      "SELECT * FROM products WHERE sku=? AND active=true",
                      (rs, n) ->
                          new Product(
                              rs.getString("sku"),
                              rs.getString("slug"),
                              rs.getString("name"),
                              rs.getString("description"),
                              rs.getString("brand"),
                              rs.getString("category"),
                              List.of(new Image(rs.getString("image"), rs.getString("name"))),
                              List.of(
                                  new Attribute("Autonomia", "Até 40 horas"),
                                  new Attribute("Conectividade", "Bluetooth 5.3"),
                                  new Attribute("Garantia", "12 meses")),
                              db.query(
                                  "SELECT sku,label,color FROM products WHERE family=(SELECT family FROM products WHERE sku=?) AND active=true ORDER BY sku",
                                  (r, i) ->
                                      new Variant(
                                          r.getString("sku"),
                                          r.getString("label"),
                                          r.getString("color")),
                                  sku)),
                      sku);
              if (rows.isEmpty())
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
              var p = rows.getFirst();
              cache.put(key, p, Duration.ofSeconds(300 + ThreadLocalRandom.current().nextInt(30)));
              return p;
            });
  }

  public Detail detail(String sku, String channel, String location) {
    var product = product(sku);
    if (db.queryForObject(
            "SELECT count(*) FROM locations WHERE id=? AND channel=?",
            Integer.class,
            location,
            channel)
        != 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid channel/location");
    String key = "catalog:offer:v1:" + sku + ":" + channel + ":" + location + ":AURORA:BRL";
    // Public record Detail is used for JSON decoding rather than a local record.
    var cached = cache.get(key, Detail.class);
    if (cached.isPresent())
      return new Detail(product, cached.get().offer(), cached.get().availability());
    var rows =
        db.query(
            "SELECT o.*,i.quantity,i.updated_at AS stock_updated FROM offers o JOIN inventory i ON i.sku=o.sku AND i.location_id=o.location_id WHERE o.sku=? AND o.channel=? AND o.location_id=?",
            (rs, n) ->
                new Detail(
                    product,
                    new Offer(
                        "AVAILABLE",
                        "BRL",
                        rs.getBigDecimal("list_price").toPlainString(),
                        rs.getBigDecimal("price").toPlainString(),
                        channel,
                        location,
                        rs.getTimestamp("updated_at").toInstant()),
                    new Availability(
                        rs.getInt("quantity") > 0 ? "IN_STOCK" : "OUT_OF_STOCK",
                        rs.getInt("quantity"),
                        rs.getTimestamp("stock_updated").toInstant())),
            sku,
            channel,
            location);
    if (rows.isEmpty())
      return new Detail(
          product,
          new Offer("UNAVAILABLE", "BRL", null, null, channel, location, null),
          new Availability("UNAVAILABLE", 0, null));
    var result = rows.getFirst();
    cache.put(key, result, Duration.ofSeconds(10));
    return result;
  }

  public List<Location> locations() {
    return db.query(
        "SELECT * FROM locations ORDER BY id",
        (r, n) -> new Location(r.getString("id"), r.getString("name"), r.getString("channel")));
  }
}
