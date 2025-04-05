package depchain.client.commands;

import depchain.client.DepChainClient;
import depchain.utils.Logger;
import depchain.utils.Logger.LogLevel;
import depchain.library.ClientLibrary;

public class GetDepBalanceCommand implements Command {

    private final DepChainClient client;

    public GetDepBalanceCommand(DepChainClient client) {
        this.client = client;
    }

    @Override
    public void execute(String[] args, ClientLibrary clientLib) {
        if (args.length != 0 || !args[0].matches("\\d+") || !args[1].matches("\\d+")) {
            Logger.log(LogLevel.ERROR, "Usage: getDepBal");
            return;
        }

        int recipientId = Integer.parseInt(args[0]);
        long amount = Long.parseLong(args[1]);

        Logger.log(LogLevel.INFO, "Client sending get DepCoin balance request...");
        try {
            // TODO: Implement the getDepBalance method in ClientLibrary
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "Failed to get DepCoin balance: " + e.getMessage());
        }
    }
}