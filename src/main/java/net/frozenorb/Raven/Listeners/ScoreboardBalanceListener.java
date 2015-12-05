package net.frozenorb.Raven.Listeners;

import net.frozenorb.mBasic.EconomySystem.UUID.UUIDEconomyListener;

import java.util.UUID;

public class ScoreboardBalanceListener implements UUIDEconomyListener {

	@Override
	public void onDeposit(UUID name, double deposited) {}

	@Override
	public void onWithdraw(UUID name, double withdrawn) {}

	@Override
	public void onBalanceChanged(UUID name, double newBalance) {

	}
}
