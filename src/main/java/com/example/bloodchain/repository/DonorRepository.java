package com.example.bloodchain.repository;

import com.example.bloodchain.model.Donor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public class DonorRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // ✅ Get all donors from the database
    public List<Donor> findAll() {
        String query = "SELECT d FROM Donor d";  // JPQL query (not SQL)
        return entityManager.createQuery(query, Donor.class).getResultList();
    }

    // ✅ Add new donor
    public void save(Donor donor) {
        entityManager.persist(donor);
    }

    // ✅ Find donor by ID
    public Donor findById(Long id) {
        return entityManager.find(Donor.class, id);
    }

    // ✅ Delete donor by ID
    public void deleteById(Long id) {
        Donor donor = findById(id);
        if (donor != null) {
            entityManager.remove(donor);
        }
    }

    // ✅ Count total donors
    // ✅ Count total donors
    public long count() {
        String query = "SELECT COUNT(d) FROM Donor d";
        return (long) entityManager.createQuery(query, Long.class).getSingleResult();
    }

    // ✅ Find donor by email (added)
    public Donor findByEmail(String email) {
        try {
            String query = "SELECT d FROM Donor d WHERE d.email = :email";
            return entityManager.createQuery(query, Donor.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}
