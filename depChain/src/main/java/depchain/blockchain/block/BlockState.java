package depchain.blockchain.block;

import java.util.Map;


public class BlockState {
  private Map<String, Long> balances;


  public BlockState(Map<String, Long> balances) {
    this.balances = balances;
  }

  // Getters and setters
  public Map<String, Long> getBalances() {
    return balances;
  }

  public void setBalances(Map<String, Long> balances) {
    this.balances = balances;
  }

  @Override
  public String toString() {
    String str = "State{";
    for (Map.Entry<String, Long> entry : balances.entrySet()) {
      str += " " + entry.getKey() + ": " + entry.getValue() + ",";
    }
    str = str.substring(0, str.length() - 1); // Remove the last comma
    str += " }";
    return str;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof BlockState) {
      BlockState other = (BlockState) obj;
      return balances.equals(other.balances);
    }
    return false;
  }

}