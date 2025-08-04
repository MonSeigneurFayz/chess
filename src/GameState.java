public class GameState {
    private boolean isWhiteTurn;
    private int[] lastDoubleStepPawn = null; // format [row, col]
    public GameState() {
        this.isWhiteTurn = true; // commence par les blancs
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public void switchTurn() {
        isWhiteTurn = !isWhiteTurn;
    }

    public String getCurrentPlayerColor() {
        return isWhiteTurn ? "white" : "black";
    }

    public void setLastDoubleStepPawn(int row, int col) {
        lastDoubleStepPawn = new int[] { row, col };
    }

    public int[] getLastDoubleStepPawn() {
        return lastDoubleStepPawn;
    }

    public void resetLastDoubleStepPawn() {
        lastDoubleStepPawn = null;
    }
    
    public boolean isLastDoubleStepPawn(int row, int col) {
        return lastDoubleStepPawn != null &&
               lastDoubleStepPawn[0] == row &&
               lastDoubleStepPawn[1] == col;
    }
    
}
