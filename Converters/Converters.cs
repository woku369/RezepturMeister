using System.Globalization;
using System.Windows;
using System.Windows.Data;

namespace RezepturMeister.Converters;

public class NullToVisibilityConverter : IValueConverter
{
    public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
    {
        return value == null ? Visibility.Collapsed : Visibility.Visible;
    }

    public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
    {
        throw new NotImplementedException();
    }
}

public class PercentageConverter : IValueConverter
{
    public object Convert(object value, Type targetType, object parameter, CultureInfo culture)
    {
        if (value is double menge && parameter is double gesamt && gesamt > 0)
        {
            return (menge / gesamt * 100).ToString("F1", CultureInfo.GetCultureInfo("de-DE"));
        }
        return "0.0";
    }

    public object ConvertBack(object value, Type targetType, object parameter, CultureInfo culture)
    {
        throw new NotImplementedException();
    }
}