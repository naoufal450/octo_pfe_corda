package com.octo.contracts;

import com.google.common.collect.ImmutableList;
import com.r3.corda.lib.tokens.contracts.FungibleTokenContract;
import org.junit.Test;

import static net.corda.testing.node.NodeTestUtils.ledger;

public class DenyOrCancelPledgeContractTests extends BaseObligationContractTests {

    @Test
    public void denyPledgeShouldHaveOneInputPledgeRequest() {
        ledger(ledgerServices, (ledger -> {
            ledger.transaction(tx -> {

                tx.command(ImmutableList.of(bankA.getPublicKey(), centralBank.getPublicKey()),
                        new DDRObligationContract.DDRObligationCommands.DenyDDRPledge());

                tx.tweak(tw2 -> {
                    tw2.input(DDRObligationContract.ID, examplePledgeApproved);
                    return tw2.failsWith("Input DDRObligationState should have status REQUEST");
                });

                tx.tweak(tw2 -> {
                    tw2.input(DDRObligationContract.ID, exampleRedeemRequest);
                    return tw2.failsWith("Input DDRObligationState should have type PLEDGE");
                });

                tx.input(DDRObligationContract.ID, examplePledgeRequest);
                tx.verifies();

                tx.input(FungibleTokenContract.Companion.getContractId(), exampleDDRObject);

                return tx.failsWith("Only 1 input of type DDRObligationState should be consumed when denying or canceling DDR Pledge");
            });
            return null;
        }));
    }

    @Test
    public void cancelPledgeShouldHaveOneInputPledgeRequest() {
        ledger(ledgerServices, (ledger -> {
            ledger.transaction(tx -> {

                tx.command(ImmutableList.of(bankA.getPublicKey(), centralBank.getPublicKey()),
                        new DDRObligationContract.DDRObligationCommands.CancelDDRPledge());

                tx.tweak(tw2 -> {
                    tw2.input(DDRObligationContract.ID, examplePledgeApproved);
                    return tw2.failsWith("Input DDRObligationState should have status REQUEST");
                });

                tx.tweak(tw2 -> {
                    tw2.input(DDRObligationContract.ID, exampleRedeemRequest);
                    return tw2.failsWith("Input DDRObligationState should have type PLEDGE");
                });

                tx.input(DDRObligationContract.ID, examplePledgeRequest);
                tx.verifies();

                tx.input(FungibleTokenContract.Companion.getContractId(), exampleDDRObject);

                return tx.failsWith("Only 1 input of type DDRObligationState should be consumed when denying or canceling DDR Pledge");
            });
            return null;
        }));
    }

    @Test
    public void denyPledgeShouldHaveNoOutputs() {
        ledger(ledgerServices, (ledger -> {
            ledger.transaction(tx -> {

                tx.input(DDRObligationContract.ID, examplePledgeRequest);
                tx.command(ImmutableList.of(bankA.getPublicKey(), centralBank.getPublicKey()),
                        new DDRObligationContract.DDRObligationCommands.DenyDDRPledge());
                tx.verifies();
                tx.output(DDRObligationContract.ID, examplePledgeRequest);
                return tx.failsWith("No outputs must be created when denying or canceling DDR Pledge");

            });
            return null;
        }));
    }

    @Test
    public void cancelPledgeShouldHaveNoOutputs() {
        ledger(ledgerServices, (ledger -> {
            ledger.transaction(tx -> {

                tx.input(DDRObligationContract.ID, examplePledgeRequest);
                tx.command(ImmutableList.of(bankA.getPublicKey(), centralBank.getPublicKey()),
                        new DDRObligationContract.DDRObligationCommands.CancelDDRPledge());
                tx.verifies();
                tx.output(DDRObligationContract.ID, examplePledgeRequest);
                return tx.failsWith("No outputs must be created when denying or canceling DDR Pledge");

            });
            return null;
        }));
    }

}
