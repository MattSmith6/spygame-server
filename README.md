# spygame-server

This is the server side code for the Spy Game project, which is a location based elimination game for CSUN students. The server's code handles features pertaining to account registration and verification, managing player connections and game features, as well as persisting the data with an SQL database. 

## General Overview

The server implementation in the Spy Game project is a bridge between the [Spy Game app](https://github.com/rgilyard/spygame) and the SQL database, which stores data pertaining to player accounts, player statistics, and game lobbies and records. 

The server's code itself can be broken down into three major components:
* The webserver
* The management of the database
* The player communication handler

### Webserver

The webserver is created using [Spark](http://sparkjava.com/) and handles features relating to player account registration, verification, and password resets.  

The following features are currently implemented under their respective [routes](https://github.com/MattSmith6/spygame-server/blob/main/src/main/java/com/github/spygameserver/auth/website/SparkWebsiteHandler.java#L25):
* Account registration
  * Following a successful account registration, an email is sent to the player with two links that can be clicked for the following features:
    * Account verification (required before logins)
    * Account disable (if the account was created in error)
* Password reset
  * The 'request' route handles the request for a reset, where a link to a password reset form POSTs the information to the 'doReset' route
* Check username
  * Checks if a username is already registered (as duplicate usernames are not allowed)
* Request username
  * Gets the username for an account that already exists
  
### Database Management

This project uses [HikariCP](https://github.com/brettwooldridge/HikariCP) for thread-safe connection pooling to the SQL database. A few classes were created to represent their respective namesakes in the SQL server: tables and databases.

#### [AbstractTable](https://github.com/MattSmith6/spygame-server/blob/main/src/main/java/com/github/spygameserver/database/table/AbstractTable.java)

This class includes utility functions for formatting queries with the appropriate table name as well as methods for disabling and reenabling key checks. A class that extends AbstractTable should represents a table located in the SQL server and include all relevant SQL queries within it, as well as methods to read and write data using those queries to the SQL server. Tables created in this project include ones representing all tables in the SQL server: player accounts, player game info, game lobbies, game records, and verification tokens.  

#### [AbstractDatabase](https://github.com/MattSmith6/spygame-server/blob/main/src/main/java/com/github/spygameserver/database/impl/AbstractDatabase.java)

This class provides methods to create new connections to the specific database utilizing the aforementioned connection pooling, as well as a method to initialize all tables in the database. A class that extends AbstractDatabase should represent a database present within the SQL server, as well as contain references to all tables in that database in the SQL server. The two databases in this project are the game and authentication databases.

### Player Communication Handler

The player communication handler uses sockets and multithreading to allow concurrent player connections to send encrypted data to, and receive encrypted data from, the server. 

#### Packets

Packets are a way for players and the server to communicate with an established protocol for each small feature a player may request access to (e.g. logging in, accessing a leaderboard, eliminating another player). Each packet has a unique integer id that is used to differentiate packets and provide consistent access to features. 

The process of sending a packet is simple. The player starts by sending an unencrypted packet id to the server so that the correct protocol established. After that, encrypted JSON objects are sent between the player and server as needed for each protocol.

An example packet protocol is listed below for when a [player joins a game](https://github.com/MattSmith6/spygame-server/blob/main/src/main/java/com/github/spygameserver/packet/JoinGamePacket.java):

```
            Player                                        Server
---------------------------------------------------------------------------------------

// App sends the unencrypted packet id, then the encrypted JSON object payload: the invite code for the game

              11   -----------------------------------> 
   { "invite_code": "AAAAA" }   ----------------------> 

// Server processes the information given by the app and sends an encrypted JSON object response back

                  <------------------------------------  { "success": [true/false] }
                  
// App receives the response and updates the UI as necessary, or initiates another packet protocol

```


#### Data Encryption

The process of setting up the encryption for a player connection happens through the login packet. The protocol for Secure Remote Password, or [SRP-6](https://en.wikipedia.org/wiki/Secure_Remote_Password_protocol), is a 'login' process that sends unencrypted data between the player and the server. This process uses a mathematical proof that verifies the identites of the client and server to each other without the transmission of data that can compromise the player's account. The result of a successful authentication proof with SRP-6 is an encryption key which we use to encrypt the data for each player.

## Unimplemented Features

As of time of last updating, the following features have either not been tested or implemented for the server side code:
* Game record table
* Game record packet
* Notifications sent to the app
