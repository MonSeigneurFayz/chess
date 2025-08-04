import javax.swing.JButton;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class Choice extends JButton {

    private Color couleurRond = Color.GRAY;

    public Choice() {
        setContentAreaFilled(false);  // Ne pas remplir le fond standard
        setFocusPainted(false);       // Ne pas afficher la bordure focus
        setBorderPainted(false);      // Pas de bordure
    }

    public void setCouleurRond(Color c) {
        this.couleurRond = c;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Appel pour garder le comportement par défaut

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON
        );

        int diameter = Math.min(getWidth(), getHeight()) - 80; // marges
        int x = (getWidth() - diameter) / 2;
        int y = (getHeight() - diameter) / 2;

        g2.setColor(couleurRond);
        g2.fillOval(x, y, diameter, diameter); // Dessin du rond
    }
}
