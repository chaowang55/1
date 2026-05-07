package uk.ac.ncl.csc8019.team4.menu;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController

@RequestMapping("/api/menu")
public class MenuItemController {

    private final MenuItemRepository menuItems;

    public MenuItemController(MenuItemRepository menuItems) {
        this.menuItems = menuItems;
    }

    /** Public: list all available items. */
    @GetMapping
    public List<MenuItem> listAvailable() {
        return menuItems.findAllByAvailableTrue();
    }

    /** Staff: list every item including unavailable ones. */
    @GetMapping("/all")
    public List<MenuItem> listAll() {
        return menuItems.findAll();
    }

    /** Staff: add a new menu item. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItem create(@Valid @RequestBody MenuItemRequest req) {
        MenuItem item = new MenuItem(req.name(), req.regularPrice(), req.largePrice(), req.category());
        item.setDescription(req.description());
        return menuItems.save(item);
    }

    /** Staff: edit an existing menu item's details or prices. */
    @PutMapping("/{id}")
    public MenuItem update(@PathVariable Long id, @Valid @RequestBody MenuItemRequest req) {
        MenuItem item = findOrThrow(id);
        item.setName(req.name());
        item.setDescription(req.description());
        item.setRegularPrice(req.regularPrice());
        item.setLargePrice(req.largePrice());
        item.setCategory(req.category());
        return menuItems.save(item);
    }

    /** Staff: mark item unavailable (out of stock) or available again. */
    @PatchMapping("/{id}/availability")
    public MenuItem setAvailability(@PathVariable Long id, @RequestParam boolean available) {
        MenuItem item = findOrThrow(id);
        item.setAvailable(available);
        return menuItems.save(item);
    }

    /** Staff: remove a menu item permanently. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        menuItems.deleteById(id);
    }

    // helper methods

    private MenuItem findOrThrow(Long id) {
        return menuItems
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Menu item not found: " + id));
    }

    // request

    public record MenuItemRequest(
            @NotBlank String name,
            @Size(max = 500) String description,
            @NotNull @DecimalMin("0.01") BigDecimal regularPrice,
            @DecimalMin("0.01") BigDecimal largePrice,
            @NotNull Category category) {}
}
