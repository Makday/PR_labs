namespace CoffeeShop;

public class Stock
{
    private readonly Dictionary<string, int> _items = new();
    private readonly object _lock = new();

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

    public bool TryTake(IReadOnlyDictionary<string, int> wanted, out Dictionary<string, int> shortages)
    {
        lock (_lock)
        {
            shortages = new Dictionary<string, int>();

            foreach (var (name, qty) in wanted)
            {
                int have = _items.GetValueOrDefault(name);
                if (have < qty) shortages[name] = have;
            }

            if (shortages.Count > 0) return false;

            foreach (var (name, qty) in wanted)
                _items[name] -= qty;

            return true;
        }
    }
}