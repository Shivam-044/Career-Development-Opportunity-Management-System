package com.studenttracker.fx;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Guards the contract between each FXML file and its controller without starting JavaFX: every fx:id needs
 * an @FXML field, every field needs an fx:id, and every onAction handler needs an @FXML method. A mismatch
 * would otherwise only show up as a null pointer when the page is first opened.
 */
class FxmlConsistencyTest {

    private static final String FXML_NS = "http://javafx.com/fxml/1";
    private static final Path VIEWS = Path.of("src", "main", "resources", "com", "studenttracker", "fx", "view");

    @Test
    void everyViewMatchesItsController() throws Exception {
        List<String> problems = new ArrayList<>();
        List<Path> files;
        try (Stream<Path> walk = Files.walk(VIEWS)) {
            files = walk.filter(path -> path.toString().endsWith(".fxml")).sorted().toList();
        }
        assertTrue(files.size() >= 17, "expected the page and dialog views to be present");
        for (Path file : files) {
            check(file, problems);
        }
        assertTrue(problems.isEmpty(), String.join("\n", problems));
    }

    private static void check(Path file, List<String> problems) throws Exception {
        Document document = parse(file);
        Element root = document.getDocumentElement();
        String controllerName = root.getAttributeNS(FXML_NS, "controller");
        if (controllerName.isEmpty()) {
            problems.add(file.getFileName() + ": no fx:controller");
            return;
        }
        Class<?> controller;
        try {
            controller = Class.forName(controllerName);
        } catch (ClassNotFoundException ex) {
            problems.add(file.getFileName() + ": controller class not found: " + controllerName);
            return;
        }

        Set<String> fields = new HashSet<>();
        Set<String> methods = new HashSet<>();
        for (Class<?> type = controller; type != null && type != Object.class; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (isInjected(field)) {
                    fields.add(field.getName());
                }
            }
            for (Method method : type.getDeclaredMethods()) {
                if (isInjected(method)) {
                    methods.add(method.getName());
                }
            }
        }

        Set<String> ids = new HashSet<>();
        NodeList all = document.getElementsByTagName("*");
        for (int i = 0; i < all.getLength(); i++) {
            Element element = (Element) all.item(i);
            String id = element.getAttributeNS(FXML_NS, "id");
            if (!id.isEmpty()) {
                ids.add(id);
                if (!fields.contains(id)) {
                    problems.add(file.getFileName() + ": fx:id \"" + id + "\" has no @FXML field in "
                            + controller.getSimpleName());
                }
            }
            String action = element.getAttribute("onAction");
            if (action.startsWith("#") && !methods.contains(action.substring(1))) {
                problems.add(file.getFileName() + ": handler " + action + " has no @FXML method in "
                        + controller.getSimpleName());
            }
        }
        for (String field : fields) {
            if (!ids.contains(field)) {
                problems.add(file.getFileName() + ": @FXML field \"" + field + "\" has no fx:id in the file");
            }
        }
    }

    private static boolean isInjected(java.lang.reflect.AnnotatedElement element) {
        return element.isAnnotationPresent(javafx.fxml.FXML.class);
    }

    private static Document parse(Path file) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            return factory.newDocumentBuilder().parse(file.toFile());
        } catch (Exception ex) {
            throw new IOException("Couldn't parse " + file, ex);
        }
    }
}
