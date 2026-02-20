# MatchVault

A powerful addon for **MBedwars** that tracks detailed per-match player statistics and allows players to view their complete match history.

MatchVault provides deep analytics for every Bedwars match — from combat performance to resource collection and shop activity.

---

## Features

### Match Overview
- Match start & end time
- Total match duration
- Final team placement
- Team eliminations
- Bed breaks

### Combat Statistics
- Kills
- Final kills
- Deaths
- Highest kill streak
- Beds broken

### Resource Tracking
Tracks total resources collected:
- Iron
- Gold
- Diamonds
- Emeralds

Also tracks:
- Resources collected specifically from spawners

### Shop & Upgrades
- Shop purchases
- Upgrades purchased

### Activity Tracking
- Total time actively played in the match

---

## Commands

| Command | Description |
|----------|------------|
| `/matchvault` | Main command |
| `/matchvault reload` | Reload configuration |
| `/bw matches [player]` | View your own or another player's match history |

---

## Permissions

| Permission | Description |
|------------|------------|
| `matchvault.commands.history` | View your own history |
| `matchvault.commands.history.others` | View other players' history |
| `matchvault.commands.reload` | Reload configuration |

---

## Installation

1. Place the addon inside:  
   `plugins/MBedwars/add-ons`
2. Restart your server.
3. You're ready to go

---

## Data Storage

MatchVault supports the following databases:

- **MySQL**
- **MariaDB**
- **H2** (Recommended)
- **SQLite**

> H2 is recommended over SQLite for better performance and reliability.

---

## Support

Need help?

- Join our **Discord**:  
  https://discord.gg/vSuKz7dfve

- Report issues or check updates on **GitHub**:  
  https://github.com/Hosairis/MBedwars-MatchVault