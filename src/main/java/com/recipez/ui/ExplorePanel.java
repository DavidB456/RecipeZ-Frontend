package com.recipez.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.recipez.core.ApiException;
import com.recipez.core.ApiTask;
import com.recipez.core.Application;
import com.recipez.recipe.Ingredient;
import com.recipez.recipe.Recipe;
import com.recipez.util.DietType;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ExplorePanel {

    // ---- shared style constants (match DashboardUI / AuthenticationUI) ----
    private static final String FIELD_STYLE =
            "arc:40;font:bold;foreground:#C5D1BA;background:#121417;"
                    + "borderWidth:4;borderColor:#C5D1BA;caretColor:#C5D1BA;"
                    + "focusWidth:0;margin:20,20,20,20;";

    private static final String COMBO_STYLE =
            "arc:20;background:#121417;foreground:#C5D1BA;borderColor:#C5D1BA;"
                    + "borderWidth:2;focusWidth:0;buttonArrowColor:#C5D1BA;"
                    + "buttonBackground:#252A2F;popupBackground:#121417;"
                    + "buttonEditableBackground:#121417;padding:5,5,5,5;";

    private static final String LABEL_STYLE =
            "font:bold; foreground:#C5D1BA;";

    // ---- layout ----
    private final JPanel     outerPanel;      // returned to DashboardUI
    private final JPanel     recipeGridPanel;
    private final JTextArea  detailArea;

    // ---- toolbar widgets ----
    private final JTextField  searchField;
    private final JComboBox<String> dietCombo;
    private final JButton     searchButton;

    public ExplorePanel() {
        outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBackground(Color.decode("#121417"));
        outerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---- toolbar (top) ----
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setBackground(Color.decode("#121417"));
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Diet type filter
        String[] dietOptions = buildDietOptions();
        dietCombo = new JComboBox<>(dietOptions);
        dietCombo.setPreferredSize(new Dimension(160, 46));
        dietCombo.putClientProperty(FlatClientProperties.STYLE, COMBO_STYLE);

        // Search field
        searchField = new JTextField();
        searchField.putClientProperty(FlatClientProperties.STYLE, FIELD_STYLE);
        searchField.setToolTipText("Search by recipe name…");

        // Search button
        searchButton = new JButton("Search");
        searchButton.setFocusPainted(false);
        searchButton.putClientProperty(FlatClientProperties.STYLE,
                "arc:20;font:bold;foreground:#121417;background:#FFE96B;"
                        + "borderWidth:0;margin:5,14,5,14;");

        toolbar.add(dietCombo,    BorderLayout.WEST);
        toolbar.add(searchField,  BorderLayout.CENTER);
        toolbar.add(searchButton, BorderLayout.EAST);

        // ---- recipe grid (center, scrollable) ----
        recipeGridPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 15, 20));
        recipeGridPanel.setBackground(Color.decode("#121417"));

        JScrollPane gridScroll = new JScrollPane(recipeGridPanel);
        gridScroll.setBorder(BorderFactory.createEmptyBorder());
        gridScroll.getViewport().setBackground(Color.decode("#121417"));
        gridScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // ---- detail sidebar (right) ----
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Color.decode("#252A2F"));
        sidebar.setPreferredSize(new Dimension(250, Integer.MAX_VALUE));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel detailHeader = new JLabel("Details");
        detailHeader.setHorizontalAlignment(JLabel.CENTER);
        detailHeader.putClientProperty(FlatClientProperties.STYLE,
                "font:bold 20; foreground:#C5D1BA;");

        detailArea = new JTextArea("Select a recipe to view details");
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.putClientProperty(FlatClientProperties.STYLE,
                "font:16; foreground:#C5D1BA; background:#252A2F");

        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailScroll.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        detailScroll.setBackground(Color.decode("#252A2F"));

        sidebar.add(detailHeader,  BorderLayout.NORTH);
        sidebar.add(detailScroll,  BorderLayout.CENTER);

        // ---- assemble ----
        outerPanel.add(toolbar,    BorderLayout.NORTH);
        outerPanel.add(gridScroll, BorderLayout.CENTER);
        outerPanel.add(sidebar,    BorderLayout.EAST);

        // ---- wire toolbar actions ----
        searchButton.addActionListener(e -> refresh());
        searchField.addActionListener(e -> refresh()); // Enter key in search field
        dietCombo.addActionListener(e -> refresh());
    }

    // ------------------------------------------------------------------ //
    //  Public API                                                          //
    // ------------------------------------------------------------------ //

    /** Returns the panel to embed in DashboardUI's CardLayout. */
    public JPanel getPanel() { return outerPanel; }

    /**
     * Calls GET /explore with current toolbar values and repaints the grid.
     * Call this when switching to the explore card.
     */
    public void refresh() {
        String search   = searchField.getText().trim();
        String selected = (String) dietCombo.getSelectedItem();
        String dietType = (selected == null || selected.equals("ALL")) ? null : selected;

        searchButton.setEnabled(false);

        ApiTask.run(
                () -> Application.apiClient.exploreRecipes(null, dietType, search),
                this::populateGrid,
                error -> {
                    searchButton.setEnabled(true);
                    String msg = (error instanceof ApiException)
                            ? ((ApiException) error).userMessage()
                            : error.getMessage();
                    JOptionPane.showMessageDialog(outerPanel,
                            "Failed to load recipes: " + msg);
                }
        );
    }

    // ------------------------------------------------------------------ //
    //  Private helpers                                                     //
    // ------------------------------------------------------------------ //

    private void populateGrid(List<Recipe> recipes) {
        searchButton.setEnabled(true);
        recipeGridPanel.removeAll();

        if (recipes == null || recipes.isEmpty()) {
            JLabel empty = new JLabel("No recipes found.");
            empty.putClientProperty(FlatClientProperties.STYLE, LABEL_STYLE);
            recipeGridPanel.add(empty);
        } else {
            for (Recipe r : recipes) {
                RecipeCardPanel card = RecipeCardPanel.forRecipe(r);
                card.addActionListener(e -> showDetail(r));
                recipeGridPanel.add(card);
            }
        }

        recipeGridPanel.revalidate();
        recipeGridPanel.repaint();
        detailArea.setText("Select a recipe to view details");
    }

    private void showDetail(Recipe r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(r.getName()).append("\n\n");

        if (r.getDescription() != null && !r.getDescription().isBlank())
            sb.append("Description:\n").append(r.getDescription()).append("\n\n");

        if (r.getInstructions() != null && !r.getInstructions().isBlank())
            sb.append("Instructions:\n").append(r.getInstructions()).append("\n\n");

        sb.append("Calories: ").append(r.getCalories()).append("\n\n");
        sb.append("Diet Type: ").append(r.getDietType()).append("\n\n");

        sb.append("Ingredients:\n");
        List<Ingredient> ings = r.getIngredients();
        if (ings != null && !ings.isEmpty()) {
            for (Ingredient ing : ings) {
                sb.append("  - ").append(ing.getQuantifier())
                        .append(" ").append(ing.getMeasurementType())
                        .append(" ").append(ing.getName()).append("\n");
            }
        } else {
            sb.append("  (none listed)\n");
        }

        detailArea.setText(sb.toString());
        detailArea.setCaretPosition(0);
    }

    /** Builds the diet type dropdown items: "ALL" first, then every enum value. */
    private static String[] buildDietOptions() {
        DietType[] values = DietType.values();
        String[] options = new String[values.length + 1];
        options[0] = "ALL";
        for (int i = 0; i < values.length; i++) {
            options[i + 1] = values[i].name();
        }
        return options;
    }
}