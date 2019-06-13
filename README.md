# CraftBay

Auction off your precious items! CraftBay is an auctioning system that allows players to trade their items by bidding for them. After a set amount of time, the highest bid wins. Payment is handled via **Vault**.

## Links
- [GitHub page](https://github.com/StarTux/CraftBay) *(Source code)*
- [SpigotMC resource page](https://www.spigotmc.org/resources/craftbay.58813/) *(1.13+ downloads)*
- [BukkitDev plugin page](https://dev.bukkit.org/projects/craftbay) *(Current and legacy downloads)*
- [Configuration file](https://github.com/StarTux/CraftBay/blob/master/src/main/resources/config.yml) *(yaml)*
- [Language file](https://github.com/StarTux/CraftBay/blob/master/src/main/resources/lang/en_US.yml) *(yaml)*

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

## Configuration
This is the content of the default `config.yml` file:
```yaml
# The initial duration of any auction
auctiontime: 600
# How much can people shorten their auctions (prevents exploits)
minauctiontime: 60
# The delay after which an auction reminder is broadcast
reminderinterval: 120
# The minimum delay between two messages
spamprotection: 3
# The minimal amount a player has to raise for
minincrement: 5
# The percentage of the current pot which the next bidder has to raise for
minraise: 0.05
# The maximal amount of stored future auctions
maxqueue: 1
# The maximal amount of stored past auctions
maxhistory: 10
# The language file to use
lang: en_US
# The starting bid of any auction
startingbid: 10
# A flat fee for starting an auction
auctionfee: 0
# The percentage of the additional starting price an auctioneer has to pay
auctiontax: 0
# The time a player has to wait between starting two auctions
auctioneercooldown: 30
# The pause between two consecutive auctions in seconds
auctionpause: 5
# Prevent players from starting auctions while in creative mode
denycreative: true
# The final seconds at which the countdown is broadcast in chat
countdown: [10, 5, 3, 2, 1]
# A list of worlds where CraftBay cannot be used
blacklistworlds: []
# Expiration time of stored items in days
itemexpiry: 30
# If true, people cannot bid on the same auction twice in a row.
denydoublebid: false
# Verbose logging to console
debug: false
# DefaultChat configuration
defaultchat:
        # Are players listening by default?
        autojoin: true
# Colors
colors:
        default: dark_aqua
        header: yellow
        highlight: aqua
        shadow: dark_gray
        shortcut: white
        admin: dark_red
        adminhigh: red
        error: dark_red
        warn: red
        warnhigh: dark_red
```

## Language
All in-game chat output is configurable and CraftBay comes with several language files: English (en_US), German (de_DE), Simplified Chinese (zh_CN), and Russian (ru_RU). The preferred language can be chosen in the configuration file and adjusted via configuration files.

This is the content of the default `en_US` language file:
```yaml
# File: plugins/CraftBay/lang/en_US.yml

Tag: 'Auction:'
# Command help, accessed via "/auc help"
help:
  Header: '&eAuction Help'
  Help: '&3/auc &b?&3 &eAuction help'
  Info: '&3/auc &fi&bnfo &eDisplay auction information'
  Bid: '&3/auc &fb&bid &3[&bamount&3] &ePlace a bid'
  BidShort: '&3/bid &3[&bamount&3] &ePlace a bid'
  Start: '&3/auc &fs&btart &3[&bprice&3] &eStart an auction'
  Hand: '&3/auc &bhand &3[&bprice&3] &eAuction the item in your hand'
  Fee: '&3Starting an auction costs &b{fee}&3.'
  Tax: '&3Starting bids above &b{minbid}&3 will cause &b{tax}%&3 tax.'
  End: '&3/auc &fe&bnd &3[&bminutes&3:&bseconds&3] &eEnd current auction'
  Listen: '&3/auc &blisten&3|&bignore &eListen to or ignore auctions'
  History: '&3/auc &bhistory &3[&bid&3] &eView past or queued auctions'
  Cancel: '&3/auc &fc&bancel &3[&bid&3] &eCancel an auction'
# Admin command help, accessed via "/auc help" for admins
adminhelp:
  Bank: '&4/auc &cbank &4\<&citem&4\> [&camount&4] &4[&cprice&4] [&ctime&4] &eAuction spawned items'
  Bank: '&4/auc &cbankhand &eAuction item in hand on behalf of the bank'
  BankBid: '&4/auc &cbankbid &4\<&camount&4\> &ePlace a bid on behalf of the bank'
  Reload: '&4/auc &creload &eReload configuration file'
  Log: '&4/auc &clog &4[&cid&4] &eView auction log'
  Fake: '&4/auc &cfake &4\<&ctitle&4\> [&camount&4] [&cprice&4] &eAuction off a fake item'
# Error messages during command line parsing
# Environment:
#  player - the name of the player issuing the command
#  cmd - the issued subcommand
#  arg - the erroneous argument (if applicable)
command:
  NoEntry: '&cNo such command: "{cmd}"!'
  NoPerm: "&cYou don't have permission!"
  BadWorld: '&cYou cannot do that in this world!'
  NotAPlayer: '&cOnly players can do that!'
  NoCurrentAuction: '&cNo auction running!'
  NoSuchAuction: '&cUnknown such auction: &4{arg}&c!'
  ArgsTooSmall: '&cNot enough arguments!'
  ArgsTooBig: '&cToo many arguments!'
  NotANumber: '&cNumber expected: &4{arg}&c!'
  BadTimeFormat: '&cBad time format: &4{arg}&c!'
  NoSuchItem: '&cInvalid item: &4{arg}&c!'
  IllegalItem: '&cInvalid item: &4{arg}&c!'
  UnclosedQuote: '&cNo closing quote!'
# Error messages from commands
# Environment:
#   player - the name of the player issuing the command
#   auction variables where applicable, see auction.*
#   arg - the erroneous argument, if applicable
commands:
  listen:
    AlreadyListen: You are already listening to auctions!
    AlreadyIgnore : You are already ignoring auctions!
    ListenError: An error occured trying to perform this command!
    IgnoreError: An error occured trying to perform this command!
    ListenSuccess: Listening to auctions
    IgnoreSuccess: Ignoring auctions
  history:
    NoEntry: '&cNo such id: {id}'
  start:
    HandEmpty: '&cThere is nothing in your hand!'
    AmountTooSmall: '&cPositive amount expected!'
    CreativeDenial: '&cYou cannot start auctions in creative mode!'
    Success: '&3Your auction for &b{amount}&3x&b{itemdesc}&3 will start soon!'
  end:
    NotOwner: '&cYou are not the owner of this auction!'
    DelayTooLong: '&cYou can only shorten your auction!'
    DelayTooShort: '&cThe delay must be at least {min}!'
    DelayNegative: '&cPositive delay expected!'
  cancel:
    NotOwner: '&cYou are not the owner of this auction!'
    Running: '&cYou cannot cancel a running auction!'
    Canceled: '&cThis auction is already canceled!'
    Ended: '&cThis auction is over!'
  fake:
    Fail: '&cCreating fake auction failed'
    Success: '&3Created fake auction successfully'
# Error messages from auctions
# Environment (where applicable):
#   id - the auction id
#   item - the item name
#   itemdesc - a description of the item (the name and whether it is enchanted ot not)
#   amount - the item amount, user friendly
#   totalamount - the item amount, bare number
#   iteminfo, enchantments - mixed item information
#   owner - the owner's name
#   winner - the winner, if any
#   minbid - smallest possible bid to participate
#   maxbid - highest placed bid
#   price - the current price
#   state - the state of this auction
#   timeleft - the remaining auction duration
#   fee - the total fee
auction:
  create:
    QueueFull: '&cWait until other auctions have ended!'
    NotEnoughItems: '&cYou do not have enough &4{itemdesc}&c!'
    FeeTooHigh: '&cYou cannot afford the fee of &4{fee}&4!'
    FeeDebited: '&3Your account has been debited &b{fee}&3.'
    OwnerCooldown: '&cYou have to wait &4{cooldown}&c!'
  start:
    Announce: '&b{owner}&3 is auctioning &b{amount}&3x&b{itemdesc}&3 for &b{minbid}&3. &bClick here.'
  gui:
    ChestTitle: 'Items for auction'
    ItemsNotEqual: '&cAll items must be identical!'
    Success: '&3Your auction for &b{amount}&3x&b{itemdesc}&3 will start soon!'
  state:
    Queued: Queued
    Running: Running
    Canceled: Canceled
    Ended: Ended
  info:
    Header: '&eAuction Information'
    Owner: '&3Auctioneer: &b{owner}'
    RealItem: '&3Item: &b{totalamount}&3x&b{item}&3 &b{iteminfo}&3'
    FakeItem: '&3Item: &b{item}'
    Winner: '&3Winning: &b{winner}&3 for &b{price}'
    NoWinner: '&3Minimal bid: &b{minbid}'
    Self: '&3Your bid: &b{maxbid}'
    Time: '&3Time left: &b{timeleft}'
    State: '&3State: &b{state}'
    Fee: '&3Fee: &b{fee}'
    Help: '&3Type &b/auc ?&3 for a list of commands.'
  reminder:
    NoWinner: '&3Auction for &b{amount}&3x&b{itemdesc}&3 ends in &b{timeleft}&3. Price: &b{minbid}&3. &bClick here.'
    Winner: '&b{winner}&3 wins &b{amount}&3x&b{itemdesc}&3 for &b{price}&3 in &b{timeleft}&3. &bClick here.'
  countdown:
    NoWinner: '&3Auction for &b{amount}&3x&b{itemdesc}&3 ends in &b{timeleft}&3!'
    Winner: '&b{winner}&3 wins &b{amount}&3x&b{itemdesc}&3 in &b{timeleft}&3!'
  bid:
    IsOwner: "&cYou can't bid on your own auction!"
    UnderbidSelf: '&cYou have already made a larger bid!'
    DoubleBid: '&cYou cannot bid twice in a row!'
    BidTooSmall: '&cYou must bid at least &4{minbid}&c!'
    TooPoor: '&cYou do not have enough money!'
    Win: '&3You are now winning this auction for &b{price}&3!'
    Fail: '&b{winner}&3 is still winning but you raised the price to &b{price}&3!'
    ToLoser: '&3{winner} just beat you with {price}!'
    NewPrice: '&3Price for &b{amount}&3x&b{itemdesc}&3 is now &b{price}&3 for &b{winner}&3'
    NewWinner: '&b{winner}&3 beats {oldwinner}. Price &b{price}'
    UnderBid: '&3{player} raises but &b{winner}&3 is higher. Price &b{price}'
    Still: '&3You are still winning this auction for &b{price}&3.'
  end:
    ToWinner: '&3Enjoy your &b{amount}&3x&b{itemdesc}&3 for &b{price}&3!'
    OwnerSell: '&3Your &b{amount}&3x&b{itemdesc}&3 has been sold to &b{winner}&3 for &b{price}&3.'
    OwnerReturn: '&3Your &b{amount}&3x&b{itemdesc}&3 has been returned to you.'
    OwnerPaymentError: '&cError processing payment. Auction canceled.'
    Winner: '&b{winner}&3 buys &b{amount}&3x&b{itemdesc}&3 for &b{price}&3.'
    WinnerPaymentError: '&cError processing payment. Auction canceled.'
    PaymentError: '&4{winner}&c cannot afford the &4{price}&c he bid. Punish him!'
    NoBid: '&3Auction for &b{amount}&3x&b{itemdesc}&3 ended with no bids.'
    Manual: '&b{player}&3 ended the auction.'
  cancel:
    Announce: '&4Auction canceled by &c{player}&4!'
    ToOwner: '&cYour auction has been canceled!'
    FeeReturn: '&3Your fee of &b{fee}&3 has been returned to you.'
  time:
    Change: '&b{player}&3 set the remaining time to &b{newtimeleft}&3.'
# Special item describing strings not covered by Vault
item:
  damaged:
    Singular: 'Damaged'
    Plural: 'Damaged'
  enchanted:
    Singular: 'Enchanted'
    Plural: 'Enchanted'
  minute:
    Singular: 'minute'
    Plural: 'minutes'
  second:
    Singular: 'second'
    Plural: 'seconds'
  stack:
    Singular: 'Stack'
    Plural: 'Stacks'
  chest:
    Singular: 'Chest'
    Plural:   'Chests'
  doubleChest:
    Singular: 'Double Chest'
    Plural:   'Double Chests'
  inventory:
    Singular: 'Inventory'
    Plural: 'Inventories'
  page:
    Singular: 'page'
    Plural: 'pages'
  book:
    ByAuthor: ' by '
# Display of the history.
# Environment: see auction.*
history:
  Header: '&eAuction History'
  Queue: '&8[{id}] {owner} &b{totalamount}&3x&b{itemdesc}'
  Current: '&e[{id}] {owner} &b{totalamount}&3x&b{itemdesc}'
  History: '&3[&b{id}&3] {owner} &b{totalamount}&3x&b{itemdesc}'
log:
  Header: '&eAuction Log [{id}]'
  Log: '&4{log}'
```