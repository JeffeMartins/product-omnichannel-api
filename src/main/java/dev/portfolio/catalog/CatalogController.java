package dev.portfolio.catalog;

import dev.portfolio.catalog.Models.*;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/v1")
public class CatalogController {
  private final CatalogService catalog;

  public CatalogController(CatalogService catalog) {
    this.catalog = catalog;
  }

  @GetMapping("/products/{sku}")
  public Detail product(
      @PathVariable @Pattern(regexp = "[A-Z0-9][A-Z0-9._-]{0,63}") String sku,
      @RequestParam(defaultValue = "WEB") @Pattern(regexp = "WEB|PICKUP") String channel,
      @RequestParam(defaultValue = "CD-DEMO-01") @Pattern(regexp = "[A-Z0-9-]{1,40}")
          String locationId) {
    return catalog.detail(sku, channel, locationId);
  }

  @GetMapping("/products/{sku}/availability")
  public Availability availability(
      @PathVariable @Pattern(regexp = "[A-Z0-9][A-Z0-9._-]{0,63}") String sku,
      @RequestParam(defaultValue = "WEB") @Pattern(regexp = "WEB|PICKUP") String channel,
      @RequestParam(defaultValue = "CD-DEMO-01") @Pattern(regexp = "[A-Z0-9-]{1,40}")
          String locationId) {
    return catalog.detail(sku, channel, locationId).availability();
  }

  @GetMapping("/locations")
  public List<Location> locations() {
    return catalog.locations();
  }
}
