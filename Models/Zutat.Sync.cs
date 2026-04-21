using System;
using System.ComponentModel;

namespace RezepturMeister.Models;

public partial class Zutat : INotifyPropertyChanged
{
    public event PropertyChangedEventHandler? PropertyChanged;

    // PropertyChanged-Trigger für RohstoffId
    // Die eigentliche Property ist in Rezeptur.cs
    public void OnRohstoffIdChanged()
    {
        PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(nameof(RohstoffId)));
    }
}
