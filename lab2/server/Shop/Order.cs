namespace CoffeeShop;

public class Order
{
    public int Id { get; }
    public IReadOnlyDictionary<string, int> Items { get; }
    public int PrepSeconds { get; }
    public ClientConnection Client { get; }

    private int _state;                                   // 0 active, 1 ready, 2 cancelled (changes once, atomically)
    private readonly ManualResetEventSlim _cancelled = new(false);

    public bool IsCancelled => Volatile.Read(ref _state) == 2;
    public bool TryCancel()                               // true for the one caller that cancels it
    {
        if (Interlocked.CompareExchange(ref _state, 2, 0) != 0) return false;
        _cancelled.Set();                                 // wakes the worker if it is preparing this order
        return true;
    }
    public bool TryComplete() => Interlocked.CompareExchange(ref _state, 1, 0) == 0;
    public bool WaitCancelled(int ms) => _cancelled.Wait(ms);   // true = cancelled during the wait

    public Order(int id, Dictionary<string, int> items, ClientConnection client)
    {
        Id = id;
        Items = new Dictionary<string, int>(items);
        Client = client;

        int total = 0;
        foreach (var (name, qty) in Items)
            total += qty * Menu.Get(name).PrepSeconds;
        PrepSeconds = total;
    }
}