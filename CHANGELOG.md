# Changelog

## Future

- Stock Market
- Finish purchase handler (mainly a command to view purchases, and importer/exporter support)
  - This allows us the start working on the stock market
- Gui improvements
- Bulk item/fluid pricing

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
