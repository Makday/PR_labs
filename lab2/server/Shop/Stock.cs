namespace CoffeeShop;

public class Stock
{
    private readonly Dictionary<string, int> _items = new();
    private readonly object _lock = new();

    public const int LowLevel = 5;      // a product below this level triggers an alert

    public Stock()
    {
        foreach (var p in Menu.All)
            _items[p.Name] = Random.Shared.Next(10, 51);
    }

    public Dictionary<string, int> Snapshot()
    {
        lock (_lock)
            return new Dictionary<string, int>(_items);
    }

    // low: products that this take pushed from LowLevel or more to below LowLevel (only filled on success)
    public bool TryTake(IReadOnlyDictionary<string, int> wanted, out Dictionary<string, int> shortages,
                        out Dictionary<string, int> low)
    {
        lock (_lock)
        {
            shortages = new Dictionary<string, int>();
            low = new Dictionary<string, int>();

            foreach (var (name, qty) in wanted)
            {
                int have = _items.GetValueOrDefault(name);
                if (have < qty) shortages[name] = have;
            }

            if (shortages.Count > 0) return false;

            foreach (var (name, qty) in wanted)
            {
                int before = _items[name];
                _items[name] = before - qty;
                if (before >= LowLevel && before - qty < LowLevel) low[name] = before - qty;
            }

            return true;
        }
    }

    public void Add(IReadOnlyDictionary<string, int> items)
    {
        lock (_lock)
            foreach (var (name, qty) in items)
                _items[name] += qty;
    }
}