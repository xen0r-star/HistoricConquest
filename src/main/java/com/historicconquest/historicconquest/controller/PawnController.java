package com.historicconquest.historicconquest.controller;

import com.historicconquest.historicconquest.model.player.PlayerColor;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class PawnController {
	private static final String BASE_HEX_COLOR = "#A2383A";
	private static final String HORSE_SVG_PATH = "/pawn/Horse-drawn.svg";
	private static final String SHIP_SVG_PATH = "/pawn/Ship.svg";
	private static final Map<String, Document> pawnFiles = new HashMap<>();

	private PawnController() { }


	public static Group createPawn(PlayerColor color, double targetSizePx) {
		return createPawn(HORSE_SVG_PATH, color, targetSizePx);
	}

	public static Group createHorsePawn(PlayerColor color, double targetSizePx) {
		return createPawn(HORSE_SVG_PATH, color, targetSizePx);
	}

	public static Group createShipPawn(PlayerColor color, double targetSizePx) {
		return createPawn(SHIP_SVG_PATH, color, targetSizePx);
	}

	public static Group createPawn(String svgResourcePath, PlayerColor color, double targetSizePx) {
		Document doc = loadSvgDocument(svgResourcePath);
		Group root = new Group();

		if (doc == null) return root;

		Color playerFxColor = mapPlayerColor(color);
		boolean baseLayerAdded = false;

		Element svg = doc.getDocumentElement();
		NodeList children = svg.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE) continue;

			Element el = (Element) node;
			if (!"path".equalsIgnoreCase(el.getTagName())) continue;

			String d = el.getAttribute("d");
			if (d.isEmpty()) continue;

			String fill = el.getAttribute("fill");
			String fillOpacityAttr = el.getAttribute("fill-opacity");
			String opacityAttr = el.getAttribute("opacity");
			String fillRuleAttr = el.getAttribute("fill-rule");


			SVGPath path = new SVGPath();
			path.setContent(d);

			Color resolvedFill = resolveFill(fill, playerFxColor);
			if (resolvedFill == null) continue;
			path.setFill(resolvedFill);
			path.setFillRule(resolveFillRule(fillRuleAttr));
			path.setStroke(null);
			applyOpacity(path, fillOpacityAttr, opacityAttr);

			root.getChildren().add(path);
			baseLayerAdded = true;
		}

		if (!baseLayerAdded) return root;

		resizeToTargetSize(root, targetSizePx);

		return root;
	}

	private static FillRule resolveFillRule(String fillRuleAttr) {
		if ("evenodd".equalsIgnoreCase(fillRuleAttr)) {
			return FillRule.EVEN_ODD;
		}

		return FillRule.NON_ZERO;
	}

	private static Color resolveFill(String fill, Color playerFxColor) {
		if (fill == null || fill.isBlank() || "none".equalsIgnoreCase(fill)) return null;
		if (equalsHexColor(fill)) return playerFxColor;

		try {
			return Color.web(fill);

		} catch (IllegalArgumentException ex) {
			return Color.BLACK;
		}
	}

	private static void applyOpacity(SVGPath path, String fillOpacityAttr, String opacityAttr) {
		double opacity = 1.0;

		if (fillOpacityAttr != null && !fillOpacityAttr.isBlank()) {
			opacity *= parseOpacity(fillOpacityAttr);
		}

		if (opacityAttr != null && !opacityAttr.isBlank()) {
			opacity *= parseOpacity(opacityAttr);
		}

		path.setOpacity(Math.max(0.0, Math.min(1.0, opacity)));
	}

	private static double parseOpacity(String value) {
		try {
			return Double.parseDouble(value.trim());

		} catch (NumberFormatException ex) {
			return 1.0;
		}
	}

	private static void resizeToTargetSize(Group root, double targetSizePx) {
		if (targetSizePx <= 0) return;

		double width = root.getLayoutBounds().getWidth();
		double height = root.getLayoutBounds().getHeight();
		double maxDimension = Math.max(width, height);
		if (maxDimension <= 0) return;

		double scale = targetSizePx / maxDimension;
		root.setScaleX(scale);
		root.setScaleY(scale);
	}

	private static boolean equalsHexColor(String svgFill) {
		if (svgFill == null) return false;

		String f = svgFill.trim();
		if (!f.startsWith("#")) {
			f = "#" + f;
		}

		return f.equalsIgnoreCase(PawnController.BASE_HEX_COLOR);
	}

	private static Document loadSvgDocument(String resourcePath) {
		Document cached = pawnFiles.get(resourcePath);
		if (cached != null) return cached;

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(false);
			factory.setIgnoringComments(true);
			factory.setIgnoringElementContentWhitespace(true);
			DocumentBuilder builder = factory.newDocumentBuilder();

			InputStream is = PawnController.class.getResourceAsStream(resourcePath);
			if (is == null) return null;

			Document parsed = builder.parse(is);
			pawnFiles.put(resourcePath, parsed);
			return parsed;

		} catch (Exception e) {
			return null;
		}
	}

	private static Color mapPlayerColor(PlayerColor color) {
		if (color == null) return Color.web(BASE_HEX_COLOR);

        return switch (color) {
            case RED ->         Color.web("#A2383A");
            case ORANGE ->      Color.web("#B9693E");
            case YELLOW ->      Color.web("#B68D3B");
            case GREEN ->       Color.web("#61712A");
            case LIME ->        Color.web("#89A238");
            case CYAN ->        Color.web("#38A270");
            case BLUE ->        Color.web("#389BA2");
            case LIGHT_BLUE ->  Color.web("#385BA2");
            case PURPLE ->      Color.web("#6838A2");
            case PINK ->        Color.web("#A23887");
        };
	}
}
