package dao;

import java.util.List;

public interface BaseDAO<T, ID> {
    /** Saves a new entity to the database. */
    T save(T entity);

    /** Finds an entity by its unique ID. */
    T findById(ID id);

    /** Retrieves all entities of this type. */
    List<T> findAll();

    /** Updates an existing entity in the database. */
    boolean update(T entity);

    /** Deletes an entity by its unique ID. */
    boolean delete(ID id);
}