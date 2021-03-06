package com.github.alex1304.ultimategdbot.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.github.alex1304.ultimategdbot.core.Database;
import com.github.alex1304.ultimategdbot.core.UltimateGDBot;

/**
 * Utility methods for database fetching
 *
 * @author Alex1304
 */
public class DatabaseUtils {
	
	/**
	 * Allows to find a database entity by its ID. Returns null if object is not found
	 * 
	 * @param entityClass
	 *            - class of the entity
	 * @param key
	 *            - the ID
	 * @param <T>
	 *            - The entity type
	 * @param <K>
	 *            - the ID type
	 * @return T
	 * 
	 */
	public static <T, K extends Serializable> T findByID(Class<T> entityClass, K key) {
		T result = null;
		Session s = Database.newSession();
		
		try {
			result = s.load(entityClass, key);
		} catch (ObjectNotFoundException e) {
		}
		finally {
			s.close();
		}
		
		return result;
	}
	
	/**
	 * Makes a simple query and returns an list containing the results
	 * 
	 * @param query
	 *            - the HQL query
	 * @param params
	 *            - the query params
	 * @param <T>
	 *            - the entity type
	 * @return List&lt;Object&gt;
	 */
	public static <T> List<T> query(Class<T> entityClass, String query, Object... params) {
		Session s = Database.newSession();
		List<T> list = new ArrayList<>();
		
		try {
			Query<T> q = s.createQuery(query, entityClass);
			for (int i = 0 ; i < params.length ; i++) {
				q.setParameter(i, params[i]);
			}
			list.addAll(q.getResultList());
		} finally {
			s.close();
		}
		
		return list;
	}
	
	/**
	 * Saves an object in database
	 * 
	 * @param obj - the object to save
	 * @return boolean - whether it has saved without issues
	 */
	public static boolean save(Object obj) {
		return performTransaction(session -> session.saveOrUpdate(obj), false);
	}
	
	/**
	 * Deleted an object from database
	 * 
	 * @param obj - the object to save
	 * @return boolean - whether it has deleted without issues
	 */
	public static boolean delete(Object obj) {
		return performTransaction(session -> session.delete(obj), true);
	}
	
	/**
	 * Performs what is inside txConsumer with a transaction
	 * 
	 * @param txConsumer
	 *            - what should be done with the session during the transaction
	 * @param flush
	 *            - whether to flush
	 * @return boolean - whether the transaction has been successful
	 */
	public static boolean performTransaction(Consumer<Session> txConsumer, boolean flush) {
		Session s = Database.newSession();
		Transaction tx = null;
		boolean success = false;
		
		try {
			tx = s.beginTransaction();
			txConsumer.accept(s);
			if (flush)
				s.flush();
			tx.commit();
			success = true;
		} catch (RuntimeException e) {
			if (tx != null)
				tx.rollback();
			UltimateGDBot.logException(e);
		} finally {
			s.close();
		}
		
		return success;
	}

}
