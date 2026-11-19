using System.Net;
using System.Net.Sockets;

namespace CoffeeShop;

public class Server
{
    private readonly int _port;
    private readonly Stock _stock;
    private readonly Kitchen _kitchen;
    private int _nextOrderId;

    public Server(int port, Stock stock, Kitchen kitchen)
    {
        _port = port;
        _stock = stock;
        _kitchen = kitchen;
    }

    public void Run()
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
                Socket socket = listener.Accept();
                var conn = new ClientConnection(socket);
                Log.Info(conn.Endpoint, "connected");
                new Thread(() => HandleClient(conn)) { IsBackground = true }.Start();
            }
            catch (Exception ex)
            {
                Log.Error($"accept failed ({ex.GetType().Name}: {ex.Message})");
                Thread.Sleep(100);
            }
        }
    }

    private void HandleClient(ClientConnection conn)
    {
        try
        {
            string? line;
            while ((line = conn.ReadLine()) != null)
                Dispatch(conn, line);

            Log.Info(conn.Endpoint, "disconnected");
        }
        catch (Exception ex)
        {
            Log.Error(conn.Endpoint, $"{ex.GetType().Name}: {ex.Message}");
        }
        finally
        {
            conn.Close();
        }
    }

    private void Dispatch(ClientConnection conn, string line)
    {
        var parts = line.Split(' ', StringSplitOptions.RemoveEmptyEntries);
        if (parts.Length == 0) return;

        switch (parts[0].ToUpperInvariant())
        {
            case "HELLO":
                conn.Send("OK HELLO");
                break;

            case "MENU":
                conn.Send("OK MENU " + Format(_stock.Snapshot()));
                break;

            case "ORDER":
                HandleOrder(conn, parts[1..]);
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
            Reject(conn, error);
            return;
        }

        if (!_stock.TryTake(items, out var shortages))
        {
            Reject(conn, "NOT_ENOUGH " + Format(shortages));
            return;
        }

        var order = new Order(Interlocked.Increment(ref _nextOrderId), items, conn);
        Log.Info(conn.Endpoint, $"order {order.Id} accepted: {Format(items)} ({order.PrepSeconds}s)");
        conn.Send($"OK ORDER {order.Id} {order.PrepSeconds}");
        _kitchen.Enqueue(order);
    }

    private static void Reject(ClientConnection conn, string error)
    {
        Log.Info(conn.Endpoint, $"rejected: {error}");
        conn.Send("ERR " + error);
    }

    private static string Format(IEnumerable<KeyValuePair<string, int>> d)
        => string.Join(" ", d.Select(kv => $"{kv.Key}={kv.Value}"));
}