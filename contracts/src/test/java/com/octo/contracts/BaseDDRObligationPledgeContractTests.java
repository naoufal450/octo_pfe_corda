package com.octo.contracts;

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

public class BaseDDRObligationPledgeContractTests {

    static protected final Date exampleDate = new Date();
    static protected final Amount<Currency> exampleAmount = new Amount<Currency>(1000, Currency.getInstance("MAD"));
    static protected TestIdentity bankA = new TestIdentity(new CordaX500Name("BankA", "London", "GB"));
    static protected TestIdentity bankB = new TestIdentity(new CordaX500Name("BankB", "London", "GB"));
    static protected TestIdentity centralBank = new TestIdentity(new CordaX500Name("CentralBank", "New York", "US"));
    static protected final DDRObligationState examplePledgeRequest = new DDRObligationState(centralBank.getParty(), bankA.getParty(), exampleDate, exampleAmount
            , bankA.getParty(), DDRObligationType.PLEDGE, DDRObligationStatus.REQUEST, "externalId");
    static protected final DDRObjectState exampleDDRObject = new DDRObjectState(centralBank.getParty(), exampleDate,
            exampleAmount, exampleAmount.getToken(), bankA.getParty());
    protected final MockServices ledgerServices = new MockServices();

}