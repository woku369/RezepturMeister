using RezepturMeister.Data;
using RezepturMeister.Models;
using Microsoft.EntityFrameworkCore;

namespace RezepturMeister.Services;

public class RezepturService
{
    private readonly AppDbContext _context;

    public RezepturService(AppDbContext context)
    {
        _context = context;
    }

    public IEnumerable<Rezeptur> GetAll() =>
        _context.Rezepturen.Include(r => r.Zutaten).ThenInclude(z => z.Rohstoff).AsNoTracking().ToList();

    public Rezeptur? GetById(int id) =>
        _context.Rezepturen.Include(r => r.Zutaten).ThenInclude(z => z.Rohstoff).AsNoTracking().FirstOrDefault(r => r.Id == id);

    public void Add(Rezeptur rezeptur)
    {
        _context.ChangeTracker.Clear();
        _context.Rezepturen.Add(rezeptur);
        _context.SaveChanges();
        _context.ChangeTracker.Clear();
    }

    public void Update(Rezeptur updated)
    {
        _context.ChangeTracker.Clear();

        // Zutaten ermitteln die entfernt wurden (in DB vorhanden, aber nicht mehr in updated.Zutaten)
        var incomingIds = updated.Zutaten.Where(z => z.Id > 0).Select(z => z.Id).ToHashSet();
        var deletedZutaten = _context.Zutaten
            .Where(z => z.RezepturId == updated.Id && !incomingIds.Contains(z.Id))
            .ToList();
        if (deletedZutaten.Any())
            _context.Zutaten.RemoveRange(deletedZutaten);

        _context.Rezepturen.Update(updated);
        _context.SaveChanges();
        _context.ChangeTracker.Clear();
    }

    public void Delete(int id)
    {
        _context.ChangeTracker.Clear();
        var rezeptur = _context.Rezepturen.Find(id);
        if (rezeptur != null)
        {
            _context.Rezepturen.Remove(rezeptur);
            _context.SaveChanges();
        }
        _context.ChangeTracker.Clear();
    }

    public string GenerateNextVersion(string baseNummer)
    {
        var existing = _context.Rezepturen.Where(r => r.Nummer.StartsWith(baseNummer + ".")).Select(r => r.Nummer).ToList();
        int maxVersion = 0;
        foreach (var num in existing)
        {
            if (int.TryParse(num.Split('.').Last(), out int v))
                maxVersion = Math.Max(maxVersion, v);
        }
        return $"{baseNummer}.{maxVersion + 1}";
    }
}