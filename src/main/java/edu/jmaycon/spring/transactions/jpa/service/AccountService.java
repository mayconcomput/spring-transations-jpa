package edu.jmaycon.spring.transactions.jpa.service;

import java.util.function.Consumer;

import edu.jmaycon.spring.transactions.jpa.entity.BankAccountEntity;
import edu.jmaycon.spring.transactions.jpa.repository.BankAccountRepository;

public interface AccountService {
	
	public enum Repository{
		BANK_ACCOUNT_REPOSITORY
		, EMPTY_REPOSITORY;
	}

	void insert(BankAccountEntity bc1);


	void deleteAll();
	
	void executeTransactionalDefault(Consumer<BankAccountRepository> consumer);
	
	void executeTransactionalReadOnly(Consumer<BankAccountRepository> consumer);

	void executeTransactionNever(Consumer<BankAccountRepository> consumer);

	void executeTransactionNotSupported(Consumer<BankAccountRepository> consumer);
}
