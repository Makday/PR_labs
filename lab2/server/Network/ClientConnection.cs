using System.Net.Sockets;
using System.Text;

public class ClientConnection
{
    private const int MaxLine = 4096;

    private readonly Socket _socket;
    private readonly object _sendLock = new();
    private readonly byte[] _chunk = new byte[1024];
    private readonly List<byte> _pending = new();

    public string Endpoint { get; }

    public ClientConnection(Socket socket)
    {
        _socket = socket;
        Endpoint = socket.RemoteEndPoint?.ToString() ?? "unknown";
    }

    public string? ReadLine()
    {
        while (true)
        {
            int nl = _pending.IndexOf((byte)'\n');
            if (nl >= 0)
            {
                string line = Encoding.UTF8.GetString(_pending.GetRange(0, nl).ToArray());
                _pending.RemoveRange(0, nl + 1);
                return line.TrimEnd('\r');
            }
            if (_pending.Count > MaxLine) throw new InvalidDataException("line too long");

            int n = _socket.Receive(_chunk);
            if (n == 0) return null;
            _pending.AddRange(_chunk.AsSpan(0, n));
        }
    }

    public void Send(string line)
    {
        byte[] data = Encoding.UTF8.GetBytes(line + "\n");
        lock (_sendLock) _socket.Send(data);
    }

    public void Close() => _socket.Close();
}