package edu.jmaycon.spring.transactions.jpa.repository;

import java.util.List;
import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.jmaycon.spring.transactions.jpa.entity.BankAccountEntity;

@Repository
public class BankAccountRepository {

	@Autowired
	private EntityManager em;
	
	public void insert(BankAccountEntity entity) {
		em.unwrap(Session.class).persist(entity);
	}

	public BankAccountEntity get(String primaryKey) {
		return em.find(BankAccountEntity.class, primaryKey);
	}

	public void update(BankAccountEntity entity) {
		em.unwrap(Session.class).update(entity);
	}
	

	@SuppressWarnings("unchecked")
	public List<BankAccountEntity> findAll() {
		return em.unwrap(Session.class).createCriteria(BankAccountEntity.class).list();
	}

	public void flush() {
		em.unwrap(Session.class).flush();
	}
	
	public FlushModeType getFlushModeType() {
		return em.getFlushMode();
	}

	public void deleteAll() {
		em.unwrap(Session.class).createQuery(" delete from " + BankAccountEntity.class.getSimpleName()).executeUpdate();
	}

	public void accessEntityManager(Consumer<EntityManager> consumer) {
		consumer.accept(em);
	}

	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	public void accessEntityManagerTransactionNotSupported(Consumer<EntityManager> c) {
		c.accept(em);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void accessEntityManagerTransactionRequiresNew(Consumer<EntityManager> c) {
		c.accept(em);
	}
	

}
