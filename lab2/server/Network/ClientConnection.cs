using System.Net.Sockets;
using System.Text;

public class LineTooLongException : Exception { }

public class ClientConnection
{
    private const int MaxLine = 4096;

    private readonly Socket _socket;
    private readonly object _sendLock = new();
    private readonly byte[] _chunk = new byte[1024];
    private readonly List<byte> _pending = new();
    private bool _discarding;                 // skipping the rest of a too long line

    public string Endpoint { get; }

    public ClientConnection(Socket socket)
    {
        _socket = socket;
        Endpoint = socket.RemoteEndPoint?.ToString() ?? "unknown";
        try
        {
            _socket.SendTimeout = 5000;       // a stuck client must not block a worker forever
            // detect a vanished peer (network gone) in about 45 s
            _socket.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.KeepAlive, true);
            _socket.SetSocketOption(SocketOptionLevel.Tcp, SocketOptionName.TcpKeepAliveTime, 30);
            _socket.SetSocketOption(SocketOptionLevel.Tcp, SocketOptionName.TcpKeepAliveInterval, 5);
            _socket.SetSocketOption(SocketOptionLevel.Tcp, SocketOptionName.TcpKeepAliveRetryCount, 3);
        }
        catch (Exception) { /* option not supported on this platform: go on without it */ }
    }

    public async Task<string?> ReadLineAsync() 
    {
        while (true)
        {
            int nl = _pending.IndexOf((byte)'\n');
            if (nl >= 0)
            {
                string line = Encoding.UTF8.GetString(_pending.GetRange(0, nl).ToArray());
                _pending.RemoveRange(0, nl + 1);
                if (nl > MaxLine) throw new LineTooLongException();
                return line.TrimEnd('\r');
            }
            if (_pending.Count > MaxLine)
            {
                _pending.Clear();
                _discarding = true;               // drop bytes until the end of this line
                throw new LineTooLongException();
            }

            int n = await _socket.ReceiveAsync(_chunk.AsMemory(), SocketFlags.None);
            if (n == 0) return null;
            if (_discarding)
            {
                int end = Array.IndexOf(_chunk, (byte)'\n', 0, n);
                if (end < 0) continue;
                _discarding = false;
                _pending.AddRange(_chunk.AsSpan(end + 1, n - end - 1));
                continue;
            }
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