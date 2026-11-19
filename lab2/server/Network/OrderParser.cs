using System.Globalization;

namespace CoffeeShop;

public static class OrderParser
{
    // tokens: ["coffee=2", "tea=1"]. On failure, 'error' is the protocol text after "ERR ".
    public static bool TryParse(string[] tokens,
                                out Dictionary<string, int> items,
                                out string error)
    {
        items = new Dictionary<string, int>();
        error = "";

        if (tokens.Length == 0)
        {
            error = "BAD_FORMAT empty order";
            return false;
        }

        foreach (var token in tokens)
        {
            int eq = token.IndexOf('=');
            if (eq <= 0 || eq == token.Length - 1)
            {
                error = $"BAD_FORMAT {token}";
                return false;
            }

            string rawName = token[..eq];
            string rawQty  = token[(eq + 1)..];

            if (!Menu.TryGet(rawName, out var product))
            {
                error = $"UNKNOWN_PRODUCT {rawName}";
                return false;
            }

            if (!int.TryParse(rawQty, NumberStyles.Integer, CultureInfo.InvariantCulture, out int qty)
                || qty <= 0)
            {
                error = $"BAD_QUANTITY {rawName}={rawQty}";
                return false;
            }

            if (!items.TryAdd(product.Name, qty))   // canonical name as key
            {
                error = $"BAD_FORMAT duplicate {product.Name}";
                return false;
            }
        }

        return true;
    }
}