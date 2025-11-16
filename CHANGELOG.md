# Changelog

## Future

- Stock Market
- Gui improvements

## 1.2.2-1.6.2 (WIP)

### Changed

- **Prices File Format**: Prices are now saved as NBT instead of JSON, old files will be converted and then deleted.
  - **What This Means**: This should fix many NBT issues in the admin shop including the inability to save tools and such!
  - **File Editing**: To view or edit the file, consider using a program like NBTExplorer.
  - **Backups**: Consider backing up your old prices file in case the mod fails to convert the file. This shouldn't be necessary but can't hurt.
- **JEI Integration**: Locked prices are now hidden from JEI.

### Fixed

- **Sizing Issues**: Categories in the admin shop's category panel are now sized correctly when the scrollbar is visible.
- **Scrollbars**: Scroll bars in the admin shop are now draggable.
- **Clicking Off-Screen Elements**: You can no longer click scrollbar elements that are off-screen. 
  - **What This Means**: This should fix issues with not being able to press the purchase button when not scrolled all the way down.

## 1.2.2-1.6.1

### Added

- **Purchase Events**: Events are fired after a purchase, to be used by addon mods.
- **Purchase Tracking**: Purchases are now tracked and stored in the purchases.dat file found in a world's data folder.
- **Data Reset Commands**: Data can be reset through commands.
  - **Reset All**: `/economy reset_all`
  - **Reset Admin Shop Colors**: `/adminshop color reset_all`
  - **Reset Prices**: `/economy price reset`
  - **Reset Purchases**: `/economy purchases reset`

### Changed

- **Admin Shop Colors**: Admin shop colors are now saved per instance instead of per save, so that colors are the same across all saves.

## 1.2.2-1.6.0

### Added

- **[JEI](https://www.curseforge.com/minecraft/mc-mods/jei) Support**: You can now use **JEI** to view exporting, importing, fluid exporting, fluid importing, admin shop buying, and admin shop selling recipes
- **Golden Wallet**: The golden wallet has infinite money, allowing you to buy anything you want

### Fixed

- **Fixed a Crash on Dedicated Servers**: The admin shop would cause a crash when loaded [(Issue #2)](https://github.com/SCsupercraft/JacksEconomyForked/issues/2)
- **Fixed a Crash on Dedicated Servers**: Bulk pricing screens would crash on dedicated servers

## 1.2.2-1.5.0

### Added

- **Manifest Usage Limits**: You can now define a maximum number of uses for manifests via `/economy manifest max_uses`.
- **Admin Shop Coloring**: Admin shops can be color-coded using `/adminshop color` to help differentiate them. This is especially useful since each shop's inventory is linked to its name.

## 1.2.2-1.4.1

### Changed

- `/adminshop named` and `name` both give suggestions if a named admin shop already exists
- Changed how fluid tanks are rendered in the fluid importer and exporter GUIs for the final time

### Fixed

- The game no longer crashes when selecting a fluid in the fluid importer GUI

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
- Improved scaling and positioning of text in the admin shop GUI

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

### Added

- There is now an infinite wallet

### Changed

- The [Create Mod](https://www.curseforge.com/minecraft/mc-mods/create) is now an optional dependency
