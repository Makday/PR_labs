# Task 1 - menu, stock, one order and delayed ready event

**Requirement covered:** Task 1 (grade 5): MENU, stock, one order at a time, immediate acknowledgement + later READY.

## Simulated
A single client connects, asks for MENU, then orders coffee=1 tea=1 croissant=2. The expected preparation time is 4 + 3 + 2*2 = 11 seconds. The client keeps working after the acknowledgement and receives `EVENT READY 1` when the worker finishes.

## Why this proves it
It shows that the stock is visible, that the server acknowledges the order immediately with its id and preparation time, and that readiness is pushed by the server later instead of blocking the answer. The server log has a line for the connection, the accepted order, and the ready/delivered events.

## Screenshots

- `01a-client-menu.png` — client: MENU
- `01b-server-menu.png` — server: connection + MENU
- `01c-client-order-accepted.png` — client: instant OK ORDER
- `01d-server-order-accepted.png` — server: accepted order
- `01e-client-ready.png` — client: delayed READY event
- `01f-server-ready.png` — server: ready logged after 11 s
