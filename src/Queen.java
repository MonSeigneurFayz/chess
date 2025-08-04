import javax.swing.*;
import java.awt.*;

public class Queen extends JButton implements ColoredPiece {
    private String color;
    private int row;
    private int column;
    private int value;
    private boolean hasMoved = false;

    public Queen(String color, int row, int column) {
        this.color = color;
        this.row = row;
        this.column = column;
        this.value = 8;

        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setEnabled(true);
        setBounds((column - 1) * 100, row * 100, 100, 100);

        // Charger l'image de la reine
        ImageIcon icon;
        if (color.equals("white")) {
            icon = new ImageIcon(getClass().getResource("whiteQueen.png"));
        } else {
            icon = new ImageIcon(getClass().getResource("/blackQueen.png"));
        }

        // Redimensionner l’image
        Image image = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        setIcon(new ImageIcon(image));
    }

    @Override
    public String getColor() {
        return color;
    }

    @Override
    public String getType() {
        return "Queen";
    }

    @Override
    public boolean hasMoved() {
        return hasMoved;
    }

    @Override
    public void sethasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }
    
    public int getValue() {
    	return value;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setColumn(int column) {
        this.column = column;
    }
}
