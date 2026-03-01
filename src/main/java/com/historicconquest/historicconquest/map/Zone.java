package com.historicconquest.historicconquest.map;

import com.historicconquest.historicconquest.Constant;
import com.historicconquest.historicconquest.player.Pawn;
import com.historicconquest.historicconquest.questions.TypeThemes;
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
import java.util.ArrayList;
import java.util.List;

public class Zone extends Group {
    // Zone property
    private final String name;
    private final String blocName;
    private final TypeThemes themes;
    private int power;
    private List<Pawn> pawns;
    private List<Zone> adjacentZones;

    private Color color;
    private boolean isFocusedZone = false;

    // SVG groups
    private final Group zoneSVGGroup;


    public Zone(String name, String blocName, TypeThemes themes, int power, double x, double y, ZoneIcon icon, Color color) {
        this.name = name;
        this.blocName = blocName;
        this.themes = themes;
        this.power = power;
        this.adjacentZones = new ArrayList<>();

        // Style property
        this.color = color;

        this.setCursor(Cursor.HAND);
        this.setPickOnBounds(false);


        String zoneSVG = Constant.PATH + "zones/" + blocName + "/" + name + ".svg";
        zoneSVGGroup = loadSVG(
            zoneSVG,
            x, y,
            -1, -1,
            color
        );

        if (zoneSVGGroup == null) {
            System.err.println("Failed to load zone SVG for: " + name);
            return;
        }



        String iconSVG = Constant.PATH + "icons/" + themes + ".svg";
        Group iconSVGGroup = loadSVG(
            iconSVG,
            icon.x(), icon.y(),
            icon.width(), icon.height(),
            Color.web("#635341")
        );

        if (iconSVGGroup == null) {
            System.err.println("Failed to load icon SVG for theme: " + themes);
            return;
        }

        iconSVGGroup.setOpacity(0.5);
        iconSVGGroup.setMouseTransparent(true);



        this.setOnMouseEntered(event -> setHovered(true));
        this.setOnMouseExited(event -> setHovered(false));

        this.getChildren().addAll(zoneSVGGroup, iconSVGGroup);
    }



    private Group loadSVG(String resourcePath, double x, double y, double width, double height, Color fillColor) {
        try (InputStream svgStream = Zone.class.getResourceAsStream(resourcePath)) {
            if (svgStream == null) {
                System.err.println("File not found: " + resourcePath);
                return null;
            }

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(svgStream);
            NodeList paths = doc.getElementsByTagName("path");

            Group svgGroup = new Group();
            for (int i = 0; i < paths.getLength(); i++) {
                Element el = (Element) paths.item(i);
                SVGPath p = getSvgPath(fillColor, el);

                svgGroup.getChildren().add(p);
            }

            if (width > 0 && height > 0) {
                Bounds bounds = svgGroup.getLayoutBounds();

                double scaleX = width / bounds.getWidth();
                double scaleY = height / bounds.getHeight();

                Scale scale = new Scale(scaleX, scaleY, 0, 0);
                svgGroup.getTransforms().add(scale);

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

    private static SVGPath getSvgPath(Color fillColor, Element el) {
        SVGPath p = new SVGPath();
        p.setContent(el.getAttribute("d"));
        p.setStroke(Color.web("#635341"));

        String valF = el.getAttribute("fill");
        if (!valF.isEmpty()) {
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


    public void updateColor(Color newColor) {
        if (zoneSVGGroup != null) {
            zoneSVGGroup.getChildren().forEach(node -> {
                if (node instanceof SVGPath svgPath) {
                    if (svgPath.getFill() != null) {
                        svgPath.setFill(newColor);
                    }
                }
            });
        }
    }



    public record ZoneIcon(double x, double y, double width, double height) {}


    public String getName() {
        return name;
    }

    public Color getColor() {
        return color;
    }

    public Group getZoneSVGGroup() {
        return zoneSVGGroup;
    }

    public List<Zone> getAdjacentZones() {
        return adjacentZones;
    }


    public void setFocusedZone(boolean focused) {
        isFocusedZone = focused;
    }

    private void setHovered(boolean hovered) {
        if (isFocusedZone) return;

        if (hovered) updateColor(color.deriveColor(0, 1.0, 0.85, 1.0));
        else         updateColor(color);
    }

    public void setColor(Color color) {
        this.color = color;
        updateColor(color);
    }

    public void addAdjacentZones(Zone zone) {
        if (adjacentZones.contains(zone)) return;
        adjacentZones.add(zone);
    }
}
