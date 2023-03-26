

public class GameSessionTurn {
    int timestamp;
    int gameSessionID;
    int playerID;
    String action;
    String dealerHand;
    String playerHand;

    public GameSessionTurn(int timestamp, int gameSessionID, int playerID, String action, String dealerHand, String playerHand){
        this.timestamp = timestamp;
        this.gameSessionID = gameSessionID;
        this.playerID =  playerID;
        this.action = action;
        this.dealerHand = dealerHand;
        this.playerHand = playerHand;
    }

    @Override
    public String toString() {
        return timestamp + "," + gameSessionID + "," + playerID + "," + action + "," + dealerHand + "," + playerHand;
    }

    public int getGameSessionID() {
        return gameSessionID;
    }

    public int getTimestamp() {
        return timestamp;
    }

}
