using Microsoft.EntityFrameworkCore;
using RezepturMeister.Data;
using RezepturMeister.Models;

namespace RezepturMeister.Services;

public class SirupRezepturService
{
    private readonly AppDbContext _context;

    public SirupRezepturService(AppDbContext context)
    {
        _context = context;
    }

    public IEnumerable<SirupRezeptur> GetAll() =>
        _context.SirupRezepturen.Include(r => r.Positionen).ThenInclude(p => p.Komponente).AsNoTracking().ToList();

    public SirupRezeptur? GetById(int id) =>
        _context.SirupRezepturen.Include(r => r.Positionen).ThenInclude(p => p.Komponente).AsNoTracking().FirstOrDefault(r => r.Id == id);

    public void Add(SirupRezeptur rezeptur)
    {
        _context.ChangeTracker.Clear();
        _context.SirupRezepturen.Add(rezeptur);
        _context.SaveChanges();
        _context.ChangeTracker.Clear();
    }

    public void Update(SirupRezeptur updated)
    {
        _context.ChangeTracker.Clear();

        var incomingIds = updated.Positionen.Where(p => p.Id > 0).Select(p => p.Id).ToHashSet();
        var deletedPositionen = _context.SirupPositionen
            .Where(p => p.SirupRezepturId == updated.Id && !incomingIds.Contains(p.Id))
            .ToList();
        if (deletedPositionen.Any())
            _context.SirupPositionen.RemoveRange(deletedPositionen);

        _context.SirupRezepturen.Update(updated);
        _context.SaveChanges();
        _context.ChangeTracker.Clear();
    }

    public void Delete(int id)
    {
        _context.ChangeTracker.Clear();
        var rezeptur = _context.SirupRezepturen.Find(id);
        if (rezeptur != null)
        {
            _context.SirupRezepturen.Remove(rezeptur);
            _context.SaveChanges();
        }
        _context.ChangeTracker.Clear();
    }
}
