<div align="center">
  <img src="https://cdn.modrinth.com/data/hcWa8Cc7/fb2475e6d16126ca37922fca378e2adf4a547774.png" alt="Item Highlighter Logo" width="128" />
  <h1>Item Highlighter</h1>
  <p>Highlight newly collected items in your inventory with an illuminated slot pedestal — rendered behind the item so textures and stack counts stay completely visible.</p>
</div>

<br/>

<p align="center">
  <img src="https://cdn.modrinth.com/data/hcWa8Cc7/images/ced107e254717255dc67a3f665bfd0df293d6fbd.png" alt="Inventory Preview" width="85%" />
</p>

When you pick up items in Minecraft, it is easy to lose track of what just landed in your bags.

Most highlighter mods handle this by drawing a star badge, corner icon, or colored tint directly over your items. While functional, covering the item makes it hard to see what you actually picked up, obscures the stack count number, and quickly turns a full inventory into visual clutter.

**Item Highlighter** highlights new items from behind. It renders an illuminated, recessed pedestal shelf directly onto the slot background itself. Your item sprite and count stay completely crisp on top, while the slot underneath glows with a soft pastel hue so you can spot new loot at a glance.

<br/>

<p align="center">
  <img src="https://cdn.modrinth.com/data/hcWa8Cc7/images/95d10552440f9db3c3af851bd3eaf4985f882a82.png" alt="In-Game Preview" width="90%" />
</p>

## Features

- **Rendered Behind Items**: Highlights are drawn on the slot background beneath the item. No obscured sprites, no hidden durability bars, and no covered stack counts.
- **Recessed Slot Shelf**: Designed with a subtle inner shadow, a soft vertical gradient, and a translucent border that keeps adjacent slots clearly separated.
- **Neutral Default Look**: Uses a warm buttercup yellow (`#F6D365`) out of the box — gentle on the eyes, easy to spot, and fits right into vanilla inventories.
- **Multiple Color Modes**:
  - `DEFAULT`: Clean, uniform single color for all items.
  - `RARITY`: Automatically colors items based on their rarity tier (Common, Uncommon, Rare, Epic).
  - `AUTO_MOD`: Assigns a unique, deterministic pastel shade to each mod namespace.
  - **Custom Overrides**: Manually map any hex color to specific mod IDs (`modColors`) or specific item IDs (`itemColors`).
- **Intuitive Dismissal**: Highlights automatically clear when you hover over them, pick them up, select the slot on your hotbar, or (optionally) when you close the inventory screen.
- **Purely Client-Side**: Safe to use on any multiplayer server, Realm, or singleplayer world. No server-side installation required.

---

## Configuration

The mod generates a simple configuration file at `.minecraft/config/item-highlighter.json`:

```json
{
  "colorMode": "DEFAULT",
  "defaultColor": "#F6D365",
  "clearOnHover": true,
  "clearOnClick": true,
  "clearOnHotbarSelect": true,
  "clearOnInventoryClose": false,
  "trackHotbar": true,
  "modColors": {
    "create": "#E0A96D",
    "farmersdelight": "#A7D489"
  },
  "itemColors": {
    "minecraft:netherite_ingot": "#795548",
    "minecraft:enchanted_golden_apple": "#FFD54F"
  }
}
```

### Color Modes
- `"DEFAULT"`: Uses `defaultColor` for all newly collected items.
- `"RARITY"`: Colors items by vanilla rarity (Common = yellow, Uncommon = mint green, Rare = sky cyan, Epic = lavender).
- `"AUTO_MOD"`: Automatically generates consistent pastel colors for different mods.

---

## Supported Loaders & Versions

| Minecraft Version | Fabric | Quilt | NeoForge | Forge |
|:---:|:---:|:---:|:---:|:---:|
| **1.21.x** (1.21 - 1.21.4) |  |  |  | — |
| **1.20.1** (1.20 - 1.20.6) |  |  |  |  |

---

## Installation

- **Fabric / Quilt**: Install Fabric/Quilt Loader and Fabric API, then place the Fabric jar into your `.minecraft/mods` folder.
- **NeoForge**: Install NeoForge and place the NeoForge jar into your `.minecraft/mods` folder.
- **Forge**: Install Forge and place the Forge jar into your `.minecraft/mods` folder.

This mod is 100% client-side. You can freely connect to vanilla servers, LAN worlds, or modded servers without the server needing the mod.

---

## License

Item Highlighter is licensed under the [MIT License](LICENSE).
