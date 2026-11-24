using System.Collections.Concurrent;

namespace CoffeeShop;

public class Kitchen
{
    private readonly BlockingCollection<Order> _queue = new();
    private readonly int _workerCount = Environment.ProcessorCount;
    private readonly Stock _stock;
    private readonly ConcurrentDictionary<int, Order> _active = new();   // queued or being prepared

    public Kitchen(Stock stock) => _stock = stock;

    public void Start()
    {
        Log.Info($"starting {_workerCount} workers");
        for (int i = 0; i < _workerCount; i++)
        {
            new Thread(Work) { IsBackground = true, Name = $"worker-{i}" }.Start();
        }
    }

    public void Track(Order order) => _active[order.Id] = order;
    public void Enqueue(Order order) => _queue.Add(order);

    // The client is gone: cancel its waiting and its being-prepared orders, products go back to the stock.
    public void CancelAll(ClientConnection client)
    {
        foreach (var order in _active.Values.Where(o => o.Client == client).ToList())
        {
            if (!order.TryCancel()) continue;             // already ready or already cancelled
            _active.TryRemove(order.Id, out _);
            _stock.Add(order.Items);
            Log.Info(client.Endpoint, $"order {order.Id} cancelled, products returned to stock: " +
                string.Join(" ", order.Items.Select(kv => $"{kv.Key}={kv.Value}")));
        }
    }

    private void Work()
    {
        foreach (var order in _queue.GetConsumingEnumerable())
        {
            try
            {
                if (order.IsCancelled) continue;              // cancelled while it waited in the queue
                if (order.WaitCancelled(order.PrepSeconds * 1000))   // the preparation (simulated by waiting)
                {
                    Log.Info(order.Client.Endpoint, $"order {order.Id} preparation stopped");
                    continue;
                }
                if (!order.TryComplete()) continue;           // cancelled at the last moment
                _active.TryRemove(order.Id, out _);
                Log.Info(order.Client.Endpoint, $"order {order.Id} ready");

                order.Client.Send($"EVENT READY {order.Id}");
                Log.Info(order.Client.Endpoint, $"order {order.Id} delivered");
            }
            catch (Exception ex)
            {
                Log.Error(order.Client.Endpoint,
                    $"order {order.Id}: could not deliver ({ex.GetType().Name}: {ex.Message})");
            }
        }
    }
}