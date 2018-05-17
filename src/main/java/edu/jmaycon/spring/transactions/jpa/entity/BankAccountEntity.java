package edu.jmaycon.spring.transactions.jpa.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "bank_account")
public class BankAccountEntity {

	@Id
	@Column(name = "account")
	private String account;
	
	@Column(name =  "balance")
	private BigDecimal balance;
	
	public BankAccountEntity() {
		super();
	}

	public BankAccountEntity(String account, BigDecimal balance) {
		super();
		this.account = account;
		this.balance = balance;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof BankAccountEntity))
			return false;
		BankAccountEntity other = (BankAccountEntity) obj;
		if (account == null) {
			if (other.account != null)
				return false;
		} else if (!account.equals(other.account))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BankAccountEntity [account=" + account + ", balance=" + balance + "]";
	}

	public BigDecimal withDraw() {
		return withDraw(this.balance);
	}
	
	public BigDecimal withDraw(BigDecimal amount) {
		this.balance = this.balance.subtract(amount);
		return amount;
	}

	public void deposit(BigDecimal amount) {
		this.balance = this.balance.add(amount);
	}
}
