using System.Collections.Concurrent;

namespace CoffeeShop;

public class Kitchen
{
    private readonly BlockingCollection<Order> _queue = new();
    private readonly int _workerCount = Environment.ProcessorCount;

    public void Start()
    {
        Log.Info($"starting {_workerCount} workers");
        for (int i = 0; i < _workerCount; i++)
        {
            new Thread(Work) { IsBackground = true, Name = $"worker-{i}" }.Start();
        }
    }

    public void Enqueue(Order order) => _queue.Add(order);

    private void Work()
    {
        foreach (var order in _queue.GetConsumingEnumerable())
        {
            try
            {
                Thread.Sleep(order.PrepSeconds * 1000);
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