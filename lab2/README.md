# Lab 2 — A coffee shop server

## Structure

```
lab2/
├── server/                      # C#/.NET 10 TCP server
│   ├── Program.cs               # entry point
│   ├── Log.cs                   # writes the event log
│   ├── Network/
│   │   ├── Server.cs            # sockets
│   │   ├── ClientConnection.cs  # line framing and sends
│   │   └── OrderParser.cs       # command parsing
│   └── Shop/
│       ├── Stock
│       ├── Menu
│       ├── Product
│       ├── Order
│       └── Kitchen              # worker pool
├── client/                      # Java console CoffeeClient (runs as client or provider)
│   └── src/
│       └── main/
│           └── java/
│               └── org/
│                   └── example/
│                       └── lab2/
├── docs/                        # report
│   └── proofs/                  # screenshots of the defended scenarios
└── prerequisites/               # lab assignment
```

## Requirements to build

- JDK 25
- .NET 10

## Build and run

From this folder (`lab2/`):

```sh
# Server
dotnet build server/server.csproj
dotnet server/bin/Debug/net10.0/server.dll 5000
# alternatively: dotnet run --project server -- 5000

# Client (recommended: run as an executable .jar)
mkdir -p client/out
javac -d client/out client/src/main/java/org/example/lab2/CoffeeClient.java
jar --create --file client/CoffeeClient.jar --main-class org.example.lab2.CoffeeClient -C client/out .

java -jar client/CoffeeClient.jar 127.0.0.1 5000          # client (default role)
java -jar client/CoffeeClient.jar 127.0.0.1 5000 provider # provider
```

Type `help` in the client for its commands. Use `quit` to disconnect.

## API (server ↔ client / provider)

Plain TCP. One UTF-8 message per line, ended by `\n` (`\r\n` is accepted); split or coalesced receives are reassembled. Lines longer than 4096 bytes get `ERR MESSAGE_TOO_LONG`. Items are `name=quantity` pairs (`coffee`, `tea`, `croissant`; quantity is a positive integer). Preparation time is the sum of item times: coffee 4 s, tea 3 s, croissant 2 s.

| Direction | Message | Meaning / reply |
|---|---|---|
| → server | `HELLO client` / `HELLO provider` | Announce role; `OK HELLO`, or `ERR BAD_ROLE <role>` / `ERR PROVIDER_EXISTS`. Only one provider at a time. |
| → server | `MENU` | `OK MENU coffee=<n> tea=<n> croissant=<n>` with current stock. |
| → server | `ORDER coffee=2 tea=1` | Stock is reserved at once; immediate `OK ORDER <id> <prepSeconds>`, later `EVENT READY <id>`. Errors: `ERR BAD_FORMAT …`, `ERR UNKNOWN_PRODUCT <name>`, `ERR BAD_QUANTITY <item>`, `ERR NOT_ENOUGH <name>=<left> …`. |
| → server | `CANCEL` | Cancel this connection's waiting/preparing orders and return products to stock (no reply). Disconnecting does the same automatically. |
| → server | `DELIVER coffee=20 croissant=10` | Provider only; adds to stock at once → `OK DELIVER`. Others get `ERR NOT_PROVIDER`; a registered provider may only deliver. |
| server → | `EVENT READY <id>` | Sent asynchronously when the order is prepared, even while the user types. |
| server → | `EVENT LOW_STOCK <name>=<left> …` | Pushed to the provider when an accepted order drops a product below 5; if no provider is connected, it is replayed on `HELLO provider`. |

Other error replies: `ERR UNKNOWN_COMMAND <command>` (also for random bytes), `ERR PROVIDER_CAN_ONLY_DELIVER`, `ERR MESSAGE_TOO_LONG`. The server logs connections, orders, deliveries and errors, and keeps serving other clients after a bad or broken connection.
