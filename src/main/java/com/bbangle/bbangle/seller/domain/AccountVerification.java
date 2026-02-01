package com.bbangle.bbangle.seller.domain;

import com.bbangle.bbangle.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "account_verifications")
@Entity
public class AccountVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bank_code", columnDefinition = "VARCHAR(10)")
    private String bankCode;

    @Column(name = "account_number", columnDefinition = "VARBINARY(255)")
    private String accountNumber;

    @Column(name = "account_holder", columnDefinition = "VARCHAR(50)")
    private String accountHolder;

    @Column(name = "verified")
    private boolean verified;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;

    /**
     * Creates a new AccountVerification with the specified bank code, encrypted account number, account holder, verification status, and associated seller.
     *
     * @param bankCode the bank code (stored as VARCHAR(10))
     * @param encryptedAccountNumber the account number already encrypted for storage (stored as VARBINARY(255))
     * @param accountHolder the account holder's name
     * @param verified whether the account has been verified
     * @param seller the associated Seller entity
     */
    private AccountVerification(String bankCode, String encryptedAccountNumber, String accountHolder,
                                boolean verified, Seller seller) {
        this.bankCode = bankCode;
        this.accountNumber = encryptedAccountNumber;
        this.accountHolder = accountHolder;
        this.verified = verified;
        this.seller = seller;
    }

    /**
     * Create a new AccountVerification with the provided bank code, encrypted account number, account holder, verification status, and associated seller.
     *
     * @param bankCode the bank code (stored as VARCHAR(10))
     * @param encryptedAccountNumber the account number in encrypted form (stored as VARBINARY)
     * @param accountHolder the account holder's name
     * @param verified the verification status
     * @param seller the Seller associated with this account verification
     * @return a new AccountVerification initialized with the given values
     */
    public static AccountVerification create(String bankCode, String encryptedAccountNumber,
                                             String accountHolder, boolean verified, Seller seller) {
        return new AccountVerification(bankCode, encryptedAccountNumber, accountHolder, verified, seller);
    }
}