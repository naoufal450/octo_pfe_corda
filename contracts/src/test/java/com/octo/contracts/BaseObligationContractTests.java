package com.octo.contracts;

import com.octo.builders.DDRObjectStateBuilder;
import com.octo.builders.DDRObligationStateBuilder;
import com.octo.enums.DDRObligationStatus;
import com.octo.enums.DDRObligationType;
import com.octo.states.DDRObjectState;
import com.octo.states.DDRObligationState;
import net.corda.core.contracts.Amount;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;

import java.util.Currency;
import java.util.Date;

public class BaseObligationContractTests {

    static protected final Date exampleDate = new Date();
    static protected final long exampleAmount = 1000;
    static protected final Currency exampleCurrency = Currency.getInstance("MAD");
    static protected TestIdentity bankA = new TestIdentity(new CordaX500Name("BankA", "London", "GB"));
    static protected TestIdentity bankB = new TestIdentity(new CordaX500Name("BankB", "London", "GB"));
    static protected TestIdentity centralBank = new TestIdentity(new CordaX500Name("CentralBank", "New York", "US"));
    static protected final DDRObligationState examplePledgeRequest = new DDRObligationState(centralBank.getParty(), bankA.getParty(), exampleDate,
            new Amount<>(exampleAmount, exampleCurrency), bankA.getParty(), DDRObligationType.PLEDGE, DDRObligationStatus.REQUEST, "externalId");
    static protected final DDRObligationState examplePledgeApproved = new DDRObligationStateBuilder(examplePledgeRequest).status(DDRObligationStatus.APPROVED).build();
    static protected final DDRObligationState examplePledgeCanceled = new DDRObligationStateBuilder(examplePledgeRequest).status(DDRObligationStatus.CANCELED).build();
    static protected final DDRObligationState examplePledgeRejected = new DDRObligationStateBuilder(examplePledgeRequest).status(DDRObligationStatus.REJECTED).build();
    static protected final DDRObligationState exampleRedeemRequest = new DDRObligationStateBuilder(examplePledgeRequest).type(DDRObligationType.REDEEM).build();
    static protected final DDRObligationState exampleRedeemApproved = new DDRObligationStateBuilder(exampleRedeemRequest).status(DDRObligationStatus.APPROVED).build();
    static protected final DDRObligationState exampleRedeemCanceled = new DDRObligationStateBuilder(exampleRedeemRequest).status(DDRObligationStatus.CANCELED).build();
    static protected final DDRObligationState exampleRedeemRejected = new DDRObligationStateBuilder(exampleRedeemRequest).status(DDRObligationStatus.REJECTED).build();
    static protected final DDRObjectState exampleDDRObject = new DDRObjectStateBuilder().amount(exampleAmount).currency(exampleCurrency)
            .owner(bankA.getParty()).issuer(centralBank.getParty()).issuerDate(exampleDate).build();
    static protected final MockServices ledgerServices = new MockServices();
}
