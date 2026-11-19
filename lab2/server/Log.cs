namespace CoffeeShop;

public static class Log
{
    public static void Info(string endpoint, string message)  => Write("INFO ", endpoint, message);
    public static void Error(string endpoint, string message) => Write("ERROR", endpoint, message);

    public static void Info(string message)  => Write("INFO ", "-", message);
    public static void Error(string message) => Write("ERROR", "-", message);

    private static void Write(string level, string endpoint, string message)
    {
        string line = $"{DateTime.Now:yyyy-MM-dd HH:mm:ss.fff} {level} [{endpoint}] {message}";
        Console.WriteLine(line);
    }

}