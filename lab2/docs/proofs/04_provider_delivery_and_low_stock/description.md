# Task 3 - provider delivery and pushed low-stock alert

**Requirement covered:** Task 3 (grade 7): second role, unsolicited server messages, provider only deliveries.

## Simulated
A provider registers with `HELLO provider`. A client asks for MENU, then orders croissant=(stock-4), leaving exactly 4 in stock. The server immediately pushes `EVENT LOW_STOCK croissant=4` to the provider. The provider delivers croissant=20 and a client MENU confirms the stock grew. The provider then disconnects and the client places another successful order.

## Why this proves it
The provider receives a message it never requested, proving the server can initiate messages on a connection. Only the registered provider sends DELIVER, and the stock change is visible to clients. The last two screenshots show the shop continues with its current stock when the provider disappears.

## Screenshots

- `04a-provider-low-alert.png` — provider: pushed low-stock alert
- `04b-server-low-alert.png` — server: alert sent
- `04c-client-order-trigger.png` — client: order that crossed the threshold
- `04d-provider-delivery.png` — provider: DELIVER accepted
- `04e-client-stock-after-delivery.png` — client: stock after delivery
- `04f-server-delivery.png` — server: delivery logged
- `04g-provider-gone-server.png` — server: provider gone
- `04h-client-order-without-provider.png` — client: shop still works
