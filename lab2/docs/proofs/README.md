# Lab 2 - proof index

Each folder contains real screenshots of the Konsole/devbox terminals used to run
the C# server and the Java `CoffeeClient.jar`, plus a short `description.md`.

| Folder | Task | What is proven |
|---|---|---|
| `01_menu_and_order` | 1 | MENU, stock, one order, immediate OK ORDER and later EVENT READY |
| `02_invalid_orders` | 1, 4 | unknown command/product, bad quantity/format, duplicate, NOT_ENOUGH |
| `03_many_clients_queue_and_last_item` | 2 | 9 clients vs 8 workers, FIFO queue, atomic last-croissant race |
| `04_provider_delivery_and_low_stock` | 3 | provider role, pushed LOW_STOCK alert, DELIVER, no provider afterwards |
| `05_provider_only_rules` | 3 | NOT_PROVIDER, PROVIDER_EXISTS, PROVIDER_CAN_ONLY_DELIVER |
| `06_client_disconnect_cancels_orders` | 4 | SIGKILL/graceful quit cancel queued+preparing orders, stock returned |
| `07_invalid_bytes_and_long_message` | 4 | random bytes, >4096-byte line, server and other clients survive |
| `08_server_disconnect_clients_exit` | 4 | clients/provider see EOF immediately and exit cleanly |

Programs were started inside the devbox shell of `PR_labs`:
`dotnet server/bin/Debug/net10.0/server.dll <port>` and
`java -jar client/CoffeeClient.jar 127.0.0.1 <port> [client|provider]`.
