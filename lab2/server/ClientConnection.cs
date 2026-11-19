using System.Net.Sockets;
using System.Text;

namespace CoffeeShop;

public class ClientConnection
{
    private const int MaxLine = 4096;

    private readonly Socket _socket;
    private readonly object _sendLock = new();
    private int _closed;

    private readonly byte[] _buf = new byte[MaxLine];
    private int _start, _end;

    public string Endpoint { get; }

    public ClientConnection(Socket socket)
    {
        _socket = socket;
        _socket.NoDelay = true;
        Endpoint = socket.RemoteEndPoint?.ToString() ?? "unknown";
    }

    public string? ReadLine()
    {
        while (true)
        {
            int nl = Array.IndexOf(_buf, (byte)'\n', _start, _end - _start);
            if (nl >= 0)
            {
                string line = Encoding.UTF8.GetString(_buf, _start, nl - _start).TrimEnd('\r');
                _start = nl + 1;
                return line;
            }

            if (_start > 0)
            {
                Buffer.BlockCopy(_buf, _start, _buf, 0, _end - _start);
                _end -= _start;
                _start = 0;
            }


            if (_end == _buf.Length)
                throw new InvalidDataException($"line longer than {MaxLine} bytes");

            int n = _socket.Receive(_buf, _end, _buf.Length - _end, SocketFlags.None);
            if (n == 0) return null;
            _end += n;
        }
    }

    public void Send(string line)
    {
        byte[] data = Encoding.UTF8.GetBytes(line + "\n");
        lock (_sendLock)
        {
            int sent = 0;
            while (sent < data.Length)
                sent += _socket.Send(data, sent, data.Length - sent, SocketFlags.None);
        }
    }

    public void Close()
    {
        if (Interlocked.Exchange(ref _closed, 1) == 1) return;
        try { _socket.Shutdown(SocketShutdown.Both); } catch (SocketException) { }
        _socket.Close();
    }
}