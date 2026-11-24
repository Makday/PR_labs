using CoffeeShop;

if (args.Length != 1 || !int.TryParse(args[0], out int port) || port is < 1 or > 65535)
{
    Console.Error.WriteLine("usage: CoffeeShop <port>");
    return 1;
}

var stock = new Stock();
Log.Info($"initial stock: {string.Join(" ", stock.Snapshot().Select(kv => $"{kv.Key}={kv.Value}"))}");

var kitchen = new Kitchen(stock);
kitchen.Start();

await new Server(port, stock, kitchen).RunAsync();
return 0;