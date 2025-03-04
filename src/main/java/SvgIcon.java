import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.w3c.dom.Document;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.GrayFilter;
import javax.swing.Icon;

public class SvgIcon implements Icon {
    private Document document;

    /**
     * The width of the rendered image.
     */
    private final int width;

    /**
     * The height of the rendered image.
     */
    private final int height;
    private BufferedImage cachedImage;

    /**
     * Create a new SvgIcon object.
     *
     * @param doc The SVG document.
     * @param w   The width of the icon.
     * @param h   The height of the icon.
     */
    protected SvgIcon( Document doc, int w, int h ) {
        this.document = doc;
        this.width = w;
        this.height = h;
    }

    /**
     * Returns the icon's width.
     */
    @Override
    public int getIconWidth() {
        return width;
    }

    /**
     * Returns the icon's height.
     */
    @Override
    public int getIconHeight() {
        return height;
    }

    /**
     * Draw the icon at the specified location, using the component's
     * graphics.
     */
    public void paintIcon( Component c, int x, int y ) {
        paintIcon( c, c.getGraphics(), x, y );
    }

    /**
     * Draw the icon at the specified location.
     */
    @Override
    public void paintIcon( Component c, Graphics g, int x, int y ) {

        if ( this.cachedImage == null ) {
            SVGUserAgentAdapter userAgentAdapter = new SVGUserAgentAdapter();

            cachedImage = userAgentAdapter.makeBufferedImage( c );
            // Release any memory held by document.
            document = null;
        }
        if ( c == null || c.isEnabled() ) {
            g.drawImage( cachedImage, x, y, null );
        } else {
            g.drawImage( GrayFilter.createDisabledImage( cachedImage ), x, y, null );
        }
    }

    private class SVGUserAgentAdapter extends UserAgentAdapter {

        private GVTBuilder builder;

        public SVGUserAgentAdapter() {
            prepareBridgeContext();
        }

        private void prepareBridgeContext() {
            DocumentLoader loader = new DocumentLoader( this );
            ctx = new BridgeContext( this, loader );
            ctx.setDynamicState( BridgeContext.DYNAMIC );
            builder = new GVTBuilder();
        }

        // UserAgent /////////////////////////////////////////////////////////////

        /**
         * Returns the default size of this user agent.
         */
        @Override
        public Dimension2D getViewportSize() {
            return new Dimension( width, height );
        }

        BufferedImage makeBufferedImage( Component c ) {
            GraphicsNode node = builder.build( ctx, document );

            Dimension2D bounds = ctx.getDocumentSize();
            double widthOffset = bounds.getWidth();
            double heightOffset = bounds.getHeight();

            double scaleX = getIconWidth() / widthOffset;
            double scaleY = getIconHeight() / heightOffset;

            double scale = Math.min( scaleX, scaleY );

            AffineTransform newTransform = new AffineTransform( scale, 0.0, 0.0, scale, 0, 0 );
            Map<RenderingHints.Key, Object> hints = new HashMap();
            hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
            hints.put( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
            node.setRenderingHints( hints );
            BufferedImage translucent;
            if ( c == null ) {
                translucent = new BufferedImage( getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB );
            } else {
                GraphicsConfiguration config = c.getGraphicsConfiguration();
                if ( config == null ) {
                    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    for ( GraphicsDevice gd : ge.getScreenDevices() ) {
                        if ( gd.getType() == GraphicsDevice.TYPE_RASTER_SCREEN ) {
                            config = gd.getDefaultConfiguration();
                            break;
                        }
                    }
                    if ( config == null ) {
                        throw new NullPointerException( "GraphicsConfiguration Is Null." );
                    }
                }

                translucent = config.createCompatibleImage( getIconWidth(), getIconHeight(), BufferedImage.TRANSLUCENT );
            }
            Graphics2D tempGraphics = (Graphics2D) translucent.getGraphics();

            tempGraphics.setTransform( newTransform );
            node.paint( (Graphics2D) tempGraphics );
            tempGraphics.dispose();

            return translucent;
        }
    }

    /**
     * This method is only needed by certain components which require an object
     * of type image and should only be used if required.
     *
     * @param c Component to paint the buffered image on.
     *
     * @return
     */
    @Deprecated
    public BufferedImage getBufferedImage( Component c ) {
        if ( this.cachedImage == null ) {
            SVGUserAgentAdapter userAgentAdapter = new SVGUserAgentAdapter();

            cachedImage = userAgentAdapter.makeBufferedImage( c );
            // Release any memory held by document.
            document = null;
        }
        return cachedImage;
    }
}
