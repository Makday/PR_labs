namespace CoffeeShop;

public class Order
{
    public int Id { get; }
    public IReadOnlyDictionary<string, int> Items { get; }
    public int PrepSeconds { get; }
    public ClientConnection Client { get; }

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