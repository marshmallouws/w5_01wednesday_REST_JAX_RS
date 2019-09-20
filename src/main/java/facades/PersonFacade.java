package facades;

import entities.Person;
import exceptions.PersonNotFoundException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

/**
 *
 * Rename Class to a relevant name Add add relevant facade methods
 */
public class PersonFacade {

    private static PersonFacade instance;
    private static EntityManagerFactory emf;

    //Private Constructor to ensure Singleton
    private PersonFacade() {
    }

    /**
     *
     * @param _emf
     * @return an instance of this facade class.
     */
    public static PersonFacade getPersonFacade(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new PersonFacade();
        }
        return instance;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public long getPersonCount() {
        EntityManager em = emf.createEntityManager();
        try {
            long personCount = (long) em.createQuery("SELECT COUNT(r) FROM Person r").getSingleResult();
            return personCount;
        } finally {
            em.close();
        }

    }

    public Person addPerson(String fName, String lName, String phone) {
        EntityManager em = getEntityManager();
        Person p = new Person(fName, lName, phone);
        try {
            em.getTransaction().begin();
            em.persist(p);
            em.getTransaction().commit();
            return p;
        } finally {
            em.close();
        }
    }

    public Person deletePerson(int id) throws PersonNotFoundException {
        EntityManager em = getEntityManager();
        try {
            Person p = em.find(Person.class, id);
            if (p == null) {
                throw new PersonNotFoundException("Could not delete, provided id does not exist");
            }
            em.getTransaction().begin();
            em.remove(p);
            em.getTransaction().commit();
            return p;
        } finally {
            em.close();
        }
    }

    public Person getPerson(int id) throws PersonNotFoundException {
        EntityManager em = getEntityManager();
        try {
            Person p = em.find(Person.class, id);
            if(p == null) {
                throw new PersonNotFoundException("No person with provided id found");
            }
            return p;
        } finally {
            em.close();
        }
    }

    public List<Person> getAllPersons() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Person> query
                    = em.createQuery("SELECT p FROM Person p", Person.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Person editPerson(Person p) throws PersonNotFoundException {
        p.setLastEdited();
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            TypedQuery<Person> query
                    = em.createQuery("UPDATE Person p SET p.firstname = :firstname, p.lastname = :lastname, p.phone = :phone, p.lastEdited = :last  WHERE p.id = :id", Person.class);
            query.setParameter("firstname", p.getFirstname());
            query.setParameter("lastname", p.getLastname());
            query.setParameter("phone", p.getPhone());
            query.setParameter("last", p.getLastEdited());
            query.setParameter("id", p.getId());
            query.executeUpdate();
            em.getTransaction().commit();
            
        } finally {
            em.close();
        }
        return p;
    }
}
