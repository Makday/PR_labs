# Task 3 - provider identity and permission rules

**Requirement covered:** Task 3 (grade 7): only one provider connection, only it may deliver.

## Simulated
Provider 1 connects first, then a second provider tries to register. A normal client tries `deliver`, provider 1 tries `menu` and `order`, then sends a valid `deliver tea=10`.

## Why this proves it
The server recognises the provider by the connection that sent `HELLO provider` and stores it as the single provider. A client delivery gets `ERR NOT_PROVIDER`, a second provider gets `ERR PROVIDER_EXISTS`, and the provider's non-DELIVER commands get `ERR PROVIDER_CAN_ONLY_DELIVER`. Only the authorised delivery changes the stock.

## Screenshots

- `05a-client-deliver-denied.png` — client: delivery denied
- `05b-provider-menu-order-denied.png` — provider: restricted to DELIVER
- `05c-second-provider-denied.png` — second provider: rejected
- `05d-server-rules.png` — server: all role checks logged
