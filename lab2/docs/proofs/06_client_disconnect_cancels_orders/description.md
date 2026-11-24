# Task 4 - client disappears, orders cancelled and stock returned

**Requirement covered:** Task 4 (grade 8): vanished client must not harm the shop; queued and preparing orders are cancelled.

## Simulated
Client A places `order coffee=10` (40 s, being prepared) and `order tea=10` (30 s, queued). Its JVM is killed with SIGKILL. Client B then checks MENU and later quits gracefully while its own croissant order is still being prepared.

## Why this proves it
The server notices the closed socket, cancels every order owned by that connection: the worker's wait is interrupted (`preparation stopped`) and the queued order is dropped. The log lines 'products returned to stock' and client B's identical MENU before/after prove the products went back. The graceful `quit` shows the same cleanup path, and the server remains alive.

## Screenshots

- `06a-client-two-orders.png` — client A: two pending orders
- `06b-server-two-orders.png` — server: accepted
- `06c-server-cancellation.png` — server: cancelled + stock returned
- `06d-client-b-stock-restored.png` — client B: stock restored
- `06e-client-b-graceful-quit.png` — client B: quit
- `06f-server-after-both.png` — server: alive after disconnects
