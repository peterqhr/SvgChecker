import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

import java.awt.Dimension;
import java.awt.Graphics;
import java.io.IOException;
import java.io.StringReader;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class SvgChecker {

    public static void main(String[] args) throws IOException {
        String svgString = JOptionPane.showInputDialog( null, "Enter SVG string:", "SVG Input", JOptionPane.PLAIN_MESSAGE );

        if (svgString == null || svgString.trim().isEmpty()) {
            System.out.println("No SVG string provided.");
            return;
        }

        StringReader reader = new StringReader( svgString );
        String uri = "file:test.svg";
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory f = new SAXSVGDocumentFactory( parser );
        SVGDocument doc = f.createSVGDocument( uri, reader );

        Icon icon = new SvgIcon( doc, 48, 48 );

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent( Graphics g ) {
                super.paintComponent(g);
                int x = (getWidth() - icon.getIconWidth()) / 2;
                int y = (getHeight() - icon.getIconHeight()) / 2;
                icon.paintIcon(this, g, x, y);
            }
        };
        panel.setPreferredSize(new Dimension( icon.getIconWidth(), icon.getIconHeight()) );

        JFrame frame = new JFrame("SVG Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);

    }
}
