package com.historicconquest.historicconquest.map;

import com.historicconquest.historicconquest.player.Pawn;
import com.historicconquest.historicconquest.questions.TypeThemes;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.List;

public class Zone extends Group {
    private final String name;
    private final String blocName;
    private final TypeThemes themes;
    private int power;
    private List<Pawn> pawns;

    private final double x, y;
    private final ZoneIcon icon;


    public Zone(String name, String blocName, TypeThemes themes, int power, double x, double y, ZoneIcon icon) {
        this.name = name;
        this.blocName = blocName;
        this.themes = themes;
        this.power = power;

        this.x = x;
        this.y = y;
        this.icon = icon;

        String zoneSVG = "/com/historicconquest/historicconquest/zones/" + blocName + "/" + name + ".svg";
        Group zoneSVGGroup = loadSVG(
            zoneSVG,
            x, y,
            -1, -1,
            Color.LIGHTGRAY
        );

        String iconSVG = "/com/historicconquest/historicconquest/icons/" + themes + ".svg";
        Group iconSVGGroup = loadSVG(
            iconSVG,
            icon.x(), icon.y(),
            icon.width(), icon.height(),
            Color.WHITE
        );

        if (iconSVGGroup != null) iconSVGGroup.setOpacity(0.5);


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
                double originalWidth = svgGroup.getBoundsInLocal().getWidth();
                double originalHeight = svgGroup.getBoundsInLocal().getHeight();

                double scaleX = width / originalWidth;
                double scaleY = height / originalHeight;

                svgGroup.setScaleX(scaleX);
                svgGroup.setScaleY(scaleY);

                double deltaX = (originalWidth - (originalWidth * scaleX)) / 2;
                double deltaY = (originalHeight - (originalHeight * scaleY)) / 2;

                svgGroup.setLayoutX(x - deltaX);
                svgGroup.setLayoutY(y - deltaY);

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
        p.setStroke(Color.WHITE);

        String valF = el.getAttribute("fill");
        if (!valF.isEmpty()) {
            p.setFill(fillColor);
        } else {
            p.setFill(Color.TRANSPARENT);
        }

        String valSW = el.getAttribute("stroke-width");
        if (!valSW.isEmpty()) {
            p.setStrokeWidth(Double.parseDouble(valSW.replace("px", "")));

        } else {
            p.setStrokeWidth(1.0);
        }

        p.setStrokeLineJoin(StrokeLineJoin.ROUND);
        p.setStrokeLineCap(StrokeLineCap.ROUND);
        return p;
    }


    public static record ZoneIcon(double x, double y, double width, double height) {}
}
