# Changelog

## Future

- Stock Market
- Finish purchase handler (mainly a command to view purchases, and importer/exporter support)
  - This allows us the start working on the stock market
- Gui improvements

## 1.2.2-1.4.0

### Added

- Admin shops can now be given names, the contents of each admin shop depends on its name
  - You can give an admin shop a name by holding an admin shop and running `/adminshop name`
  - If advanced tooltips is enabled, you can hover over an admin shop in your inventory to get its name
  - Translation keys can be used as a shop's name, and will be replaced correctly
- `/adminshop` has 3 new sub-commands, which are operator only
  - `default` opens the default admin shop (no name)
  - `named <admin_shop_name>` opens the admin shop with the specified name
  - `name <admin_shop_name>` names the admin shop in your hand to the specified name (this is different to using an anvil)
- You can now price multiple items at once with the bulk item, fluid, and admin shop GUIs
  - These are accessed by doing `/economy price bulk <gui>`

### Changed

- `/adminshop` now opens the admin shop with the name specified in the config
- Translation keys can be used as names for categories in the admin shop

## 1.2.2-1.3.3

### Changed

- The mod should now support both create `6.0` and `0.5`
- Manifests and the shopping cart screen now also use an item's custom name instead of its default name

## 1.2.2-1.3.2

### Fixed

- Fixed issues with `1.3.1`

## 1.2.2-1.3.1

### Changed

- Switched to Create 6.0
- The admin shop will now use an item's custom name instead of its default name
- Category icons now include NBT
- Changed how fluid tanks are rendered in the fluid importer and exporter GUIs

## 1.2.2-1.3.0

### Added

- Added a new /economy command
  - This replaces the /price command
- Added fluid variants of exporters and importers
  - Added ponders to go with these new variants
- You can now customize the amount of items an importer or exporter can buy/sell at once
  - This depends on the max process count of the used manifest
  - You can change the max process count using `/economy manifest max_process_count`

### Fixed

- Fixed a server-side only crash from `1.2.0`

## 1.2.2-1.2.0

### Added

- Added ponders (Requires Create)
- Added `/price reload` to reload prices without restarting the server

### Fixed

- Fixed an issue where importers used the wrong config option

## 1.2.2-1.1.0

### Changed

- You now get given change rather than going into capacity overflow

## 1.2.2-1.0.0

### Changed

- The [Create Mod](https://www.curseforge.com/minecraft/mc-mods/create) is now an optional dependency

### Added

- Added an infinite wallet
