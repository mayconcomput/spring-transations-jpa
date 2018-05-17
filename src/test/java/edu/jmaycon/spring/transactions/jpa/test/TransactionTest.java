package edu.jmaycon.spring.transactions.jpa.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.jmaycon.spring.transactions.jpa.config.Application;
import edu.jmaycon.spring.transactions.jpa.entity.BankAccountEntity;
import edu.jmaycon.spring.transactions.jpa.service.AccountService;

/**
 * @author Maycon Cesar
 * 
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TransactionTest {
	
	@Autowired
	private AccountService accountService;
	
	@Before
	public void before() {
		BankAccountEntity bc1 = new BankAccountEntity("acc1", new BigDecimal("100.00"));
		accountService.insert(bc1);
		
		BankAccountEntity bc2 = new BankAccountEntity("acc2", new BigDecimal("100.00"));
		accountService.insert(bc2);
	}
	
	@After
	public void after() {
		accountService.deleteAll();
	}
	
	/**
	 * It's executed with {@link Transactional}
	 * the default propagation is {@link Propagation#REQUIRED} and readOnly = false
	 * which are defaults.
	 * Even if we don't call explicit the update statement the update will occur
	 * because the flush mode is setting to {@link FlushModeType#AUTO}
	 */
	@Test
	public void assertTransactionalDefault() {
		accountService.executeTransactionalDefault(accountRepo -> {
			BankAccountEntity bc1 = accountRepo.get("acc1");
			BankAccountEntity bc2 = accountRepo.get("acc2");
			bc1.deposit(bc2.withDraw());

			// The mode of flush it's in auto mode so, the update will occur
			assertEquals(FlushModeType.AUTO, accountRepo.getFlushModeType());
			
			accountRepo.accessEntityManager(em -> {
				assertTrue(em.isJoinedToTransaction()); // There is active transaction
			});
			
		});	
		
		accountService.executeTransactionNotSupported(repo -> {
			BankAccountEntity bc1 = repo.get("acc1");
			BankAccountEntity bc2 = repo.get("acc2");
			
			assertEquals(new BigDecimal("200.00"), bc1.getBalance());
			assertEquals(new BigDecimal(  "0.00"), bc2.getBalance());
			
		});
	}
	
	/**
	 * The {@link Transactional#readOnly()} is set to true
	 * so the {@link EntityManager#getFlushMode()} becomes null,
	 * and the flush of the update does no occur
	 * i.e. only if explicit called the update will
	 * reflect on data base
	 */
	@Test
	public void assertTransactionalReadOnly() {
		accountService.executeTransactionalReadOnly(accountRepo -> {
			BankAccountEntity bc1 = accountRepo.get("acc1");
			BankAccountEntity bc2 = accountRepo.get("acc2");
			bc1.deposit(bc2.withDraw());
			
			// Flush in manual mode as the transaction was marked as read only
			assertNull(accountRepo.getFlushModeType());
			
			accountRepo.accessEntityManager(em -> {
				assertTrue(em.isJoinedToTransaction()); // There is active transaction
			});
			
		});	

		// Nothing will happens because the flush is MANUAL and because of that the value don't gets updated
		accountService.executeTransactionNotSupported(repo -> {
			BankAccountEntity bc1 = repo.get("acc1");
			BankAccountEntity bc2 = repo.get("acc2");
			
			assertEquals(new BigDecimal("100.00"), bc1.getBalance());
			assertEquals(new BigDecimal("100.00"), bc2.getBalance());
			
		});
	}
	
	/**
	 * The {@link Transactional#readOnly()} is set to true
	 * so the {@link EntityManager#getFlushMode()} becomes null,
	 * if the flush is called an error will raise.
	 */
	@Test(expected = JpaSystemException.class)
	public void assertTransactionalReadOnlyHibernateFlush() {
		// Here we invoke the flush operation manually and the update will occur
		accountService.executeTransactionalReadOnly(accountRepo -> {
			BankAccountEntity bc1 = accountRepo.get("acc1");
			BankAccountEntity bc2 = accountRepo.get("acc2");
			bc1.deposit(bc2.withDraw());
			
			assertNull(accountRepo.getFlushModeType());
			
			accountRepo.accessEntityManager(em -> {
				assertTrue(em.isJoinedToTransaction()); // There is ACTIVE transaction
				em.unwrap(Session.class).flush();  // Will raise an error
			});
		});
	}
	
	/**
	 * Although the transaction is in flush {@link FlushModeType#AUTO}
	 * the flush will only occur if explicit
	 * when occur will raise the error {@link InvalidDataAccessApiUsageException}
	 * 
	 * If invoked {@link EntityManager#persist(Object)} or {@link EntityManager#merge(Object)}EntityManage.merge
	 * will raise the same error also.
	 */
	@Test(expected = InvalidDataAccessApiUsageException.class)
	public void assertTransactionNeverWithEntityManagerFlush() {
		accountService.executeTransactionNever(accountRepo -> {
			BankAccountEntity bc1 = accountRepo.get("acc1");
			BankAccountEntity bc2 = accountRepo.get("acc2");
			bc1.deposit(bc2.withDraw());
			
			// Although the transaction is using the propagation NEVER, the flush will still be auto
			assertEquals(FlushModeType.AUTO, accountRepo.getFlushModeType());
			
			accountRepo.accessEntityManager(em -> {
				assertFalse(em.isJoinedToTransaction()); // There is NO active transaction
				em.flush();
			});
		});	
	}
	
	/**
	 * As there's no proxy for the hibernate session the flush occur
	 * with no error.
	 * 
	 * @see #assertTransactionNeverWithEntityManagerFlush()
	 */
	@Test
	public void assertTransactionNeverWithHibernateFlush() {
		accountService.executeTransactionNever(accountRepo -> {
			BankAccountEntity bc1 = accountRepo.get("acc1");
			BankAccountEntity bc2 = accountRepo.get("acc2");
			bc1.deposit(bc2.withDraw());
			
			// Although the transaction is using the propagation NEVER, the flush will still be auto
			assertEquals(FlushModeType.AUTO, accountRepo.getFlushModeType());
			
			accountRepo.accessEntityManager(em -> {
				assertFalse(em.isJoinedToTransaction()); // There is NO active transaction
				em.unwrap(Session.class).flush();
			});
		});	
	}
	
	/**
	 * A transaction  default it's started 
	 * and a nested method a change on
	 * the object where the transaction is marked as {@link Propagation#NOT_SUPPORTED}
	 * , in this scenario the value will not the value of the object
	 * and any call explicit to {@link EntityManager#merge(Object)} or {@link EntityManager#persist(Object)}
	 */
	@Test
	public void assertNestedTransactionNotSupported() {
		accountService.executeTransactionalDefault(accountRepo -> {
			BankAccountEntity bc1 = accountRepo.get("acc1");
			bc1.deposit(new BigDecimal("10"));

			// The mode of flush it's in auto mode, the update will occur
			assertEquals(FlushModeType.AUTO, accountRepo.getFlushModeType());
			
			// The transaction is SUSPENDED but the entity manager it's the same
			accountRepo.accessEntityManager(em1 -> {
				
				assertTrue(em1.isJoinedToTransaction()); // There is ACTIVE transaction
				
				accountRepo.accessEntityManagerTransactionNotSupported(em2 -> {
					
					assertFalse(em2.isJoinedToTransaction()); // There is NO active transaction
					
					// as the operation occur inside where the transaction was suspend no value will be changed
					BankAccountEntity bc = accountRepo.get("acc1");
					bc.setBalance(BigDecimal.ZERO);
				});
			});
		});	
		
		accountService.executeTransactionNotSupported(repo -> {
			BankAccountEntity bc1 = repo.get("acc1");
			
			assertEquals(new BigDecimal("110.00"), bc1.getBalance());
		});
	}
	
	/**
	 * Enter the context without a transaction and then
	 * invoke a method with the {@link Propagation#REQUIRES_NEW}
	 * and inside of this method call another one with {@link Propagation#REQUIRES_NEW}
	 * The transaction must occur in separated way, so when the second one
	 * fails don't affect the first one.
	 */
	@Test
	public void assertTransactionalRequiresNew() {
		accountService.executeTransactionNever(accountRepo -> {
			// TRANSACTION 1
			accountRepo.accessEntityManagerTransactionRequiresNew(em1 -> {
				BankAccountEntity bc1 = accountRepo.get("acc1");
				bc1.setBalance(new BigDecimal("11"));
				em1.merge(bc1);
				em1.flush();
				
				RuntimeException ex = null;
				try {
					// NEW TRANSACTION
					accountRepo.accessEntityManagerTransactionRequiresNew(em2 -> {
						BankAccountEntity bc2 = accountRepo.get("acc2");
						bc2.setBalance(new BigDecimal("122"));
						em2.merge(bc2);
						em2.flush();
						throw new RuntimeException("Rollback"); // Will automatically rollback because of the exception
					});	
				}catch(RuntimeException e) {
					ex = e;
				}
				assertEquals("Rollback", ex.getMessage());
			});
		});	
		
		accountService.executeTransactionNotSupported(repo -> {
			BankAccountEntity bc1 = repo.get("acc1");
			BankAccountEntity bc2 = repo.get("acc2");
			
			assertEquals(new BigDecimal("11.00"), bc1.getBalance());
			assertEquals(new BigDecimal("100.00"), bc2.getBalance()); // remains unchanged
		});
		
	}
}
