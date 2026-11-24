using System.Net;
using System.Net.Sockets;

namespace CoffeeShop;

public class Server
{
    private readonly int _port;
    private readonly Stock _stock;
    private readonly Kitchen _kitchen;
    private int _nextOrderId;
    private ClientConnection? _provider;                    // the one provider connection, or null
    private readonly object _providerLock = new();

    public Server(int port, Stock stock, Kitchen kitchen)
    {
        _port = port;
        _stock = stock;
        _kitchen = kitchen;
    }

    public async Task RunAsync() 
    {
        var listener = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
        listener.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.ReuseAddress, true);
        listener.Bind(new IPEndPoint(IPAddress.Any, _port));
        listener.Listen(100);
        Log.Info($"listening on port {_port}");

        while (true)
        {
            try
            {
                Socket socket = await listener.AcceptAsync();
                var conn = new ClientConnection(socket);
                Log.Info(conn.Endpoint, "connected");
                _ = HandleClientAsync(conn);  
            }
            catch (Exception ex)
            {
                Log.Error($"accept failed ({ex.GetType().Name}: {ex.Message})");
                await Task.Delay(100);  
            }
        }
    }

    private async Task HandleClientAsync(ClientConnection conn)
    {
        try
        {
            while (true)
            {
                string? line;
                try { line = await conn.ReadLineAsync(); }
                catch (LineTooLongException) { Reject(conn, "MESSAGE_TOO_LONG"); continue; }
                if (line == null) break;
                Dispatch(conn, line);
            }

            Log.Info(conn.Endpoint, "disconnected");
        }
        catch (Exception ex)
        {
            Log.Error(conn.Endpoint, $"{ex.GetType().Name}: {ex.Message}");
        }
        finally
        {
            _kitchen.CancelAll(conn);                        // its orders are cancelled, products go back
            lock (_providerLock)
                if (_provider == conn) _provider = null;     // the shop goes on without a provider
            conn.Close();
        }
    }

    private void Dispatch(ClientConnection conn, string line)
    {
        var parts = line.Split(' ', StringSplitOptions.RemoveEmptyEntries);
        if (parts.Length == 0) return;

        bool isProvider;
        lock (_providerLock) isProvider = _provider == conn;
        if (isProvider && !parts[0].Equals("DELIVER", StringComparison.OrdinalIgnoreCase))
        {
            Reject(conn, "PROVIDER_CAN_ONLY_DELIVER");
            return;
        }

        switch (parts[0].ToUpperInvariant())
        {
            case "HELLO":
                HandleHello(conn, parts);
                break;

            case "CANCEL":
                _kitchen.CancelAll(conn);
                break;

            case "MENU":
                conn.Send("OK MENU " + Format(_stock.Snapshot()));
                break;

            case "ORDER":
                HandleOrder(conn, parts[1..]);
                break;

            case "DELIVER":
                HandleDeliver(conn, parts[1..]);
                break;

            default:
                Reject(conn, $"UNKNOWN_COMMAND {parts[0]}");
                break;
        }
    }

    private void HandleOrder(ClientConnection conn, string[] tokens)
    {
        if (!OrderParser.TryParse(tokens, out var items, out var error))
        {
            Log.Info(conn.Endpoint, $"order refused: {error}");
            Reject(conn, error);
            return;
        }

        if (!_stock.TryTake(items, out var shortages, out var low))
        {
            Log.Info(conn.Endpoint, $"order refused: not enough {Format(shortages)}");
            Reject(conn, "NOT_ENOUGH " + Format(shortages));
            return;
        }

        var order = new Order(Interlocked.Increment(ref _nextOrderId), items, conn);
        Log.Info(conn.Endpoint, $"order {order.Id} accepted: {Format(items)} ({order.PrepSeconds}s)");
        _kitchen.Track(order);                               // from now on a failure cancels it and returns the products
        conn.Send($"OK ORDER {order.Id} {order.PrepSeconds}");
        _kitchen.Enqueue(order);

        if (low.Count > 0) AlertProvider(low);
    }

    private void HandleHello(ClientConnection conn, string[] parts)
    {
        string role = parts.Length > 1 ? parts[1].ToLowerInvariant() : "";

        if (role == "client")
        {
            conn.Send("OK HELLO");
        }
        else if (role == "provider")
        {
            bool accepted;
            lock (_providerLock)
            {
                accepted = _provider == null || _provider == conn;
                if (accepted) _provider = conn;
            }
            if (!accepted) { Reject(conn, "PROVIDER_EXISTS"); return; }

            Log.Info(conn.Endpoint, "registered as provider");
            conn.Send("OK HELLO");

            // alerts that came while no provider was connected
            var low = _stock.Snapshot().Where(kv => kv.Value < Stock.LowLevel)
                                       .ToDictionary(kv => kv.Key, kv => kv.Value);
            if (low.Count > 0) AlertProvider(low);
        }
        else
        {
            Reject(conn, $"BAD_ROLE {role}");
        }
    }

    private void HandleDeliver(ClientConnection conn, string[] tokens)
    {
        bool isProvider;
        lock (_providerLock) isProvider = _provider == conn;
        if (!isProvider) { Reject(conn, "NOT_PROVIDER"); return; }

        if (!OrderParser.TryParse(tokens, out var items, out var error))
        {
            Reject(conn, error);
            return;
        }

        _stock.Add(items);
        Log.Info(conn.Endpoint, $"delivery: {Format(items)}");
        conn.Send("OK DELIVER");
    }

    // Never throws: a broken provider must not break the client whose order triggered the alert.
    private void AlertProvider(IReadOnlyDictionary<string, int> low)
    {
        ClientConnection? provider;
        lock (_providerLock) provider = _provider;

        if (provider == null)
        {
            Log.Info($"low stock, no provider connected: {Format(low)}");
            return;
        }

        try
        {
            provider.Send("EVENT LOW_STOCK " + Format(low));
            Log.Info(provider.Endpoint, $"low stock alert sent: {Format(low)}");
        }
        catch (Exception ex)
        {
            Log.Error(provider.Endpoint, $"could not alert provider ({ex.GetType().Name}: {ex.Message})");
        }
    }

    private static void Reject(ClientConnection conn, string error)
    {
        // the text may echo garbage from the client: no control characters, bounded length
        error = new string(error.Select(c => char.IsControl(c) ? '?' : c).ToArray());
        if (error.Length > 200) error = error[..200];
        Log.Info(conn.Endpoint, $"rejected: {error}");
        conn.Send("ERR " + error);
    }

    private static string Format(IEnumerable<KeyValuePair<string, int>> d)
        => string.Join(" ", d.Select(kv => $"{kv.Key}={kv.Value}"));
}