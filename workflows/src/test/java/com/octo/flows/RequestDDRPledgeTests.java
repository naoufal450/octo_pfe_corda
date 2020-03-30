package com.octo.flows;

import com.google.common.collect.ImmutableList;
import com.octo.enums.DDRObligationStatus;
import com.octo.enums.DDRObligationType;
import com.octo.states.DDRObligationState;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.Amount;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.TransactionState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;

public class RequestDDRPledgeTests {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private final MockNetwork network = new MockNetwork(new MockNetworkParameters(ImmutableList.of(
            TestCordapp.findCordapp("com.octo.contracts"),
            TestCordapp.findCordapp("com.octo.flows")
    )));
    private final StartedMockNode a = network.createNode(CordaX500Name.parse("O=BankA,L=New York,C=US"));
    private final StartedMockNode bc = network.createNode(CordaX500Name.parse("O=CentralBank,L=New York,C=US"));


    public RequestDDRPledgeTests() {
        bc.registerInitiatedFlow(RequestDDRPledge.Responder.class);
    }

    @Before
    public void setup() {
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    @Test
    public void flowRecordsATransactionInBothPartiesTransactionStorages() throws ExecutionException, InterruptedException {
        RequestDDRPledge.Initiator flow = new RequestDDRPledge.Initiator(
                new Amount<Currency>(1000, Currency.getInstance("MAD")), new Date(new Date().getTime() - 86400000));

        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTx = future.get();
        for (StartedMockNode node : ImmutableList.of(a, bc)) {
            assertEquals(signedTx, node.getServices().getValidatedTransactions().getTransaction(signedTx.getId()));
        }
    }

    @Test
    public void negativeAmount_ThrowsException() throws Exception {
        exception.expect(instanceOf(IllegalArgumentException.class));
        RequestDDRPledge.Initiator flow = new RequestDDRPledge.Initiator(
                new Amount<Currency>(-1, Currency.getInstance("MAD")), new Date());
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();
        future.get();
    }

    @Test
    public void signedTransactionReturnedByTheFlowIsSignedByTheInitiator() throws Exception {
        RequestDDRPledge.Initiator flow = new RequestDDRPledge.Initiator(
                new Amount<Currency>(1, Currency.getInstance("MAD")), new Date());
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();

        SignedTransaction signedTx = future.get();
        signedTx.verifySignaturesExcept(bc.getInfo().getLegalIdentities().get(0).getOwningKey());
    }

    @Test
    public void signedTransactionReturnedByTheFlowIsSignedByTheAcceptor() throws Exception {
        RequestDDRPledge.Initiator flow = new RequestDDRPledge.Initiator(
                new Amount<Currency>(1, Currency.getInstance("MAD")), new Date());
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();

        SignedTransaction signedTx = future.get();
        signedTx.verifySignaturesExcept(a.getInfo().getLegalIdentities().get(0).getOwningKey());
    }

    @Test
    public void recordedTransactionHasNoInputsAndASingleOutput() throws Exception {
        Amount<Currency> amount = new Amount<Currency>(1, Currency.getInstance("MAD"));
        Date requesterDate = new Date();
        RequestDDRPledge.Initiator flow = new RequestDDRPledge.Initiator(amount, requesterDate);
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTx = future.get();

        // We check the recorded transaction in both vaults.
        for (StartedMockNode node : ImmutableList.of(a, bc)) {
            SignedTransaction recordedTx = node.getServices().getValidatedTransactions().getTransaction(signedTx.getId());
            List<TransactionState<ContractState>> txOutputs = recordedTx.getTx().getOutputs();
            assertEquals(1, txOutputs.size());
            assertEquals(0, recordedTx.getTx().getInputs().size());

            DDRObligationState recordedState = (DDRObligationState) txOutputs.get(0).getData();
            assertEquals(amount, recordedState.getAmount());
            assertEquals(a.getInfo().getLegalIdentities().get(0), recordedState.getOwner());
            assertEquals(a.getInfo().getLegalIdentities().get(0), recordedState.getRequester());
            assertEquals(bc.getInfo().getLegalIdentities().get(0), recordedState.getIssuer());
            assertEquals(requesterDate, recordedState.getRequesterDate());
            assertEquals(DDRObligationType.PLEDGE, recordedState.getType());
            assertEquals(DDRObligationStatus.REQUEST, recordedState.getStatus());
        }
    }

    @Test
    public void flowRecordsTheCorrectDDRObligationInBothPartiesVaults() throws Exception {
        Amount<Currency> amount = new Amount<Currency>(1, Currency.getInstance("MAD"));
        Date requesterDate = new Date();
        RequestDDRPledge.Initiator flow = new RequestDDRPledge.Initiator(amount, requesterDate);
        CordaFuture<SignedTransaction> future = a.startFlow(flow);
        network.runNetwork();
        future.get();

        // We check the recorded DDR obligation in both vaults.
        for (StartedMockNode node : ImmutableList.of(a, bc)) {
            node.transaction(() -> {
                List<StateAndRef<DDRObligationState>> ddrObligations = node.getServices().getVaultService().queryBy(DDRObligationState.class).getStates();
                assertEquals(1, ddrObligations.size());
                DDRObligationState recordedState = ddrObligations.get(0).getState().getData();
                assertEquals(amount, recordedState.getAmount());
                assertEquals(a.getInfo().getLegalIdentities().get(0), recordedState.getOwner());
                assertEquals(a.getInfo().getLegalIdentities().get(0), recordedState.getRequester());
                assertEquals(requesterDate, recordedState.getRequesterDate());
                assertEquals(DDRObligationType.PLEDGE, recordedState.getType());
                assertEquals(DDRObligationStatus.REQUEST, recordedState.getStatus());
                return null;
            });
        }
    }
}