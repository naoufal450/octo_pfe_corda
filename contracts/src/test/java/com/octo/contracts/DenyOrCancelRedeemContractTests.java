package com.octo.contracts;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static net.corda.testing.node.NodeTestUtils.ledger;

public class DenyOrCancelRedeemContractTests extends BaseObligationContractTests {

    @Test
    public void denyRedeemShouldHaveOneInputRedeemRequest() {
        ledger(ledgerServices, (ledger -> {
            ledger.transaction(tx -> {

                tx.command(ImmutableList.of(bankA.getPublicKey(), centralBank.getPublicKey()),
                        new DDRObligationContract.DDRObligationCommands.DenyDDRRedeem());

                tx.output(DDRObligationContract.ID, exampleRedeemRejected);

                tx.tweak(tw2 -> {
                    tw2.input(DDRObligationContract.ID, exampleRedeemApproved);
                    return tw2.failsWith("Input DDRObligationState should have status REQUEST");
                });

                tx.tweak(tw2 -> {
                    tw2.input(DDRObligationContract.ID, examplePledgeRequest);
                    return tw2.failsWith("Input DDRObligationState should have type REDEEM");
                });

                tx.input(DDRObligationContract.ID, exampleRedeemRequest);
                tx.verifies();

                tx.input(DDRObjectContract.ID, exampleDDRObject);

                return tx.failsWith("Only 1 input of type DDRObligationState should be consumed when denying or canceling DDR Redeem");
            });
            return null;
        }));
    }

    @Test
    public void cancelRedeemShouldHaveOneInputRedeemRequest() {
        ledger(ledgerServices, (ledger -> {
            ledger.transaction(tx -> {

                tx.command(ImmutableList.of(bankA.getPublicKey(), centralBank.getPublicKey()),
                        new DDRObligationContract.DDRObligationCommands.CancelDDRRedeem());

                tx.output(DDRObligationContract.ID, exampleRedeemCanceled);

                tx.tweak(tw2 -> {
                    tw2.input(DDRObligationContract.ID, exampleRedeemApproved);
                    return tw2.failsWith("Input DDRObligationState should have status REQUEST");
                });

                tx.tweak(tw2 -> {
                    tw2.input(DDRObligationContract.ID, examplePledgeRequest);
                    return tw2.failsWith("Input DDRObligationState should have type REDEEM");
                });

                tx.input(DDRObligationContract.ID, exampleRedeemRequest);
                tx.verifies();

                tx.input(DDRObjectContract.ID, exampleDDRObject);

                return tx.failsWith("Only 1 input of type DDRObligationState should be consumed when denying or canceling DDR Redeem");
            });
            return null;
        }));
    }

    @Test
    public void denyRedeemShouldHaveNoOutputs() {
        ledger(ledgerServices, (ledger -> {
            ledger.transaction(tx -> {

                tx.input(DDRObligationContract.ID, exampleRedeemRequest);
                tx.command(ImmutableList.of(bankA.getPublicKey(), centralBank.getPublicKey()),
                        new DDRObligationContract.DDRObligationCommands.DenyDDRRedeem());

                tx.failsWith("1 output of type DDRObligationState must be created when denying or canceling DDR Redeem");

                tx.tweak(tw -> {
                    tw.output(DDRObligationContract.ID, exampleRedeemCanceled);
                    return tw.failsWith("Output DDRObligationState should have status REJECTED");
                });

                tx.output(DDRObligationContract.ID, exampleRedeemRejected);
                return tx.verifies();
            });
            return null;
        }));
    }

    @Test
    public void cancelRedeemShouldHaveNoOutputs() {
        ledger(ledgerServices, (ledger -> {
            ledger.transaction(tx -> {

                tx.input(DDRObligationContract.ID, exampleRedeemRequest);
                tx.command(ImmutableList.of(bankA.getPublicKey(), centralBank.getPublicKey()),
                        new DDRObligationContract.DDRObligationCommands.CancelDDRRedeem());

                tx.failsWith("1 output of type DDRObligationState must be created when denying or canceling DDR Redeem");

                tx.tweak(tw -> {
                    tw.output(DDRObligationContract.ID, exampleRedeemRejected);
                    return tw.failsWith("Output DDRObligationState should have status CANCELED");
                });

                tx.output(DDRObligationContract.ID, exampleRedeemCanceled);
                return tx.verifies();
            });
            return null;
        }));
    }

}
