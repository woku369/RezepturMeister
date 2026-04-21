using ClosedXML.Excel;
using PdfSharpCore.Drawing;
using PdfSharpCore.Pdf;
using RezepturMeister.Models;
using System.Globalization;

namespace RezepturMeister.Services;

public class ExportService
{
    // ── PDF ─────────────────────────────────────────────────────────────────

    public void ExportRohstoffePdf(IEnumerable<Rohstoff> rohstoffe, string filePath)
    {
        var document = new PdfDocument();
        document.Info.Title = "Rohstoffliste";

        var page = document.AddPage();
        page.Size = PdfSharpCore.PageSize.A4;
        var gfx = XGraphics.FromPdfPage(page);

        var fontTitle  = new XFont("Arial", 16, XFontStyle.Bold);
        var fontHeader = new XFont("Arial", 10, XFontStyle.Bold);
        var fontBody   = new XFont("Arial",  9, XFontStyle.Regular);

        double y = 40;
        gfx.DrawString("Rohstoffliste", fontTitle, XBrushes.Black, new XPoint(40, y));
        y += 30;

        gfx.DrawString("Name",             fontHeader, XBrushes.Black, new XPoint(40,  y));
        gfx.DrawString("Kategorie",        fontHeader, XBrushes.Black, new XPoint(200, y));
        gfx.DrawString("Dichte (g/ml)",    fontHeader, XBrushes.Black, new XPoint(340, y));
        gfx.DrawString("Alkohol (%)",      fontHeader, XBrushes.Black, new XPoint(440, y));
        y += 5;
        gfx.DrawLine(XPens.Black, 40, y, 555, y);
        y += 15;

        foreach (var r in rohstoffe)
        {
            if (y > page.Height - 50)
            {
                page = document.AddPage();
                page.Size = PdfSharpCore.PageSize.A4;
                gfx = XGraphics.FromPdfPage(page);
                y = 40;
            }
            gfx.DrawString(r.Name,                          fontBody, XBrushes.Black, new XPoint(40,  y));
            gfx.DrawString(r.Kategorie,                     fontBody, XBrushes.Black, new XPoint(200, y));
            gfx.DrawString(r.Dichte.ToString("F3"),         fontBody, XBrushes.Black, new XPoint(340, y));
            gfx.DrawString(r.Alkoholgehalt.ToString("F1"),  fontBody, XBrushes.Black, new XPoint(440, y));
            y += 18;
        }

        document.Save(filePath);
    }

    public void ExportRezepturPdf(Rezeptur rezeptur, string filePath)
    {
        var document = new PdfDocument();
        document.Info.Title = $"Rezeptur {rezeptur.Nummer}";

        var page = document.AddPage();
        page.Size = PdfSharpCore.PageSize.A4;
        var gfx = XGraphics.FromPdfPage(page);

        var fontTitle  = new XFont("Arial", 14, XFontStyle.Bold);
        var fontNormal = new XFont("Arial", 10, XFontStyle.Regular);
        var fontBold   = new XFont("Arial", 10, XFontStyle.Bold);

        double y = 40;
        gfx.DrawString($"Rezeptur {rezeptur.Nummer}", fontTitle, XBrushes.Black, new XPoint(40, y));
        y += 25;
        gfx.DrawString($"Datum:  {rezeptur.Erstellungsdatum:d}", fontNormal, XBrushes.Black, new XPoint(40, y));
        y += 15;
        gfx.DrawString($"Charge: {rezeptur.Chargennummer}",      fontNormal, XBrushes.Black, new XPoint(40, y));
        if (!string.IsNullOrWhiteSpace(rezeptur.Bemerkungen))
        {
            y += 15;
            gfx.DrawString($"Bemerkungen: {rezeptur.Bemerkungen}", fontNormal, XBrushes.Black, new XPoint(40, y));
        }
        y += 25;

        gfx.DrawString("Zutat",    fontBold, XBrushes.Black, new XPoint(40,  y));
        gfx.DrawString("Menge",    fontBold, XBrushes.Black, new XPoint(300, y));
        gfx.DrawString("Einheit",  fontBold, XBrushes.Black, new XPoint(370, y));
        gfx.DrawString("Anteil %", fontBold, XBrushes.Black, new XPoint(440, y));
        y += 5;
        gfx.DrawLine(XPens.Black, 40, y, 555, y);
        y += 15;

        double gesamtMenge = rezeptur.Zutaten.Sum(z =>
        {
            double m = z.Menge;
            if (z.Einheit == "ml") m *= (z.Rohstoff?.Dichte ?? z.ManuelleDichte);
            return m;
        });

        foreach (var zutat in rezeptur.Zutaten)
        {
            if (y > page.Height - 60)
            {
                page = document.AddPage();
                page.Size = PdfSharpCore.PageSize.A4;
                gfx = XGraphics.FromPdfPage(page);
                y = 40;
            }
            string name = zutat.Rohstoff?.Name ?? zutat.ManuellerName;
            double gewicht = zutat.Menge;
            if (zutat.Einheit == "ml") gewicht *= (zutat.Rohstoff?.Dichte ?? zutat.ManuelleDichte);
            double anteil = gesamtMenge > 0 ? gewicht / gesamtMenge * 100 : 0;

            gfx.DrawString(name,                         fontNormal, XBrushes.Black, new XPoint(40,  y));
            gfx.DrawString(zutat.Menge.ToString("F2"),   fontNormal, XBrushes.Black, new XPoint(300, y));
            gfx.DrawString(zutat.Einheit,                fontNormal, XBrushes.Black, new XPoint(370, y));
            gfx.DrawString(anteil.ToString("F1") + " %", fontNormal, XBrushes.Black, new XPoint(440, y));
            y += 18;
        }

        y += 10;
        gfx.DrawLine(XPens.Black, 40, y, 555, y);
        y += 15;
        gfx.DrawString($"Gesamtgewicht: {gesamtMenge:F2} g", fontBold, XBrushes.Black, new XPoint(40, y));

        document.Save(filePath);
    }

    // ── XLSX ────────────────────────────────────────────────────────────────

    public void ExportRohstoffeXlsx(IEnumerable<Rohstoff> rohstoffe, string filePath)
    {
        using var wb = new XLWorkbook();
        var ws = wb.Worksheets.Add("Rohstoffe");

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

    public void ExportRezepturXlsx(Rezeptur rezeptur, string filePath)
    {
        using var wb = new XLWorkbook();
        // Worksheet-Name darf max. 31 Zeichen haben und keine Sonderzeichen: \ / * ? : [ ]
        string sheetName = $"Rez {rezeptur.Nummer}";
        foreach (char c in new[] { '\\', '/', '*', '?', ':', '[', ']' })
            sheetName = sheetName.Replace(c, '-');
        if (sheetName.Length > 31) sheetName = sheetName[..31];
        var ws = wb.Worksheets.Add(sheetName);

        ws.Cell(1, 1).Value = $"Rezeptur {rezeptur.Nummer}";
        ws.Cell(1, 1).Style.Font.Bold = true;
        ws.Cell(1, 1).Style.Font.FontSize = 14;
        ws.Cell(2, 1).Value = $"Datum: {rezeptur.Erstellungsdatum:d}";
        ws.Cell(3, 1).Value = $"Charge: {rezeptur.Chargennummer}";
        ws.Cell(4, 1).Value = rezeptur.Bemerkungen;

        ws.Cell(6, 1).Value = "Zutat";
        ws.Cell(6, 2).Value = "Menge";
        ws.Cell(6, 3).Value = "Einheit";
        ws.Cell(6, 4).Value = "Anteil %";
        ws.Row(6).Style.Font.Bold = true;
        ws.Row(6).Style.Fill.BackgroundColor = XLColor.LightGray;

        double gesamtMenge = rezeptur.Zutaten.Sum(z =>
        {
            double m = z.Menge;
            if (z.Einheit == "ml") m *= (z.Rohstoff?.Dichte ?? z.ManuelleDichte);
            return m;
        });

        int row = 7;
        foreach (var zutat in rezeptur.Zutaten)
        {
            double gewicht = zutat.Menge;
            if (zutat.Einheit == "ml") gewicht *= (zutat.Rohstoff?.Dichte ?? zutat.ManuelleDichte);
            double anteil = gesamtMenge > 0 ? Math.Round(gewicht / gesamtMenge * 100, 1) : 0;

            ws.Cell(row, 1).Value = zutat.Rohstoff?.Name ?? zutat.ManuellerName;
            ws.Cell(row, 2).Value = zutat.Menge;
            ws.Cell(row, 3).Value = zutat.Einheit;
            ws.Cell(row, 4).Value = anteil;
            row++;
        }

        ws.Cell(row + 1, 1).Value = "Gesamtgewicht (g):";
        ws.Cell(row + 1, 1).Style.Font.Bold = true;
        ws.Cell(row + 1, 2).Value = Math.Round(gesamtMenge, 2);

        ws.Columns().AdjustToContents();
        wb.SaveAs(filePath);
    }

    // ── Nährwerte PDF ────────────────────────────────────────────────────────

    public void ExportNaehrwertePdf(Rezeptur rezeptur, NaehrwertErgebnis nw, string filePath)
    {
        var gc = CultureInfo.GetCultureInfo("de-DE");
        var doc = new PdfDocument();
        doc.Info.Title = $"Nährwerte {rezeptur.Nummer}";

        var page = doc.AddPage();
        page.Size = PdfSharpCore.PageSize.A4;
        var gfx = XGraphics.FromPdfPage(page);
        double pageBottom = page.Height - 50;

        var fTitle    = new XFont("Arial", 14, XFontStyle.Bold);
        var fHead     = new XFont("Arial", 10, XFontStyle.Bold);
        var fBody     = new XFont("Arial",  9, XFontStyle.Regular);
        var fSmall    = new XFont("Arial",  8, XFontStyle.Regular);
        var warnBrush = new XSolidBrush(XColor.FromArgb(180, 80, 0));

        double y = 40;

        void NewPageIfNeeded(double needed = 20)
        {
            if (y + needed > pageBottom)
            {
                page = doc.AddPage();
                page.Size = PdfSharpCore.PageSize.A4;
                gfx = XGraphics.FromPdfPage(page);
                pageBottom = page.Height - 50;
                y = 40;
            }
        }

        // ── Kopfzeile ──
        string rezName = string.IsNullOrWhiteSpace(rezeptur.Name) ? "" : $" — {rezeptur.Name}";
        gfx.DrawString($"Rezeptur {rezeptur.Nummer}{rezName}", fTitle, XBrushes.Black, new XPoint(40, y)); y += 20;
        gfx.DrawString($"Datum: {rezeptur.Erstellungsdatum:d}   Charge: {rezeptur.Chargennummer}", fBody, XBrushes.Black, new XPoint(40, y)); y += 30;

        // ── Zutatenliste (LMIV: absteigend nach Gewicht) ──
        gfx.DrawString("ZUTATENLISTE", fHead, XBrushes.Black, new XPoint(40, y)); y += 6;
        gfx.DrawLine(XPens.Black, 40, y, 555, y); y += 14;

        bool hatFehlende = nw.FehlendeDaten.Any();
        string zutatText = string.Join(", ", nw.Zutatenliste.Select(z =>
            !z.HatNaehrwertdaten && hatFehlende
                ? $"{z.Name}* ({z.Prozent.ToString("F1", gc)} %)"
                : $"{z.Name} ({z.Prozent.ToString("F1", gc)} %)"));

        foreach (var line in WrapText(gfx, zutatText, fBody, 515))
        {
            NewPageIfNeeded(14);
            gfx.DrawString(line, fBody, XBrushes.Black, new XPoint(40, y)); y += 14;
        }

        if (hatFehlende)
        {
            y += 4;
            NewPageIfNeeded(14);
            gfx.DrawString("* Nährwertdaten fehlen — Angaben unvollständig.", fSmall, warnBrush, new XPoint(40, y)); y += 14;
        }
        y += 16;

        // ── Nährwertdeklaration ──
        NewPageIfNeeded(180);
        gfx.DrawString("NÄHRWERTDEKLARATION", fHead, XBrushes.Black, new XPoint(40, y)); y += 6;
        gfx.DrawLine(XPens.Black, 40, y, 555, y); y += 14;

        gfx.DrawString("pro 100 ml", fHead, XBrushes.Black, new XPoint(310, y));
        gfx.DrawString("pro 100 g",  fHead, XBrushes.Black, new XPoint(440, y));
        y += 20;

        // Brennwert (kJ + kcal)
        NewPageIfNeeded(36);
        gfx.DrawString("Brennwert", fHead, XBrushes.Black, new XPoint(40, y));
        gfx.DrawString($"{nw.Energie_kJ_per100ml.ToString("F0", gc)} kJ",   fBody, XBrushes.Black, new XPoint(310, y));
        gfx.DrawString($"{nw.Energie_kJ.ToString("F0", gc)} kJ",            fBody, XBrushes.Black, new XPoint(440, y));
        y += 15;
        gfx.DrawString($"{nw.Energie_kcal_per100ml.ToString("F0", gc)} kcal", fBody, XBrushes.Black, new XPoint(310, y));
        gfx.DrawString($"{nw.Energie_kcal.ToString("F0", gc)} kcal",          fBody, XBrushes.Black, new XPoint(440, y));
        y += 20;

        void NwRow(string label, double per100ml, double per100g, bool indent = false)
        {
            NewPageIfNeeded(18);
            gfx.DrawString(label, fBody, XBrushes.Black, new XPoint(indent ? 55 : 40, y));
            gfx.DrawString($"{per100ml.ToString("F1", gc)} g", fBody, XBrushes.Black, new XPoint(310, y));
            gfx.DrawString($"{per100g.ToString("F1", gc)} g",  fBody, XBrushes.Black, new XPoint(440, y));
            y += 18;
        }

        NwRow("Fett",                        nw.Fett_per100ml,                   nw.Fett);
        NwRow("davon gesättigte Fettsäuren", nw.GesaettigteFettsaeuren_per100ml, nw.GesaettigteFettsaeuren, indent: true);
        NwRow("Kohlenhydrate",               nw.Kohlenhydrate_per100ml,          nw.Kohlenhydrate);
        NwRow("davon Zucker",                nw.Zucker_per100ml,                 nw.Zucker, indent: true);
        NwRow("Ballaststoffe",               nw.Ballaststoffe_per100ml,          nw.Ballaststoffe);
        NwRow("Eiweiß",                      nw.Eiweiss_per100ml,                nw.Eiweiss);
        NwRow("Salz",                        nw.Salz_per100ml,                   nw.Salz);

        if (hatFehlende)
        {
            y += 12;
            NewPageIfNeeded(28);
            string fehlend = string.Join(", ", nw.FehlendeDaten);
            gfx.DrawString($"Hinweis: Fehlende Nährwertdaten für: {fehlend}", fSmall, warnBrush, new XPoint(40, y)); y += 13;
            gfx.DrawString("Bitte fehlende Daten in der Rohstoffliste nachtragen.", fSmall, warnBrush, new XPoint(40, y));
        }

        doc.Save(filePath);
    }

    // ── Nährwerte XLSX ───────────────────────────────────────────────────────

    public void ExportNaehrwerteXlsx(Rezeptur rezeptur, NaehrwertErgebnis nw, string filePath)
    {
        using var wb = new XLWorkbook();

        // ── Tabellenblatt 1: Nährwertdeklaration ──
        var ws1 = wb.Worksheets.Add("Nährwerte");

        string kopf = string.IsNullOrWhiteSpace(rezeptur.Name)
            ? rezeptur.Nummer
            : $"{rezeptur.Nummer} — {rezeptur.Name}";
        ws1.Cell(1, 1).Value = $"Rezeptur {kopf}";
        ws1.Cell(1, 1).Style.Font.Bold = true;
        ws1.Cell(1, 1).Style.Font.FontSize = 13;
        ws1.Cell(2, 1).Value = $"Datum: {rezeptur.Erstellungsdatum:d}   Charge: {rezeptur.Chargennummer}";

        ws1.Cell(4, 1).Value = "Nährwert";
        ws1.Cell(4, 2).Value = "pro 100 ml";
        ws1.Cell(4, 3).Value = "pro 100 g";
        ws1.Cell(4, 4).Value = "Einheit";
        ws1.Row(4).Style.Font.Bold = true;
        ws1.Row(4).Style.Fill.BackgroundColor = XLColor.LightGray;

        static void AddNwRow(IXLWorksheet ws, int row, string label, double per100ml, double per100g, string unit, bool indent = false)
        {
            ws.Cell(row, 1).Value = indent ? "  " + label : label;
            ws.Cell(row, 2).Value = Math.Round(per100ml, 2);
            ws.Cell(row, 3).Value = Math.Round(per100g, 2);
            ws.Cell(row, 4).Value = unit;
        }

        AddNwRow(ws1, 5,  "Brennwert",                   nw.Energie_kJ_per100ml,            nw.Energie_kJ,            "kJ");
        AddNwRow(ws1, 6,  "Brennwert",                   nw.Energie_kcal_per100ml,          nw.Energie_kcal,          "kcal");
        AddNwRow(ws1, 7,  "Fett",                        nw.Fett_per100ml,                  nw.Fett,                  "g");
        AddNwRow(ws1, 8,  "davon gesättigte Fettsäuren", nw.GesaettigteFettsaeuren_per100ml, nw.GesaettigteFettsaeuren, "g", indent: true);
        AddNwRow(ws1, 9,  "Kohlenhydrate",               nw.Kohlenhydrate_per100ml,         nw.Kohlenhydrate,         "g");
        AddNwRow(ws1, 10, "davon Zucker",                nw.Zucker_per100ml,                nw.Zucker,                "g", indent: true);
        AddNwRow(ws1, 11, "Ballaststoffe",               nw.Ballaststoffe_per100ml,         nw.Ballaststoffe,         "g");
        AddNwRow(ws1, 12, "Eiweiß",                      nw.Eiweiss_per100ml,               nw.Eiweiss,               "g");
        AddNwRow(ws1, 13, "Salz",                        nw.Salz_per100ml,                  nw.Salz,                  "g");

        ws1.Row(5).Style.Font.Bold = true;
        ws1.Row(6).Style.Font.Bold = true;

        if (nw.FehlendeDaten.Any())
        {
            ws1.Cell(15, 1).Value = $"Hinweis: Fehlende Nährwertdaten für: {string.Join(", ", nw.FehlendeDaten)}";
            ws1.Cell(15, 1).Style.Font.FontColor = XLColor.OrangeRed;
            ws1.Cell(16, 1).Value = "Bitte fehlende Daten in der Rohstoffliste nachtragen.";
            ws1.Cell(16, 1).Style.Font.FontColor = XLColor.OrangeRed;
        }

        ws1.Columns().AdjustToContents();

        // ── Tabellenblatt 2: Zutatenliste (intern) ──
        var ws2 = wb.Worksheets.Add("Zutaten");
        ws2.Cell(1, 1).Value = "Zutat";
        ws2.Cell(1, 2).Value = "Gewicht (g)";
        ws2.Cell(1, 3).Value = "Anteil (%)";
        ws2.Cell(1, 4).Value = "Nährwertdaten";
        ws2.Row(1).Style.Font.Bold = true;
        ws2.Row(1).Style.Fill.BackgroundColor = XLColor.LightGray;

        int row = 2;
        foreach (var z in nw.Zutatenliste)
        {
            ws2.Cell(row, 1).Value = z.Name;
            ws2.Cell(row, 2).Value = Math.Round(z.Gewicht_g, 2);
            ws2.Cell(row, 3).Value = Math.Round(z.Prozent, 1);
            ws2.Cell(row, 4).Value = z.HatNaehrwertdaten ? "✓" : "fehlt";
            if (!z.HatNaehrwertdaten)
                ws2.Cell(row, 4).Style.Font.FontColor = XLColor.OrangeRed;
            row++;
        }

        ws2.Columns().AdjustToContents();
        wb.SaveAs(filePath);
    }

    // ── Hilfsmethode: Text-Umbruch für PDF ──────────────────────────────────

    private static List<string> WrapText(XGraphics gfx, string text, XFont font, double maxWidth)
    {
        var lines = new List<string>();
        string current = "";
        foreach (var word in text.Split(' '))
        {
            string candidate = current.Length == 0 ? word : current + " " + word;
            if (current.Length > 0 && gfx.MeasureString(candidate, font).Width > maxWidth)
            {
                lines.Add(current);
                current = word;
            }
            else
            {
                current = candidate;
            }
        }
        if (current.Length > 0) lines.Add(current);
        return lines;
    }
}
