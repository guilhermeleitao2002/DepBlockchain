package depchain.blockchain;

import java.util.*;
import java.util.concurrent.*;

import depchain.consensus.ConsensusInstance;
import depchain.consensus.State;
import depchain.consensus.TimestampValuePair;
import depchain.network.Message;
import depchain.network.PerfectLink;
import depchain.network.Message.Type;
import depchain.utils.Config;

import java.net.InetSocketAddress;
import io.github.cdimascio.dotenv.Dotenv;
import depchain.utils.Logger;
import depchain.utils.Logger.LogLevel;

public class BlockchainMember {
    private final int memberId;
    private final int memberPort;
    private final int leaderId; // Static leader ID.
    private final List<Integer> allProcessIds;
    private PerfectLink perfectLink;
    /* private final ConcurrentMap<Integer, ConsensusInstance> consensusInstances = new ConcurrentHashMap<>(); */
    private ConsensusInstance consensusInstance;
    private final int f; // Maximum number of Byzantine faults allowed.
    private int epochNumber = 0;
    private ArrayList<String> blockchain = new ArrayList<>();

    public BlockchainMember(int memberId, int memberPort, int leaderId, int f) {
        this.memberId = memberId;
        this.memberPort = memberPort;
        this.leaderId = leaderId;
        this.allProcessIds = Arrays.asList(1, 2, 3, 4);
        this.f = f;

        Dotenv dotenv = Dotenv.load();
        // Load configuration from config.txt and resources folder.
        String configFilePath = dotenv.get("CONFIG_FILE_PATH");
        String keysFolderPath = dotenv.get("KEYS_FOLDER_PATH");

        if (configFilePath == null || keysFolderPath == null) {
            Logger.log(LogLevel.ERROR, "Environment variables CONFIG_FILE_PATH or KEYS_FOLDER_PATH are not set.");
            return;
        }

        try {
            Config.loadConfiguration(configFilePath, keysFolderPath);
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "Failed to load configuration: " + e.getMessage());
            return;
        }

        PerfectLink pl;
        try {
            pl = new PerfectLink(memberId, memberPort, Config.processAddresses, Config.getPrivateKey(memberId),
                    Config.publicKeys);
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "Failed to create PerfectLink: " + e.getMessage());
            return;
        }
        this.perfectLink = pl;


        startMessageHandler();
    }

    public static void main(String[] args) throws Exception {
        // Usage: java BlockchainMember <memberId> <memberPort>
        if (args.length < 2) {
            Logger.log(LogLevel.ERROR, "Usage: BlockchainMember <processId> <port>");
            return;
        }

        int memberId = Integer.parseInt(args[0]);
        int memberPort = Integer.parseInt(args[1]);
        int leaderId = 1; // assume process 1 is leader
                
        // Assume maximum Byzantine faults f = 1 for 4 processes.
        BlockchainMember blockchainMember = new BlockchainMember(memberId, memberPort, leaderId, 1);
        Logger.log(LogLevel.INFO, "BlockchainMember " + memberId + " started on port " + memberPort);
    }

    // Message handler loop.
    private void startMessageHandler() {
        while (true) {
            try {
                Message msg = perfectLink.deliver(); // waits for new elements to be added to the linked blocking
                                                        // queue
                // If a CLIENT_REQUEST is received and this node is leader,
                // then start a consensus instance for the client request.
                new Thread(() -> {
                    if (msg.type == Message.Type.CLIENT_REQUEST) {
                        if (memberId == leaderId) {
                            int instanceId = epochNumber++;
                            consensusInstance = new ConsensusInstance(memberId, leaderId, allProcessIds, perfectLink,
                                    instanceId, f);
                            consensusInstance.setBlockchainMostRecentWrite(new TimestampValuePair(0, msg.value));
                                /* consensusInstance.readPhase(msg.value); */
                            try {
                                Logger.log(LogLevel.DEBUG, "Waiting for decision...");

                                String decidedValue = consensusInstance.decide();
                                
                                // String decidedValue = msg.value;
                                Logger.log(LogLevel.DEBUG, "Decided value: " + decidedValue);
                                
                                // Append the decided value to the blockchain.
                                this.blockchain.add(decidedValue);  

                                // Send ACK to the client.
                                InetSocketAddress clientAddr = Config.clientAddresses.get(msg.senderId);
                                if (clientAddr != null) {
                                    // TODO: EPOCH NUMBER MUST BE A NEW ONE
                                    Message reply = new Message(Type.CLIENT_REPLY, msg.epoch, decidedValue, memberId, null,
                                            msg.nonce);
                                    perfectLink.send(msg.senderId, reply);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        try {
                            // For consensus messages, dispatch to the corresponding consensus instance.
                            if (consensusInstance != null) {
                                consensusInstance.processMessage(msg);

                            } else {
                                // instantiate a new consensus instance
                                consensusInstance = new ConsensusInstance(memberId, leaderId, allProcessIds, perfectLink,
                                        msg.epoch, f);
                                        consensusInstance.processMessage(msg);
                            }

                            String decidedValue = consensusInstance.getDecidedValue();
                            if (decidedValue != null) {
                                // Append the decided value to the blockchain.
                                this.blockchain.add(decidedValue);
                                consensusInstance = null;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<String> getBlockchain() {
        return this.blockchain;
    }

    public int getMemberId() {
        return this.memberId;
    }
}