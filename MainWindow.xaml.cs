using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace RezepturMeister;

/// <summary>
/// Interaction logic for MainWindow.xaml
/// </summary>
public partial class MainWindow : Window
{
    public MainWindow()
    {
        try
        {
            InitializeComponent();
            // DataContext explizit im CodeBehind setzen
            this.DataContext = new ViewModels.MainViewModel();
        }
        catch (Exception ex)
        {
            MessageBox.Show($"Fehler beim Initialisieren des Fensters: {ex.Message}\n\nStackTrace: {ex.StackTrace}", "Fehler", MessageBoxButton.OK, MessageBoxImage.Error);
            throw;
        }
    }

    private void HelpButton_Click(object sender, RoutedEventArgs e)
    {
        var help = new Views.HelpWindow { Owner = this };
        help.Show();
    }
}