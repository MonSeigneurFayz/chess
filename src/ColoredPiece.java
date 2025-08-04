import java.awt.Rectangle;

public interface ColoredPiece {
	String getColor();
	void setBounds(int x, int y, int width, int height);
    Rectangle getBounds();

    String getType(); // Par exemple : "Roi", "Reine", "Fou", etc.

    boolean hasMoved(); // utile pour roque ou pion
    void sethasMoved(boolean hasMoved);
}
