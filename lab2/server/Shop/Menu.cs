namespace CoffeeShop;

public static class Menu
{
    private static readonly Dictionary<string, Product> _products =
        new(StringComparer.OrdinalIgnoreCase)
        {
            ["coffee"]    = new Product("coffee", 4),
            ["tea"]       = new Product("tea", 3),
            ["croissant"] = new Product("croissant", 2),
        };

    public static IEnumerable<Product> All => _products.Values;

    public static bool TryGet(string name, out Product product)
        => _products.TryGetValue(name, out product!);

    public static Product Get(string name) => _products[name];
}