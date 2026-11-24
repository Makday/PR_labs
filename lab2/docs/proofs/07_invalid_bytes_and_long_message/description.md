# Task 4 - random bytes, over-long messages, server survives

**Requirement covered:** Task 4 (grade 8): random bytes / wrong parameters / too-long message must get an error, never crash or block others.

## Simulated
A raw TCP probe sends control bytes with an unknown command, then one 5000-byte line, then a valid MENU request on the same connection. Finally a second, healthy Java client asks for MENU over its own connection.

## Why this proves it
The server sanitises and rejects the garbage (`UNKNOWN_COMMAND ...`), answers `ERR MESSAGE_TOO_LONG` and discards the rest of the oversized line, then serves the valid MENU on that same connection. A second, healthy client is never blocked and the server keeps logging normally.

## Screenshots

- `07a-raw-probe.png` — raw client: three probes
- `07b-server-invalid-bytes.png` — server: errors logged
- `07c-client-still-alive.png` — healthy client: MENU
- `07d-server-still-alive.png` — server: alive
