using Microsoft.EntityFrameworkCore;
using RezepturMeister.Data;
using RezepturMeister.Models;

namespace RezepturMeister.Services;

public class RohstoffService
{
    private readonly AppDbContext _context;

    public RohstoffService(AppDbContext context)
    {
        _context = context;
    }

    public IEnumerable<Rohstoff> GetAll() => _context.Rohstoffe.AsNoTracking().ToList();

    public Rohstoff? GetById(int id) => _context.Rohstoffe.Find(id);

    public void Add(Rohstoff rohstoff)
    {
        _context.ChangeTracker.Clear();
        _context.Rohstoffe.Add(rohstoff);
        _context.SaveChanges();
        _context.ChangeTracker.Clear();
    }

    public void Update(Rohstoff rohstoff)
    {
        _context.ChangeTracker.Clear();
        _context.Rohstoffe.Update(rohstoff);
        _context.SaveChanges();
        _context.ChangeTracker.Clear();
    }

    public bool IsReferenced(int id) =>
        _context.Zutaten.Any(z => z.RohstoffId == id);

    public void Delete(int id)
    {
        _context.ChangeTracker.Clear();
        var rohstoff = _context.Rohstoffe.Find(id);
        if (rohstoff != null)
        {
            _context.Rohstoffe.Remove(rohstoff);
            _context.SaveChanges();
        }
        _context.ChangeTracker.Clear();
    }
}
