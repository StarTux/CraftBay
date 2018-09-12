# CraftBay

Auction off your precious items! CraftBay is an auctioning system that allows players to trade their items by bidding for them. After a set amount of time, the highest bid wins. Payment is handled via **Vault**.

## Intended use
Despite the wide availability of shopping plugins, an auction is still the best way for auctioneers to achieve the maximum price for their goods by reaching a broad audience and having them duke it out. Experience shows that especially rare items such as enchanted tools and armor can make for a very intense and exciting fight for the highest bid. The winning bid may even exceed the price for the same item available in shops.

## Features
- Players can auction off items from their inventory
- Bid for items with your in-game currency
- World blacklist
- Live notifications about auction activity (players can mute it)
- Spam protection: Suppress messages when people spam bids
- Fully customizable messages
- Admin commands to moderate auction activity
- Auction history for in-game review
- Auctions survive server restarts
- Informative display of item properties (damage, enchantments, etc)
- Auctions will survive a server restart.
- A cancelled auction or one that ends without any bids will return the item to the owner.
- If items are handed over to a player with a full inventory, they will be dropped where they are standing.

## Commands
Auctions are managed with simple commands with an interactive on-line help.

- **`/auction`** This command will always direct you to an overview of your current options.
- **`/auc`** An alias for /auction that works with all of the below
- **`/auc ?`** Get help
- **`/auc info`** Display information about the current auction
- **`/auc preview`** Preview the auctioned item in a chest interface
- **`/auc history [id]`** Review past or queued future auctions
- **`/auc bid [amount]`** Place a bid
- **`/bid [amount]`** Shortcut for /auction bid
- **`/auc start [starting price]`** Auction off an item
- **`/auc hand [starting price]`** Auction off the item in your hand
- **`/auc end [delay]`** End the current auction
- **`/auc cancel [id]`** Cancel an auction
- **`/auc ignore`** Ignore broadcasts
- **`/auc listen`** Receive broadcasts

## Admin Commands
Admins have additional commands to get more information than normal users, moderate the market activity, auction off spawned items or stimulate the economy by bidding for items on behalf of the bank.

- **`/auc bank <item> <amount> [starting price]`** Auction off a spawned item
- **`/auc fake <name> [starting price]`** Auction off a fake item
- **`/auc bankhand`** Auction off item in hand on behalf of the bank
- **`/auc bankbid [amount]`** Place a bid on behalf of the bank
- **`/auc log [id]`** Display the log of an auction
- **`/auc reload`** Reload the configuration file

## Permissions
The defaults are set up so anyone can query auction information and place bids. Permission to start auctions, however, is not granted by default. Administrative actions default to op.

- **`auction.info`** Query information about the current auction
- **`auction.bid`** Place bids for auctions. Implies auction.receive
- **`auction.start`** Start your own auction
- **`auction.admin`** Perform administrative commands
- **`auction.receive`** Receive items you won (since v1.2.1)
- **`auction.nofee`** Player is exempted from the auction fee
- **`auction.notax`** Player is exempted from the auction tax

## How it works
Every player with the appropriate permission can start an auction for any amount of items in their inventory. To do so, they can simply type /auc hand to sell whatever they are holding in their hand, or /auc start to be presented with a chest interface to put their items in. If everything is alright, the auction will be publicly announced and the plugin will claim the item and start accepting bids.

Placing a bid means declaring the **maximum amount** you are willing to pay for the item. After the auction times out, the participant with the highest bid wins, but all he has to pay is **just enough to beat the second highest bid**. This is how many online auction houses function.

The result of this system is that sniping, that is overbidding by a small amount in the last second, becomes very difficult. Also players don't have to worry about placing an unnecessarily high bid when nobody else is interested in paying nearly as much. They can just declare the maximum amount right away and be guaranteed to pay up to that but no more, and only as much as is necessary to win. Also, nobody else will know about it unless they bid even more.

## Language
All in-game chat output is configurable and CraftBay comes with several language files: English (en_US), German (de_DE), Simplified Chinese (zh_CN), and Russian (ru_RU). The preferred language can be chosen in the configuration file and adjusted via configuration files.
