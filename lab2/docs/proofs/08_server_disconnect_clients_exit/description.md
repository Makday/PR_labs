# Task 4 - server disappears, clients end cleanly

**Requirement covered:** Task 4 (grade 8): client/provider must show server messages immediately and end cleanly when the server disappears.

## Simulated
Two clients and one provider are connected. The server process is stopped with Ctrl+C. All three programs observe the closed TCP connection, print `server closed the connection. Bye.` and return to the shell. A new client then tries to connect to the dead port.

## Why this proves it
The client/provider receiver threads are separate from keyboard input, so the EOF is displayed immediately even while they wait for input. They exit instead of hanging on a dead socket, and a late client gets an explicit connection error.

## Screenshots

- `08a-clients-connected.png` — clients/provider connected
- `08b-server-stopped.png` — server stopped
- `08c-client-a-clean-exit.png` — client A clean exit
- `08d-client-b-clean-exit.png` — client B clean exit
- `08e-provider-clean-exit.png` — provider clean exit
- `08f-late-client-refused.png` — late client refused
