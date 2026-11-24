# Task 1/4 - invalid orders get precise errors

**Requirement covered:** Task 1: unknown product / non-positive quantity errors. Task 4: every invalid input gets an error and the server survives.

## Simulated
One client sends an unknown command, an empty order, an unknown product, a zero quantity, a duplicated product and an impossible quantity. All six are sent over a single live connection, followed by a valid MENU.

## Why this proves it
Each malformed request produces a specific ERR text (`UNKNOWN_COMMAND`, `BAD_FORMAT`, `UNKNOWN_PRODUCT`, `BAD_QUANTITY`, `NOT_ENOUGH ... <remaining stock>`) instead of killing or blocking the connection. The final successful MENU proves the server and the connection are still healthy.

## Screenshots

- `02a-client-errors.png` — client: six ERR replies
- `02b-server-errors.png` — server: rejections logged
- `02c-client-still-works.png` — client: still usable afterwards
