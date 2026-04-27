package com.historicconquest.historicconquest.view.map;

import com.historicconquest.historicconquest.model.map.Zone;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Scale;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

public class ZoneView extends Group {
    private final Zone zone;
    private final Group zoneSVGGroup;
    private boolean hovered;
    private boolean focused;
    private boolean blockHover;

    public ZoneView(Zone zone) {
        this.zone = zone;

        setCursor(Cursor.HAND);
        setPickOnBounds(false);

        String zoneSVG = "/map/zones/" + zone.getBlocName() + "/" + zone.getName() + ".svg";
        zoneSVGGroup = loadSVG(zoneSVG, zone.getX(), zone.getY(), -1, -1, zone.getColor(), zone.getBorderColor());
        if (zoneSVGGroup == null) {
            throw new IllegalStateException("Failed to load zone SVG for: " + zone.getName());
        }
        getChildren().add(zoneSVGGroup);
        applyCurrentDisplayColor();

        zone.colorProperty().addListener((observable, oldColor, newColor) -> applyCurrentDisplayColor());

        if (zone.getIcon() != null) {
            String iconSVG = "/map/icons/" + zone.getThemes() + ".svg";
            Group iconSVGGroup = loadSVG(
                    iconSVG,
                    zone.getIcon().x(), zone.getIcon().y(),
                    zone.getIcon().width(), zone.getIcon().height(),
                    Color.web("#635341"),
                    Color.web("#635341")
            );

            if (iconSVGGroup != null) {
                iconSVGGroup.setOpacity(0.5);
                iconSVGGroup.setMouseTransparent(true);
                getChildren().add(iconSVGGroup);
            }
        }

        setOnMouseEntered(event -> setHovered(true));
        setOnMouseExited(event -> setHovered(false));
    }

    public Zone getZone() {
        return zone;
    }

    public Group getZoneSVGGroup() {
        return zoneSVGGroup;
    }

    public void setFocusedZone(boolean focused) {
        this.focused = focused;
        applyCurrentDisplayColor();
    }

    public void setBlockHover(boolean blockHover) {
        this.blockHover = blockHover;
        setCursor(blockHover ? Cursor.DEFAULT : Cursor.HAND);
        applyCurrentDisplayColor();
    }

    public void setColor(Color color) {
        zone.setColor(color);
    }

    public Color getColor() {
        return zone.getColor();
    }

    private void setHovered(boolean hovered) {
        this.hovered = hovered;
        applyCurrentDisplayColor();
    }

    private void applyCurrentDisplayColor() {
        Color baseColor = zone.getColor();
        if (baseColor == null) {
            return;
        }

        if (hovered && !blockHover && !focused) {
            updateColor(baseColor.deriveColor(0, 1.0, 0.85, 1.0));
        } else {
            updateColor(baseColor);
        }
    }

    private void updateColor(Color newColor) {
        if (zoneSVGGroup == null) return;
        zoneSVGGroup.getChildren().forEach(node -> {
            if (node instanceof SVGPath svgPath && svgPath.getFill() != null) {
                svgPath.setFill(newColor);
            }
        });
    }

    private Group loadSVG(String resourcePath, double x, double y, double width, double height, Color fillColor, Color borderColor) {
        try (InputStream svgStream = ZoneView.class.getResourceAsStream(resourcePath)) {
            if (svgStream == null) {
                System.err.println("File not found: " + resourcePath);
                return null;
            }

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(svgStream);
            NodeList paths = doc.getElementsByTagName("path");

            Group svgGroup = new Group();
            for (int i = 0; i < paths.getLength(); i++) {
                Element el = (Element) paths.item(i);
                SVGPath p = getSvgPath(fillColor, borderColor, el);
                svgGroup.getChildren().add(p);
            }

            if (width > 0 && height > 0) {
                Bounds bounds = svgGroup.getLayoutBounds();
                double scaleX = width / bounds.getWidth();
                double scaleY = height / bounds.getHeight();
                svgGroup.getTransforms().add(new Scale(scaleX, scaleY, 0, 0));
                svgGroup.setTranslateX(x);
                svgGroup.setTranslateY(y);
            } else {
                svgGroup.setLayoutX(x);
                svgGroup.setLayoutY(y);
            }

            return svgGroup;
        } catch (Exception e) {
            System.err.println("Error loading SVG: " + resourcePath);
            return null;
        }
    }

    private static SVGPath getSvgPath(Color fillColor, Color borderColor, Element el) {
        SVGPath p = new SVGPath();
        p.setContent(el.getAttribute("d"));
        p.setStroke(borderColor);

        if (!el.getAttribute("fill").isEmpty()) {
            p.setFill(fillColor);
        } else {
            p.setFill(null);
        }

        String valSW = el.getAttribute("stroke-width");
        if (!valSW.isEmpty()) {
            p.setStrokeWidth(Double.parseDouble(valSW.replace("px", "")));
        } else {
            p.setStrokeWidth(0.5);
        }

        p.setStrokeLineJoin(StrokeLineJoin.ROUND);
        p.setStrokeLineCap(StrokeLineCap.ROUND);
        return p;
    }
}
