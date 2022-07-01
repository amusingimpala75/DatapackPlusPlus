# Datapack++

Datapack++ extends the functionality of the datapacking system built into Minecraft.

### Features:
- [ ] Items
  - [x] Can load
  - [x] Can reload
  - [x] Custom Codecs
  - [x] Reload addition
  - [x] Reload removal
  - [x] Proper reload replacement (Inventories, Item Entities, etc.)
  - [ ] Component System
- [ ] GUI for editing in game
  - [ ] Permissions Handling
- [ ] Blocks
- [ ] Fluids
- [ ] Molang Support
- [ ] Mob Effects

Feel free to open an issue if you would like to see any new features, just add a "Feature Request" tag.

##### Items:

For datapack item data/dpp/dpp/item/test_item.json: 
```json
{
  "codec_name": {
    "xp_amount": 3
  }
}
```
would register the item "dpp:test_item" using the codec "codec_name", and would use the "xp_amount" as one of its field (or complain if it is invalid)