package com.historicconquest.historicconquest;

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

public class Zone extends Group {
    private final String nom ;

    public Zone(String nom , double x , double y , String blocPath)
    {
        this.nom= nom ;
        this.setLayoutX(x);
        this.setLayoutY(y);
        chargerSVG("/com/historicconquest/historicconquest/zones/"+blocPath+"/"+nom+".svg");

    }

    private void chargerSVG(String resourcePath)
    {
        try (InputStream svgStream = Zone.class.getResourceAsStream(resourcePath)) {
            if(svgStream == null)
            {
                System.err.println("Fichier non trouvé : " + resourcePath);
                return;
            }

            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(svgStream);
            NodeList paths = doc.getElementsByTagName("path");


            for (int i = 0; i < paths.getLength(); i++) {
                Element el = (Element) paths.item(i);
                SVGPath p = new SVGPath();
                p.setContent(el.getAttribute("d"));
                p.setFill(Color.LIGHTGRAY);
                p.setStroke(Color.WHITE);
                p.setStrokeWidth(0.5);
                p.setStrokeLineJoin(StrokeLineJoin.ROUND);
                p.setStrokeLineCap(StrokeLineCap.ROUND);
                this.getChildren().add(p);
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



}
