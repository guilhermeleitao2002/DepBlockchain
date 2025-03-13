package depchain.network;

import java.io.Serializable;

import depchain.utils.ByteArrayWrapper;
import depchain.consensus.State;
import depchain.consensus.TimestampValuePair;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class Message implements Serializable {
    public enum Type {
        READ, STATE, COLLECTED, WRITE, ACCEPT, CLIENT_REQUEST, CLIENT_REPLY, ACK, START_SESSION, ACK_SESSION
    }

    public final Type type;
    public final int epoch; // For our design, epoch doubles as the consensus instance ID.
    public String value; // The value (e.g., the string to append).
    public final int senderId; // The sender's ID (for clients, use a distinct range).
    public final byte[] signature; // Signature over the message content (computed by sender).
    public int nonce; // nonce for the message (computed by sender).
    public ByteArrayWrapper sessionKey; // session key for the message (computed by sender).
    public final State state;
    public final Map<Integer, State> statesMap;
    public final TimestampValuePair write;

    public Message(Type type, int epoch, String value, int senderId, byte[] signature, int nonce) {
        this(type, epoch, value, senderId, signature, nonce, null, null, null, null);
    }

    public Message(Type type, int epoch, String value, int senderId, byte[] signature, int nonce,
            ByteArrayWrapper sessionKey) {
        this(type, epoch, value, senderId, signature, nonce, sessionKey, null, null, null);
    }

    // For STATE messages
    public Message(Type type, int epoch, String value, int senderId, byte[] signature, int nonce,
            ByteArrayWrapper sessionKey, State state) {
        this(type, epoch, value, senderId, signature, nonce, sessionKey, state, null, null);
    }

    // for Collected messages
    public Message(Type type, int epoch, String value, int senderId, byte[] signature, int nonce,
            ByteArrayWrapper sessionKey, State state, Map<Integer, State> statesMap) {
        this(type, epoch, value, senderId, signature, nonce, sessionKey, state, statesMap, null);
    }

    // for WRITE messages
    public Message(Type type, int epoch, String value, int senderId, byte[] signature, int nonce,
            ByteArrayWrapper sessionKey, State state, Map<Integer, State> statesMap, TimestampValuePair write) {
        this.type = type;
        this.epoch = epoch;
        this.value = value;
        this.senderId = senderId;
        this.signature = signature;
        this.nonce = nonce;
        this.sessionKey = sessionKey;
        this.state = state;
        this.statesMap = statesMap;
        this.write = write;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    // Returns a string representation of the content to be signed.
    public String getSignableContent() {

        // only put non null values
        String content = "";
        content += type.toString();
        content += epoch;
        content += value;
        content += senderId;
        content += nonce;
        if (sessionKey != null) {
            content += sessionKey.getData();
        }
        // if (state != null) {
        // for (Object obj : state) {
        // content += obj.toString();
        // }
        // }
        return content;
    }
}
