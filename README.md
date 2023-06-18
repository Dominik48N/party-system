# PartySystem
The PartySystem is a plugin for BungeeCord and Velocity that allows players to create a "party" and invite other players to join. When the party leader joins a game, the party members are automatically taken with them. This plugin also supports multi-proxy environments, meaning that it can be used across multiple BungeeCord or Velocity servers. Communication between these servers is facilitated by Redis.

## Features
* Create and manage parties with a user-friendly interface
* Invite other players to join your party
* Automatic synchronization of party members across multiple servers
* Support for BungeeCord and Velocity proxy servers
* Redis-based communication for multi-proxy support
* Party Settings (Requests, Join/Quit Notifications, Party Chat)

## Installation
* Download the latest release of the PartySystem plugin.
* Place the plugin in your server's plugins directory.
* Restart the server.
* Configure the plugin in `config.json`.
* Restart the server again.

## Usage
Once the plugin is installed and configured, players can create parties using the `/party` command. From there, they can invite other players to join their party, and when the party leader joins a game, the party members are automatically taken with them.

## Permissions
There is the permission `party.updates` to get a message when there is a new version of the party system.

You can adjust the member limit for a party leader with the permission `party.members.limit.<limit>`. **The value must be between 0 and 1,000.** If a player does not have a limit permission, the default member limit is taken from the config.

## Database
A database system is not required to use the party system. But if you want to use the feature that players can set themselves whether they want to receive party requests, join/quit messages and party chat messages, you have to use a database system.

Four database systems are currently supported.
* [MongoDB](https://www.mongodb.com)
* [PostgreSQL](https://www.postgresql.org)
* [MariaDB](https://mariadb.org)
* [MySQL](https://www.mysql.com/)

In order to use a supported database system, you have to go to the `database` sub-item in `config.json`. Then you have to set `enabled` to `true` and enter a supported database system under `type`. Finally, enter the connection data for your selected database system and restart the proxy.

## API
The Party API is a Java library that provides an interface for managing parties of players. It includes methods for creating, joining, and leaving parties, as well as sending messages to party members and connecting parties to servers.

To use the API, you can create an instance of the `PartyAPI` class and use the `get()` method to obtain the current `PartyProvider` instance. The `PartyProvider` interface includes methods for managing parties, such as adding players, changing the party leader, and deleting a party.

For more detailed information about the API and its methods, please refer to the [Wiki](https://github.com/Dominik48N/party-system/wiki/API).

## Contributing
Contributions to the PartySystem plugin are welcome! If you would like to contribute, please follow the guidelines in the [CONTRIBUTING.md](CONTRIBUTING.md) file.

## License
The PartySystem plugin is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0). See the [LICENSE](LICENSE) file for more information.
