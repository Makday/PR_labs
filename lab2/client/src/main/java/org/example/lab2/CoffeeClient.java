package org.example.lab2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;

public class CoffeeClient {

    private static final int MAX_LINE = 4096;
    private static final String PROMPT = "> ";

    private static Socket socket;
    private static OutputStream out;
    private static final Object sendLock = new Object();
    private static final Object printLock = new Object();
    private static volatile boolean closing = false;

    public static void main(String[] args) {
        if (args.length < 2 || args.length > 3) {
            usage();
            return;
        }
        String host = args[0];
        int port;
        try {
            port = Integer.parseInt(args[1]);
            if (port < 1 || port > 65535) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            usage();
            return;
        }
        boolean provider = false;
        if (args.length == 3) {
            if (args[2].equalsIgnoreCase("provider")) provider = true;
            else if (!args[2].equalsIgnoreCase("client")) {
                usage();
                return;
            }
        }

        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 5000);
            out = socket.getOutputStream();
        } catch (IOException e) {
            System.err.println("cannot connect to " + host + ":" + port + " (" + e.getMessage() + ")");
            return;
        }

        println("connected to " + host + ":" + port + " as " + (provider ? "provider" : "client")
                + ". Type 'help' for commands.");

        // Receiver thread: prints every server line the moment it arrives,
        // independently of what the user is typing.
        Thread receiver = new Thread(CoffeeClient::receiveLoop, "receiver");
        receiver.setDaemon(true);
        receiver.start();

        try {
            send(provider ? "HELLO PROVIDER" : "HELLO");
        } catch (IOException e) {
            serverGone("send failed: " + e.getMessage());
            return;
        }

        keyboardLoop(provider);
    }

    // ---------------------------------------------------------------- keyboard

    private static void keyboardLoop(boolean provider) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        try {
            while (true) {
                prompt();
                String line = in.readLine();
                if (line == null) break; // EOF on stdin
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+", 2);
                String cmd = parts[0].toLowerCase();
                String rest = parts.length > 1 ? parts[1] : "";

                switch (cmd) {
                    case "quit":
                    case "exit":
                        shutdown();
                        return;
                    case "help":
                        printHelp(provider);
                        break;
                    case "menu":
                        send("MENU");
                        break;
                    case "order":
                        send("ORDER " + rest);
                        break;
                    case "deliver":
                        send("DELIVER " + rest);
                        break;
                    default:
                        // Sent as typed so invalid input can be tested against the server.
                        send(line);
                }
            }
            shutdown();
        } catch (IOException e) {
            serverGone("send failed: " + e.getMessage());
        }
    }

    private static void printHelp(boolean provider) {
        println("  menu                          show products and stock");
        println("  order coffee=2 tea=1          order products");
        if (provider) println("  deliver coffee=20 croissant=10   bring new products (provider only)");
        println("  quit                          disconnect");
        println("  anything else is sent as is (useful for testing errors)");
    }

    // ---------------------------------------------------------------- receiving

    private static void receiveLoop() {
        try {
            LineReader reader = new LineReader(socket.getInputStream());
            String line;
            while ((line = reader.readLine()) != null) {
                printAsync(line);
            }
            if (!closing) serverGone("server closed the connection");
        } catch (IOException e) {
            if (!closing) serverGone("connection lost (" + e.getMessage() + ")");
        }
    }

    /** Print a server message without wrecking the line the user is typing. */
    private static void printAsync(String line) {
        synchronized (printLock) {
            System.out.print("\r" + line + "\n" + PROMPT);
            System.out.flush();
        }
    }

    // ---------------------------------------------------------------- sending

    private static void send(String line) throws IOException {
        byte[] data = (line + "\n").getBytes(StandardCharsets.UTF_8);
        if (data.length > MAX_LINE) {
            println("message too long (max " + MAX_LINE + " bytes), not sent");
            return;
        }
        synchronized (sendLock) {
            out.write(data);
            out.flush();
        }
    }

    // ---------------------------------------------------------------- shutdown / output helpers

    private static void serverGone(String reason) {
        if (closing) return;
        closing = true;
        println(reason + ". Bye.");
        closeQuietly();
        System.exit(0);
    }

    private static void shutdown() {
        closing = true;
        closeQuietly();
    }

    private static void closeQuietly() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }

    private static void prompt() {
        synchronized (printLock) {
            System.out.print(PROMPT);
            System.out.flush();
        }
    }

    private static void println(String s) {
        synchronized (printLock) {
            System.out.print("\r" + s + "\n");
            System.out.flush();
        }
    }

    private static void usage() {
        System.err.println("usage: java CoffeeClient <host> <port> [client|provider]");
    }

    // ---------------------------------------------------------------- framing

    /**
     * Cuts the TCP byte stream into '\n'-terminated lines. A message split over
     * several receives stays in 'pending' until its '\n' arrives; several messages
     * in one receive are queued in 'ready' and returned one by one.
     */
    private static final class LineReader {
        private final InputStream in;
        private final byte[] chunk = new byte[1024];
        private byte[] pending = new byte[256];
        private int pendingLen = 0;
        private final ArrayDeque<String> ready = new ArrayDeque<>();

        LineReader(InputStream in) {
            this.in = in;
        }

        /** Returns the next line, or null when the server closed the connection (read returned -1). */
        String readLine() throws IOException {
            while (ready.isEmpty()) {
                int n = in.read(chunk);
                if (n < 0) return null;
                for (int i = 0; i < n; i++) {
                    byte b = chunk[i];
                    if (b == '\n') {
                        String s = new String(pending, 0, pendingLen, StandardCharsets.UTF_8);
                        pendingLen = 0;
                        if (s.endsWith("\r")) s = s.substring(0, s.length() - 1);
                        ready.add(s);
                    } else {
                        if (pendingLen >= MAX_LINE) throw new IOException("line from server too long");
                        if (pendingLen == pending.length) pending = java.util.Arrays.copyOf(pending, pending.length * 2);
                        pending[pendingLen++] = b;
                    }
                }
            }
            return ready.poll();
        }
    }
}
