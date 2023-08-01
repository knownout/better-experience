# Better Experience

Transform your experience in something really usable.

```text
Tested minecraft versions: 1.20.1
Native version: 1.20.1
API level: 1.20
```

This plugin allows you to configure generic attributes of player based on it's level,
create experience bottles for self experience and use some abilities after reaching high levels

## Experience bottles expansion

Players now can create experience bottles from themselves experience points using specified in config blocks.
By default, players can use enchanting table and anvil.

To create experience bottle, with default configuration, just sneak and right click on one of specified blocks.

### Locking and unlocking

Players can protect their experience bottles from being used by other players.

To lock bottle, take it in main hand and then just right click on one of specified blocks, unlocking works same.

Lock, unlock and use locked bottles can only bottle owner and players with `betterexperience.bottles.useLocked`
permission.


## Evolve
Evolve system allow players to increase their health, strength and receive some other benefits from their level.

### Generic attributes list

Attributes that plugin uses by default:

| Attribute               | Description                                                                                            | Default | Min | Max    |
|-------------------------|--------------------------------------------------------------------------------------------------------|---------|-----|--------|
| `GENERIC_MAX_HEALTH`    | Maximum health of player                                                                               | 20.0    | 0.0 | 1024.0 |
| `GENERIC_ATTACK_DAMAGE` | Damage dealt by attacks, in half-hearts                                                                | 2.0     | 0.0 | 2048.0 |
| `GENERIC_ATTACK_SPEED`  | Determines recharging rate of attack strength.Value is the number of full-strength attacks per second. | 4.0     | 0.0 | 1024.0 |

All attributes list: https://minecraft.fandom.com/wiki/Attribute

### Damage protection

Damage protection will subtract specified percent of damage from final damage (after subtracting damage
absorbed by armor and effects) before player receive it.

Values is set on percents divided by 100 — from 0 to 1

### Abilities

Abilities are mechanics that allow the players to apply certain effects to themselves using specified in config items.
Each ability has a usage price in experience points that will be subtracted from player after each usage.

`enablePriceImpact` option enables price increases when player applies too long effects and
will increase price from 0 to 100% per usage.

## To-do list
- [ ] Create GUI to view own evolve stats
- [ ] Fix permissions system

## Permissions

All permissions disabled by default, you can enable it in plugin config

| Permission                                    | Description                                   | Default      |
|-----------------------------------------------|-----------------------------------------------|--------------|
| `betterexperience.bottles.create`             | Allow player to create experience bottles     | no op        |
| `betterexperience.bottles.lockUnlock`         | Allow player to lock experience bottles       | no op        |
| `betterexperience.bottles.useWithoutBreaking` | Allow player to use bottles without breaking  | no op        |
| `betterexperience.bottles.useLocked`          | Allow player to use locked experience bottles | op, disabled |
| `betterexperience.evolve`                     | Allow player to evolve                        | no op        |
