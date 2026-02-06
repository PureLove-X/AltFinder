# AltFinder

This plugin is based off the [AltFinder plugin](https://www.spigotmc.org/resources/altfinder.57678/) by egg82.

This is a simplified, modern rewrite and **does not currently support Bungee or Velocity** like the original plugin.

Usage is intentionally similar, but the original plugin was outdated and overly complex for our needs.
Since none of the original code is used, this is **not a fork**, but the original repository can be found here:
https://github.com/egg82/AltFinder/tree/master

---

## Legal & Privacy Notice

AltFinder stores IP address data **solely for the purpose of abuse prevention and moderation**.

Under certain privacy laws (including UK GDPR), IP addresses may be considered personal data.  
Server owners are responsible for ensuring that:

- IP data is only used for legitimate moderation or abuse-prevention purposes
- Data is not repurposed for tracking, profiling, or unrelated analytics
- Staff access to IP information is appropriately restricted
- Data retention complies with applicable local laws and regulations

By using this plugin, you acknowledge that **legal compliance is the responsibility of the server owner**, not the plugin author.

If you operate a server in a jurisdiction with strict privacy regulations, consult your legal guidance before use.

---

## Commands & Permissions

No permissions are granted by default. You must assign them manually.

### Player Lookup
- `/seen <player>`  
  View last seen time, possible alt accounts, and optional IP history.  
  **Permission:** `altfinder.seen`  
  **Optional:**
  - `altfinder.seen.ip` – view IP history
  - `altfinder.seen.alt` – view possible alt accounts

---

### AltFinder Commands
- `/altfinder search <player|ip> [page]`  
  Find potential alt accounts using full IP history.  
  **Permission:** `altfinder.search`

- `/altfinder delete <player|ip>`  
  Remove stored IP data for a player or IP.  
  **Permission:** `altfinder.delete`

---

### Relationship Management
- `/altfinder acknowledge <player1> <player2> [reason]`  
  Mark two accounts as a known relationship.  
  **Permission:** `altfinder.acknowledge`

- `/altfinder unacknowledge <player1> <player2>`  
  Remove an acknowledged relationship.  
  **Permission:** `altfinder.unacknowledge`

---

### Investigation Tools
- `/altfinder unresolved [page]`  
  View unacknowledged alt relationships.  
  **Permission:** `altfinder.unresolved`

- `/altfinder resolved [page]`  
  View acknowledged alt relationships and shared IPs.  
  **Permission:** `altfinder.resolved`

---

### General Permissions
- `altfinder.admin`  
  Grants access to all AltFinder commands.

- `altfinder.limit.bypass`  
  Bypasses concurrent alt login limits.

- `altfinder.notify`  
  Receive staff notifications for potential alt detection.


---

## Features

### Vanish Support

AltFinder integrates with **SuperVanish** and **PremiumVanish**.

- Vanished players appear **offline** in `/seen`
- Prevents staff accidentally revealing vanished players
- Notifications and detection still function correctly

---
### Alt Detection (IP-based)
AltFinder tracks **every IP a player has ever used**, not just their most recent one.

- Each `(player, IP)` pair is stored separately
- Detection uses **full historical IP overlap**
- A **single shared IP at any point** can flag a possible alt
- Designed to catch:
  - Traveling players
  - VPN hopping
  - Location-specific alt usage

The `/seen` and `/altfinder search` commands use this complete history.

---

### Concurrent Alt Limits

AltFinder can optionally limit how many accounts from the same IP may be online at the same time.

- Only **currently online** players are counted
- Historical IP data is **not** used for this check
- The check occurs **before the player fully joins**
- No bans or punishments are applied

#### Examples
- `concurrentAlts = 1`  
  One account per IP may be online at a time

- `concurrentAlts = 0`  
  Only a single account from an IP may ever be online at once

#### Bypass Permission
Players with the following permission are exempt from this limit:

- `altfinder.limit.bypass`

This is intended for staff accounts or approved shared households.

Concurrent limits are preventative only and do not affect alt detection, notifications, or `/seen`.

---

### Last Seen Tracking
AltFinder accurately tracks player login and logout state.

- Online players show **Online now**
- Offline players show **Last seen X ago**
- On server shutdown or restart, all online players are properly marked offline
- Prevents ghost “online” states

---

### IP History Display
The `/seen` command can optionally display **recent IP history**.

- Shows up to the **last 3 IPs** used
- Ordered by most recent activity
- Controlled by permissions
- Display-only; does not affect detection logic

---

### Possible Alt Listing
When using `/seen`, AltFinder lists **possible alt accounts**:

- Matches against **all historical IPs**
- Excludes the queried player
- Sorted alphabetically
- Shows total count

Example:
`Possible alt accounts: SteveAlt, Alex2 (2)`


---

### Staff Notifications
When a player joins and shares an IP with another account:

- Staff with `altfinder.notify` are notified
- Notifications respect acknowledged relationships
- Alerts are delayed slightly to appear after the join message

Example:
`Potential alt detected: PlayerName`


---

### Relationship Acknowledgement
`/altfinder acknowledge <playername> <playername2> <relationship>`

Staff can acknowledge known relationships to prevent repeated alerts.
- Acknowledgements can include an optional reason
- Relationships are symmetric (A ↔ B)
- Acknowledged relationships:
  - Do not trigger notifications
  - Appear under `/altfinder resolved`

**This currently does not bypass the concurrent alts check**

---

### Investigation Views

#### Unresolved
`/altfinder unresolved`
- Shows unacknowledged alt relationships
- Grouped by **player pair**, not IP
- Paginated for readability

#### Resolved
`/altfinder resolved`
- Shows acknowledged relationships
- Displays **all shared IPs**
- Shows the acknowledgement reason if provided

Example:
```
PureLove ↔ MrsServerDev 
Shared IPs: 127.0.0.1
Reason: "Dev Testing Account"
```


## Planned Improvements
- More hands on configuration
- Optional Litebans dependency where it will link with Litebans already existing system and commands. 
