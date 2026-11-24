# Task 2 - more clients than workers + last-item race

**Requirement covered:** Task 2 (grade 6): many simultaneous clients, work queue, protected shared stock.

## Simulated
Nine interactive clients connect (the machine has 8 workers). Each sends `order coffee=1` (4 s) back-to-back, so 8 orders are prepared and the 9th waits. Afterwards one client reduces the croissant stock to exactly 1 and two clients send `order croissant=1` at the same moment from two tabs.

## Why this proves it
The screenshots show all 9 acceptances before the first ready events, the burst of 8 ready events at ~4 s and the 9th only at ~8 s: the oldest waiting order is taken by the first free worker. The last-croissant pair proves the shared stock is protected: exactly one client gets `OK ORDER`, the other gets `ERR NOT_ENOUGH croissant=0`; both could not get the same item.

## Screenshots

- `03a-nine-orders-server.png` — server: 9 accepted, 8 workers
- `03b-client9-accepted.png` — client 9: queued
- `03c-first-eight-ready-server.png` — server: first 8 ready
- `03d-client9-ready.png` — client 9: ready later
- `03e-last-order-ready-server.png` — server: queued order completed
- `03f-client2-last-croissant.png` — client 2: gets last croissant
- `03g-client3-last-croissant.png` — client 3: refused
- `03h-race-server.png` — server: accepted + refused logged
