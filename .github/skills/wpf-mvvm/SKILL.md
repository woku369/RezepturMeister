---
name: wpf-mvvm
description: 'WPF und MVVM Pattern für RezepturMeister. Verwende diesen Skill bei: DataBinding, Commands (RelayCommand), ObservableCollection, ObservableObject, INotifyPropertyChanged, ViewModel erstellen oder korrigieren, XAML-Bindings verdrahten, UserControls, DataGrid-Bindings, Converter, CommunityToolkit.Mvvm. Schlüsselwörter: WPF, MVVM, Binding, Command, ViewModel, DataContext, IValueConverter, DataGrid, TabControl.'
argument-hint: 'Beschreibe die WPF/MVVM-Aufgabe (z.B. "ViewModel für X erstellen", "DataGrid binden")'
---

# WPF MVVM Skill – RezepturMeister

## Projektkontext

- Framework: WPF (.NET 8), CommunityToolkit.Mvvm 8.2.2
- Pattern: MVVM mit `ObservableObject` als Basisklasse
- ViewModels: `ViewModels/`, Views: `Views/`, Converters: `Converters/`
- DataContext wird in XAML direkt gesetzt: `<vm:XyzViewModel />`

## Regeln & Best Practices

### ViewModels

- Erben immer von `ObservableObject` (CommunityToolkit)
- Properties mit `[ObservableProperty]` auf `partial`-Klassen → erzeugt `PropertyName` + `SetPropertyName()`
- Commands mit `[RelayCommand]` auf Methoden → erzeugt `PropertyNameCommand`
- Klassen mit Source Generators müssen `partial` sein

```csharp
public partial class MeinViewModel : ObservableObject
{
    [ObservableProperty]
    private string name = string.Empty;

    [RelayCommand]
    private void Speichern() { ... }
}
```

### ObservableCollections

- Immer `ObservableCollection<T>` für Listen in ViewModels
- Nie die Collection ersetzen — stattdessen `Clear()` + neu befüllen
- Korrekt:
```csharp
public ObservableCollection<Rohstoff> Rohstoffe { get; } = new();

private void LoadRohstoffe()
{
    Rohstoffe.Clear();
    foreach (var r in _service.GetAll())
        Rohstoffe.Add(r);
}
```

### DataBinding in XAML

```xml
<!-- Liste binden -->
<DataGrid ItemsSource="{Binding Rohstoffe}" SelectedItem="{Binding SelectedRohstoff}">
    <DataGrid.Columns>
        <DataGridTextColumn Header="Name" Binding="{Binding Name}" Width="*"/>
        <DataGridTextColumn Header="Dichte" Binding="{Binding Dichte, StringFormat=F3}" Width="100"/>
    </DataGrid.Columns>
</DataGrid>

<!-- Command binden -->
<Button Content="Hinzufügen" Command="{Binding AddRohstoffCommand}"/>

<!-- ComboBox in DataGrid (RelativeSource für DataContext) -->
<ComboBox ItemsSource="{Binding DataContext.VerfuegbareRohstoffe,
          RelativeSource={RelativeSource AncestorType=DataGrid}}"
          SelectedValue="{Binding RohstoffId}"
          DisplayMemberPath="Name"
          SelectedValuePath="Id"/>
```

### Converters

Alle Converter liegen in `Converters/`. Registrierung in XAML:
```xml
<UserControl.Resources>
    <conv:NullToVisibilityConverter x:Key="NullToVisibilityConverter"/>
    <conv:DecimalConverter x:Key="DecimalConverter"/>
</UserControl.Resources>
```

Verwendung:
```xml
<StackPanel Visibility="{Binding CurrentRezeptur, Converter={StaticResource NullToVisibilityConverter}}"/>
```

### UserControl DataContext

```xml
<UserControl.DataContext>
    <vm:RohstoffViewModel />
</UserControl.DataContext>
```

### TabControl in MainWindow

```xml
<TabControl>
    <TabItem Header="Rohstoffe"><views:RohstoffView /></TabItem>
    <TabItem Header="Rezepturen"><views:RezepturView /></TabItem>
</TabControl>
```

## Häufige Fehler

| Fehler | Ursache | Lösung |
|--------|---------|--------|
| Binding funktioniert nicht | DataContext nicht gesetzt | DataContext in XAML oder Code-Behind setzen |
| Command wird nicht gefunden | Klasse nicht `partial` | `partial class` verwenden |
| ObservableCollection aktualisiert nicht | Collection ersetzt statt befüllt | `Clear()` + `Add()` |
| RelativeSource funktioniert nicht | Falscher AncestorType | AncestorType auf umgebendes Element prüfen |

## Checkliste für neue ViewModels

1. `partial class` + erbt von `ObservableObject`
2. Properties mit `[ObservableProperty]` (lowercase Feld)
3. Commands mit `[RelayCommand]`
4. Collections als `ObservableCollection<T>` mit `get; } = new()`
5. Load-Methode ruft `Clear()` dann `Add()` auf
6. DataContext in XAML oder MainWindow drahtverbunden
