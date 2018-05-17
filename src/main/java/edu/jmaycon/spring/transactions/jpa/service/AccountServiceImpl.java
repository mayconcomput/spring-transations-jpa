package edu.jmaycon.spring.transactions.jpa.service;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.jmaycon.spring.transactions.jpa.entity.BankAccountEntity;
import edu.jmaycon.spring.transactions.jpa.repository.BankAccountRepository;

@Service
public class AccountServiceImpl implements AccountService{
	
	@Autowired
	private BankAccountRepository bankAccountRepository;
	

	@Override
	@Transactional()
	public void insert(BankAccountEntity bc1) {
		bankAccountRepository.insert(bc1);
	}
	
	@Override
	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	public void executeTransactionNotSupported(Consumer<BankAccountRepository> consumer) {
		consumer.accept(bankAccountRepository);
	}
	


	@Override
	@Transactional
	public void executeTransactionalDefault(Consumer<BankAccountRepository> consumer) {
		consumer.accept(bankAccountRepository);
	}
	
	@Override
	@Transactional(readOnly=true)
	public void executeTransactionalReadOnly(Consumer<BankAccountRepository> consumer) {
		consumer.accept(bankAccountRepository);
	}

	@Override
	@Transactional
	public void deleteAll() {
		bankAccountRepository.deleteAll();
	}

	@Override
	@Transactional(propagation=Propagation.NEVER)
	public void executeTransactionNever(Consumer<BankAccountRepository> consumer) {
		consumer.accept(bankAccountRepository);
	}

	
}
