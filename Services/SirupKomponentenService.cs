using Microsoft.EntityFrameworkCore;
using RezepturMeister.Data;
using RezepturMeister.Models;

namespace RezepturMeister.Services;

public class SirupKomponentenService
{
    private readonly AppDbContext _context;

    public SirupKomponentenService(AppDbContext context)
    {
        _context = context;
    }

    public IEnumerable<SirupKomponente> GetAll() => _context.SirupKomponenten.AsNoTracking().ToList();

    public SirupKomponente? GetById(int id) => _context.SirupKomponenten.Find(id);

    public void Add(SirupKomponente komponente)
    {
        _context.ChangeTracker.Clear();
        _context.SirupKomponenten.Add(komponente);
        _context.SaveChanges();
        _context.ChangeTracker.Clear();
    }

    public bool IsReferenced(int id) =>
        _context.SirupPositionen.Any(p => p.KomponenteId == id);

    public void Update(SirupKomponente komponente)
    {
        _context.ChangeTracker.Clear();
        _context.SirupKomponenten.Update(komponente);
        _context.SaveChanges();
        _context.ChangeTracker.Clear();
    }

    public void Delete(int id)
    {
        _context.ChangeTracker.Clear();
        var komponente = _context.SirupKomponenten.Find(id);
        if (komponente != null)
        {
            _context.SirupKomponenten.Remove(komponente);
            _context.SaveChanges();
        }
        _context.ChangeTracker.Clear();
    }
}
