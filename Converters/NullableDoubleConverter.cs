using System.Globalization;
using System.Windows.Data;

namespace RezepturMeister.Converters;

public class NullableDoubleConverter : IValueConverter
{
    private static readonly CultureInfo GermanCulture = new CultureInfo("de-DE");

    public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
    {
        if (value is double d) return d.ToString(GermanCulture);
        return string.Empty;
    }

    public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
    {
        if (value is string s)
        {
            if (string.IsNullOrWhiteSpace(s)) return null!;
            if (double.TryParse(s, NumberStyles.Any, GermanCulture, out double result))
                return result;
        }
        return Binding.DoNothing;
    }
}
