---
name: export-print
description: 'PDF- und XLSX-Export sowie Druckfunktion für RezepturMeister. Verwende diesen Skill bei: PDF erstellen mit PdfSharpCore, Excel/XLSX exportieren mit ClosedXML, Drucken aus WPF, Rezeptur als PDF speichern, Rohstoffliste exportieren, PrintDialog, FlowDocument drucken. Schlüsselwörter: PDF Export, XLSX Export, PdfSharpCore, ClosedXML, Drucken, PrintDialog, Rohstoffliste, Rezeptur drucken, Excel, DIN A4.'
argument-hint: 'Was soll exportiert/gedruckt werden? (z.B. "Rezeptur als PDF", "Rohstoffliste XLSX", "Rezeptur drucken")'
---

# Export & Print Skill – RezepturMeister

## Projektkontext

- PDF: `PdfSharpCore` 1.3.1 (bereits eingebunden)
- XLSX: `ClosedXML` 0.102.1 (bereits eingebunden)
- ViewModels: Export-Commands in `RezepturViewModel` und `RohstoffViewModel`
- Speicherpfad: via `SaveFileDialog` vom Benutzer wählen lassen

## PDF-Export mit PdfSharpCore

### Rohstoffliste als PDF

```csharp
using PdfSharpCore.Pdf;
using PdfSharpCore.Drawing;

public void ExportRohstoffePdf(IEnumerable<Rohstoff> rohstoffe, string filePath)
{
    var document = new PdfDocument();
    document.Info.Title = "Rohstoffliste";
    
    var page = document.AddPage();
    page.Size = PdfSharpCore.PageSize.A4;
    var gfx = XGraphics.FromPdfPage(page);
    
    var fontTitle = new XFont("Arial", 16, XFontStyle.Bold);
    var fontHeader = new XFont("Arial", 10, XFontStyle.Bold);
    var fontBody = new XFont("Arial", 9, XFontStyle.Regular);
    
    double y = 40;
    gfx.DrawString("Rohstoffliste", fontTitle, XBrushes.Black, new XPoint(40, y));
    y += 30;
    
    // Spaltenköpfe
    gfx.DrawString("Name", fontHeader, XBrushes.Black, new XPoint(40, y));
    gfx.DrawString("Kategorie", fontHeader, XBrushes.Black, new XPoint(200, y));
    gfx.DrawString("Dichte", fontHeader, XBrushes.Black, new XPoint(350, y));
    gfx.DrawString("Alkohol %", fontHeader, XBrushes.Black, new XPoint(430, y));
    y += 5;
    gfx.DrawLine(XPens.Black, 40, y, 555, y);
    y += 15;
    
    foreach (var r in rohstoffe)
    {
        if (y > page.Height - 50) // Neue Seite
        {
            page = document.AddPage();
            page.Size = PdfSharpCore.PageSize.A4;
            gfx = XGraphics.FromPdfPage(page);
            y = 40;
        }
        gfx.DrawString(r.Name, fontBody, XBrushes.Black, new XPoint(40, y));
        gfx.DrawString(r.Kategorie, fontBody, XBrushes.Black, new XPoint(200, y));
        gfx.DrawString(r.Dichte.ToString("F3"), fontBody, XBrushes.Black, new XPoint(350, y));
        gfx.DrawString(r.Alkoholgehalt.ToString("F1") + " %", fontBody, XBrushes.Black, new XPoint(430, y));
        y += 18;
    }
    
    document.Save(filePath);
}
```

### Rezeptur als PDF (DIN A4)

```csharp
public void ExportRezepturPdf(Rezeptur rezeptur, string filePath)
{
    var document = new PdfDocument();
    var page = document.AddPage();
    page.Size = PdfSharpCore.PageSize.A4;
    var gfx = XGraphics.FromPdfPage(page);
    
    var fontTitle = new XFont("Arial", 14, XFontStyle.Bold);
    var fontNormal = new XFont("Arial", 10, XFontStyle.Regular);
    var fontBold = new XFont("Arial", 10, XFontStyle.Bold);
    
    double y = 40;
    gfx.DrawString($"Rezeptur {rezeptur.Nummer}", fontTitle, XBrushes.Black, new XPoint(40, y));
    y += 25;
    gfx.DrawString($"Datum: {rezeptur.Erstellungsdatum:d}", fontNormal, XBrushes.Black, new XPoint(40, y));
    y += 15;
    gfx.DrawString($"Charge: {rezeptur.Chargennummer}", fontNormal, XBrushes.Black, new XPoint(40, y));
    y += 25;
    
    // Zutaten-Tabelle
    gfx.DrawString("Zutat", fontBold, XBrushes.Black, new XPoint(40, y));
    gfx.DrawString("Menge", fontBold, XBrushes.Black, new XPoint(300, y));
    gfx.DrawString("Einheit", fontBold, XBrushes.Black, new XPoint(380, y));
    gfx.DrawString("Anteil %", fontBold, XBrushes.Black, new XPoint(450, y));
    y += 5;
    gfx.DrawLine(XPens.Black, 40, y, 555, y);
    y += 15;
    
    double gesamt = rezeptur.Zutaten.Sum(z => z.Menge);
    foreach (var zutat in rezeptur.Zutaten)
    {
        string name = zutat.Rohstoff?.Name ?? zutat.ManuellerName;
        double anteil = gesamt > 0 ? zutat.Menge / gesamt * 100 : 0;
        gfx.DrawString(name, fontNormal, XBrushes.Black, new XPoint(40, y));
        gfx.DrawString(zutat.Menge.ToString("F2"), fontNormal, XBrushes.Black, new XPoint(300, y));
        gfx.DrawString(zutat.Einheit, fontNormal, XBrushes.Black, new XPoint(380, y));
        gfx.DrawString(anteil.ToString("F1") + " %", fontNormal, XBrushes.Black, new XPoint(450, y));
        y += 18;
    }
    
    y += 10;
    gfx.DrawLine(XPens.Black, 40, y, 555, y);
    y += 15;
    gfx.DrawString($"Gesamtmenge: {gesamt:F2} g", fontBold, XBrushes.Black, new XPoint(40, y));
    
    document.Save(filePath);
}
```

## XLSX-Export mit ClosedXML

### Rohstoffliste als Excel

```csharp
using ClosedXML.Excel;

public void ExportRohstoffeXlsx(IEnumerable<Rohstoff> rohstoffe, string filePath)
{
    using var wb = new XLWorkbook();
    var ws = wb.Worksheets.Add("Rohstoffe");
    
    // Kopfzeile
    ws.Cell(1, 1).Value = "Name";
    ws.Cell(1, 2).Value = "Kategorie";
    ws.Cell(1, 3).Value = "Dichte (g/ml)";
    ws.Cell(1, 4).Value = "Alkoholgehalt (%)";
    ws.Row(1).Style.Font.Bold = true;
    ws.Row(1).Style.Fill.BackgroundColor = XLColor.LightGray;
    
    int row = 2;
    foreach (var r in rohstoffe)
    {
        ws.Cell(row, 1).Value = r.Name;
        ws.Cell(row, 2).Value = r.Kategorie;
        ws.Cell(row, 3).Value = r.Dichte;
        ws.Cell(row, 4).Value = r.Alkoholgehalt;
        row++;
    }
    
    ws.Columns().AdjustToContents();
    wb.SaveAs(filePath);
}
```

### Rezeptur als Excel

```csharp
public void ExportRezepturXlsx(Rezeptur rezeptur, string filePath)
{
    using var wb = new XLWorkbook();
    var ws = wb.Worksheets.Add($"Rezeptur {rezeptur.Nummer}");
    
    ws.Cell(1, 1).Value = $"Rezeptur {rezeptur.Nummer}";
    ws.Cell(1, 1).Style.Font.Bold = true;
    ws.Cell(1, 1).Style.Font.FontSize = 14;
    ws.Cell(2, 1).Value = $"Datum: {rezeptur.Erstellungsdatum:d}";
    ws.Cell(3, 1).Value = $"Charge: {rezeptur.Chargennummer}";
    
    ws.Cell(5, 1).Value = "Zutat";
    ws.Cell(5, 2).Value = "Menge";
    ws.Cell(5, 3).Value = "Einheit";
    ws.Cell(5, 4).Value = "Anteil %";
    ws.Row(5).Style.Font.Bold = true;
    
    double gesamt = rezeptur.Zutaten.Sum(z => z.Menge);
    int row = 6;
    foreach (var zutat in rezeptur.Zutaten)
    {
        ws.Cell(row, 1).Value = zutat.Rohstoff?.Name ?? zutat.ManuellerName;
        ws.Cell(row, 2).Value = zutat.Menge;
        ws.Cell(row, 3).Value = zutat.Einheit;
        ws.Cell(row, 4).Value = gesamt > 0 ? Math.Round(zutat.Menge / gesamt * 100, 1) : 0;
        row++;
    }
    
    ws.Cell(row + 1, 1).Value = "Gesamtmenge:";
    ws.Cell(row + 1, 2).Value = gesamt;
    ws.Columns().AdjustToContents();
    wb.SaveAs(filePath);
}
```

## SaveFileDialog (WPF)

```csharp
using Microsoft.Win32;

[RelayCommand]
private void ExportPdf()
{
    if (SelectedRezeptur == null) return;
    
    var dialog = new SaveFileDialog
    {
        Filter = "PDF-Datei (*.pdf)|*.pdf",
        FileName = $"Rezeptur_{SelectedRezeptur.Nummer}",
        DefaultExt = ".pdf"
    };
    
    if (dialog.ShowDialog() == true)
    {
        _exportService.ExportRezepturPdf(SelectedRezeptur, dialog.FileName);
        MessageBox.Show("PDF erfolgreich exportiert.", "Export");
    }
}
```

## WPF-Druckfunktion

```csharp
[RelayCommand]
private void DruckeRezeptur()
{
    if (SelectedRezeptur == null) return;
    
    var pd = new PrintDialog();
    if (pd.ShowDialog() != true) return;
    
    var doc = new FlowDocument();
    doc.PageWidth = pd.PrintableAreaWidth;
    doc.PageHeight = pd.PrintableAreaHeight;
    
    doc.Blocks.Add(new Paragraph(new Run($"Rezeptur {SelectedRezeptur.Nummer}")) 
        { FontSize = 16, FontWeight = FontWeights.Bold });
    doc.Blocks.Add(new Paragraph(new Run($"Datum: {SelectedRezeptur.Erstellungsdatum:d}")));
    
    // Tabelle für Zutaten
    var table = new Table();
    // ... Tabelle befüllen
    
    var docPaginator = ((IDocumentPaginatorSource)doc).DocumentPaginator;
    pd.PrintDocument(docPaginator, $"Rezeptur {SelectedRezeptur.Nummer}");
}
```

## Export-Service einrichten

Exportlogik in eigenen Service auslagern: `Services/ExportService.cs`

```csharp
public class ExportService
{
    public void ExportRohstoffePdf(IEnumerable<Rohstoff> rohstoffe, string filePath) { ... }
    public void ExportRohstoffeXlsx(IEnumerable<Rohstoff> rohstoffe, string filePath) { ... }
    public void ExportRezepturPdf(Rezeptur rezeptur, string filePath) { ... }
    public void ExportRezepturXlsx(Rezeptur rezeptur, string filePath) { ... }
}
```

## Checkliste

1. `ExportService.cs` in `Services/` erstellen
2. In ViewModel per Konstruktor injizieren
3. `SaveFileDialog` für Dateipfad verwenden
4. Fehlerbehandlung (try/catch) um `document.Save()` legen
5. Encoding für deutsche Umlaute prüfen (`PdfSharpCore` braucht teils explizite Fonts)
