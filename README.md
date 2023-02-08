# Minesweeper

###### By TeddyBear_2004

Welcome to our Minesweeper plugin for Spigot! With this plugin, you can play the popular puzzle game directly on your
Minecraft server. This plugin is designed for various Minecraft version until 1.19.2.

### Prerequisites

- A Minecraft server running Spigot 1.19.2
- A functioning Minecraft installation to test the plugin

### Installation

- Download the latest version of the Minesweeper plugin from our GitHub page.
- Copy the plugin into the plugins directory of your Minecraft server.
- Start or restart your Minecraft server to activate the plugin.

### Configuration

The Minesweeper plugin includes a config.yml file where various settings can be configured. Here are some of the key
options included in the configuration file:

#### resource_pack

In this section, you can set whether or not a resource pack should be used for the Minesweeper game and, if so, how it
should be loaded. You have the option to use either an internal or external resource pack. If you want to use an
internal resource pack, you will need to enable the web server and specify the details of the web server. If you instead
want to use an external resource pack, you will need to provide a link to the pack.

#### allow_fly

With this option, you can set whether or not players are allowed to fly during the Minesweeper game.

#### allow_default_watch

This option sets whether or not players are allowed to use the default watch inventory during the Minesweeper game.

#### events

In this section, you can set which events should be blocked during the Minesweeper game. You can block any of the events
listed in the configuration file by setting the value to true.

#### location_based

With this option, you can define specific areas on your server where certain actions will be taken when a player is
within that area. You can specify actions such as temporary fly and block certain events in these areas. You can define
multiple areas by providing the coordinates for each area and the world in which it is located.

#### use_default_map

If this option is set to true, players will be able to use the default Minesweeper map when starting a game. If it is
set to false, players will need to specify a map to use.

#### available_games_inventory_lines

This option sets the number of inventory lines that will be used to display the available games in the Minesweeper menu.

#### games

In this section, you can define specific Minesweeper games that players can choose from. Each game includes options such
as the corner and spawn location of the map, the border size and bomb count, and the inventory material and position in
the menu.

### Usage

To start a game of Minesweeper, use the item in the inventory.

Once the game has started, you can use right-click to place or remove a flag on a cell and left-click to uncover a cell.
If you hit a mine, the game is over. If you manage to flag all the mines on the board, you win the game.

We hope you enjoy playing Minesweeper on your server! If you have any questions or feedback, don't hesitate to contact
us.