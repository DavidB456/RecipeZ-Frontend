package com.recipez.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.recipez.core.ApiException;
import com.recipez.core.ApiTask;
import com.recipez.core.Application;
import com.recipez.recipe.Ingredient;
import com.recipez.recipe.MeasurementType;
import com.recipez.recipe.Recipe;
import com.recipez.recipe.RecipeSearch;
import com.recipez.user.BodyGoal;
import com.recipez.util.DietType;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
//import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class DashboardUI {

    private JPanel mainPanel;
    private JPanel leftSideBarPanel;
    private JPanel rightSideBarPanel;

    private JPanel centerPanelCardContainer;
    private CardLayout centerPanelCardLayout;
    private JPanel centerPanel;
    private JPanel createRecipePanel;

    private JPanel searchBarPanel;
    private JPanel recipeGridPanel;

    private JButton homeButton;
    private JButton returnButton;
    private JButton searchButton;
    private JButton filterButton;

    private JTextField searchField;

    private JLabel rightSideBarHeaderLabel;
    private JTextArea recipeInfoLabel;

    private JButton removeRecipeButton;

    private RecipeCardPanel currentSelectedCardPanel;

    private List<RecipeCardPanel> recipeCards;

    private final ExplorePanel explorePanel = new ExplorePanel(); //new

    /** Local cache so search/filter can run client-side without re-hitting the API. */
    private List<Recipe> currentRecipes = new ArrayList<>();

    public DashboardUI() {
        mainPanel = new JPanel(new BorderLayout());
        recipeCards = new ArrayList<>();

        centerPanelCardLayout = new CardLayout();
        centerPanelCardContainer = new JPanel(centerPanelCardLayout);

        centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.decode("#121417"));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centerPanelCardContainer.add(centerPanel, "center");

        buildCreateRecipePanel();
        centerPanelCardContainer.add(explorePanel.getPanel(), "explore"); //new
        buildLayout();
    }

    // =================================================================
    // CREATE RECIPE PANEL
    // =================================================================
    private void buildCreateRecipePanel() {
        createRecipePanel = new JPanel(new GridBagLayout());
        createRecipePanel.setBackground(Color.decode("#121417"));
        createRecipePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        gbc.gridwidth = 2;
        JLabel title = new JLabel("Create Recipe");
        title.putClientProperty(FlatClientProperties.STYLE, "font: bold 25; foreground:#C5D1BA;");
        createRecipePanel.add(title, gbc);

        String labelStyle = "font: bold; foreground:#C5D1BA;";
        String fieldStyle = "arc:20;background:#121417;foreground:#C5D1BA;caretColor:#C5D1BA;"
                          + "borderColor:#C5D1BA;borderWidth:2;margin:5,5,5,5;";
        String comboStyle = "arc:20;background:#121417;foreground:#C5D1BA;borderColor:#C5D1BA;"
                          + "borderWidth:2;focusWidth:0;buttonArrowColor:#C5D1BA;"
                          + "buttonBackground:#252A2F;popupBackground:#121417;"
                          + "buttonEditableBackground:#121417;padding:5,5,5,5;";

        gbc.gridwidth = 1; gbc.gridy++;
        JLabel recipeTitle = new JLabel("Recipe Title");
        recipeTitle.putClientProperty(FlatClientProperties.STYLE, labelStyle);
        createRecipePanel.add(recipeTitle, gbc);
        gbc.gridx++;
        JTextField recipeTitleTextField = new JTextField();
        recipeTitleTextField.putClientProperty(FlatClientProperties.STYLE, fieldStyle);
        createRecipePanel.add(recipeTitleTextField, gbc);

        gbc.gridy++; gbc.gridx = 0;
        JLabel descriptionTitle = new JLabel("Description");
        descriptionTitle.putClientProperty(FlatClientProperties.STYLE, labelStyle);
        createRecipePanel.add(descriptionTitle, gbc);
        gbc.gridx = 1;
        JTextField descriptionTextField = new JTextField();
        descriptionTextField.putClientProperty(FlatClientProperties.STYLE, fieldStyle);
        createRecipePanel.add(descriptionTextField, gbc);

        gbc.gridy++; gbc.gridx = 0;
        JLabel instructionsTitle = new JLabel("Instructions");
        instructionsTitle.putClientProperty(FlatClientProperties.STYLE, labelStyle);
        createRecipePanel.add(instructionsTitle, gbc);
        gbc.gridx = 1;
        JTextField instructionsTextField = new JTextField();
        instructionsTextField.putClientProperty(FlatClientProperties.STYLE, fieldStyle);
        createRecipePanel.add(instructionsTextField, gbc);

        gbc.gridy++; gbc.gridx = 0;
        JLabel caloriesTitle = new JLabel("Calories");
        caloriesTitle.putClientProperty(FlatClientProperties.STYLE, labelStyle);
        createRecipePanel.add(caloriesTitle, gbc);
        gbc.gridx = 1;
        JTextField caloriesTextField = new JTextField();
        caloriesTextField.putClientProperty(FlatClientProperties.STYLE, fieldStyle);
        createRecipePanel.add(caloriesTextField, gbc);

        gbc.gridy++; gbc.gridx = 0;
        JLabel dietTypeTitle = new JLabel("Diet Type");
        dietTypeTitle.putClientProperty(FlatClientProperties.STYLE, labelStyle);
        createRecipePanel.add(dietTypeTitle, gbc);
        gbc.gridx = 1;
        JComboBox<DietType> dietTypeComboBox = new JComboBox<>(DietType.values());
        dietTypeComboBox.putClientProperty(FlatClientProperties.STYLE, comboStyle);
        createRecipePanel.add(dietTypeComboBox, gbc);

        // ---- ingredients ----
        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;
        JLabel ingredientTitle = new JLabel("Ingredients");
        ingredientTitle.putClientProperty(FlatClientProperties.STYLE, labelStyle);
        createRecipePanel.add(ingredientTitle, gbc);

        gbc.gridy++;
        JPanel ingredientListPanel = new JPanel(new GridBagLayout());
        ingredientListPanel.setBackground(Color.decode("#121417"));
        createRecipePanel.add(ingredientListPanel, gbc);

        GridBagConstraints igbc = new GridBagConstraints();
        igbc.insets = new Insets(5, 5, 5, 5);
        igbc.anchor = GridBagConstraints.CENTER;

        Runnable addIngredientRow = () -> {
            int row = ingredientListPanel.getComponentCount() / 3;
            igbc.gridx = 0; igbc.gridy = row;
            JTextField ingredientNameField = new JTextField(10);
            ingredientNameField.putClientProperty(FlatClientProperties.STYLE, fieldStyle);
            ingredientListPanel.add(ingredientNameField, igbc);

            igbc.gridx = 1;
            JTextField quantifierField = new JTextField(5);
            quantifierField.putClientProperty(FlatClientProperties.STYLE, fieldStyle);
            ingredientListPanel.add(quantifierField, igbc);

            igbc.gridx = 2;
            JComboBox<MeasurementType> measurementBox = new JComboBox<>(MeasurementType.values());
            measurementBox.putClientProperty(FlatClientProperties.STYLE, comboStyle);
            ingredientListPanel.add(measurementBox, igbc);

            ingredientListPanel.revalidate();
            ingredientListPanel.repaint();
        };
        addIngredientRow.run();

        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;
        JButton addIngredientButton = new JButton("Add Ingredient");
        addIngredientButton.setFocusable(false);
        addIngredientButton.putClientProperty(FlatClientProperties.STYLE,
                "arc:20;background:#252A2F;borderWidth:2;borderColor:#C5D1BA;"
                + "foreground:#C5D1BA;font:bold;margin:5,5,5,5;");
        addIngredientButton.addActionListener(e -> addIngredientRow.run());
        createRecipePanel.add(addIngredientButton, gbc);

        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;
        JButton saveRecipeButton = new JButton("Save Recipe");
        saveRecipeButton.setFocusable(false);
        saveRecipeButton.putClientProperty(FlatClientProperties.STYLE,
                "arc:20;background:#121417;borderWidth:2;borderColor:#C5D1BA;"
                + "foreground:#C5D1BA;font:bold;margin:5,5,5,5;");
        createRecipePanel.add(saveRecipeButton, gbc);

        // ====== SAVE -> POST /recipes?userId=X ======
        saveRecipeButton.addActionListener(e -> {
            try {
                String name         = recipeTitleTextField.getText().trim();
                String description  = descriptionTextField.getText().trim();
                String instructions = instructionsTextField.getText().trim();
                String caloriesStr  = caloriesTextField.getText().trim();

                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(createRecipePanel, "Recipe title cannot be empty!");
                    return;
                }

                int calories;
                try { calories = Integer.parseInt(caloriesStr); }
                catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(createRecipePanel, "Calories must be a number!");
                    return;
                }

                DietType dietType = (DietType) dietTypeComboBox.getSelectedItem();

                List<Ingredient> ingredients = new ArrayList<>();
                Component[] comps = ingredientListPanel.getComponents();
                for (int i = 0; i < comps.length; i += 3) {
                    JTextField ingredientNameField = (JTextField) comps[i];
                    JTextField quantifierField     = (JTextField) comps[i + 1];
                    JComboBox<?> measurementBox    = (JComboBox<?>) comps[i + 2];

                    String ingredientName = ingredientNameField.getText().trim();
                    String quantifierStr  = quantifierField.getText().trim();

                    if (ingredientName.isEmpty()) {
                        JOptionPane.showMessageDialog(createRecipePanel, "Ingredient name cannot be empty!");
                        return;
                    }

                    double quantifier;
                    try { quantifier = Double.parseDouble(quantifierStr); }
                    catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(createRecipePanel, "Invalid number for ingredient quantity!");
                        return;
                    }

                    MeasurementType measurement = (MeasurementType) measurementBox.getSelectedItem();
                    ingredients.add(new Ingredient(ingredientName, quantifier, measurement));
                }

                Recipe recipe = new Recipe(name, description, instructions, ingredients, calories, dietType);

                saveRecipeButton.setEnabled(false);
                Long userId = Application.activeUser.getId();

                ApiTask.run(
                        () -> Application.apiClient.createRecipe(userId, recipe),
                        saved -> {
                            saveRecipeButton.setEnabled(true);
                            JOptionPane.showMessageDialog(createRecipePanel, "Recipe saved successfully!");

                            // reset form
                            recipeTitleTextField.setText("");
                            descriptionTextField.setText("");
                            instructionsTextField.setText("");
                            caloriesTextField.setText("");
                            ingredientListPanel.removeAll();
                            addIngredientRow.run();

                            // refresh from server and switch back to home
                            refreshRecipes();
                            centerPanelCardLayout.show(centerPanelCardContainer, "center");
                        },
                        error -> {
                            saveRecipeButton.setEnabled(true);
                            JOptionPane.showMessageDialog(createRecipePanel,
                                    "Failed to save recipe: " + friendlyError(error));
                        }
                );
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(createRecipePanel,
                        "An unexpected error occurred while saving the recipe.");
            }
        });

        centerPanelCardContainer.add(createRecipePanel, "create");
    }

    // =================================================================
    // MAIN LAYOUT (sidebar, recipe grid, search/filter, right detail panel)
    // =================================================================
    private void buildLayout() {
        recipeGridPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 15, 20));
        recipeGridPanel.setBackground(Color.decode("#121417"));

        searchBarPanel = new JPanel(new BorderLayout());
        searchBarPanel.setBackground(Color.decode("#121417"));

        leftSideBarPanel = new JPanel(new BorderLayout());
        leftSideBarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftSideBarPanel.setBackground(Color.decode("#252A2F"));
        leftSideBarPanel.setPreferredSize(new Dimension(120, Integer.MAX_VALUE));

        rightSideBarPanel = new JPanel(new BorderLayout());
        rightSideBarPanel.setBackground(Color.decode("#252A2F"));
        rightSideBarPanel.setPreferredSize(new Dimension(250, Integer.MAX_VALUE));

        rightSideBarHeaderLabel = new JLabel("Details");
        rightSideBarHeaderLabel.setHorizontalAlignment(JLabel.CENTER);
        rightSideBarHeaderLabel.putClientProperty(FlatClientProperties.STYLE,
                "font:bold 20; foreground:#C5D1BA;");

        recipeInfoLabel = new JTextArea("no recipe selected");
        recipeInfoLabel.setEditable(false);
        recipeInfoLabel.setLineWrap(true);
        recipeInfoLabel.setWrapStyleWord(true);
        recipeInfoLabel.putClientProperty(FlatClientProperties.STYLE,
                "font:18; foreground:#C5D1BA; background:#252A2F");

        JScrollPane scroll = new JScrollPane(recipeInfoLabel);
        scroll.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        scroll.setBackground(Color.decode("#252A2F"));

        homeButton   = makeIconButton("/assets/home.png",          "Home");
        JButton exploreButton = makeIconButton("/assets/explore.png", "Explore"); //new
        returnButton = makeIconButton("/assets/return.png",        "Logout");
        searchButton = makeIconButton("/assets/Search.png",        "Search");
        filterButton = makeIconButton("/assets/dropdownButton.png","Filter");

        removeRecipeButton = new JButton("Remove Recipe");
        removeRecipeButton.setFocusPainted(false);
        removeRecipeButton.putClientProperty(FlatClientProperties.STYLE,
                "arc:20;font:bold 18;foreground:#C5D1BA;background:#252A2F;"
                + "borderColor:#C5D1BA;borderWidth:2;margin:5,5,5,5;");

        JPanel topButtonsPanel = new JPanel(new GridLayout(2, 1, 0, 8)); //new
        topButtonsPanel.setBackground(Color.decode("#252A2F"));
        topButtonsPanel.add(homeButton);
        topButtonsPanel.add(exploreButton);
        leftSideBarPanel.add(topButtonsPanel, BorderLayout.NORTH);
        leftSideBarPanel.add(returnButton,    BorderLayout.SOUTH); //new
        searchBarPanel.add(filterButton,   BorderLayout.WEST);

        searchField = new JTextField();
        searchField.putClientProperty(FlatClientProperties.STYLE,
                "arc:40;font:bold;foreground:#C5D1BA;background:#121417;"
                + "borderWidth:4;borderColor:#C5D1BA;caretColor:#C5D1BA;"
                + "focusWidth:0;margin:20,20,20,20;");

        searchBarPanel.add(searchField,  BorderLayout.CENTER);
        searchBarPanel.add(searchButton, BorderLayout.EAST);

        centerPanel.add(searchBarPanel,  BorderLayout.NORTH);
        centerPanel.add(recipeGridPanel, BorderLayout.CENTER);

        rightSideBarPanel.add(rightSideBarHeaderLabel, BorderLayout.NORTH);
        rightSideBarPanel.add(scroll, BorderLayout.CENTER);
        rightSideBarPanel.add(removeRecipeButton, BorderLayout.SOUTH);
        rightSideBarPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        centerPanelCardLayout.show(centerPanelCardContainer, "center");

        mainPanel.add(centerPanelCardContainer, BorderLayout.CENTER);
        mainPanel.add(rightSideBarPanel, BorderLayout.EAST);
        mainPanel.add(leftSideBarPanel,  BorderLayout.WEST);
        mainPanel.setBackground(Color.decode("#121417"));

        // ---- button handlers ----
        homeButton.addActionListener(e -> {
            refreshRecipes();
            centerPanelCardLayout.show(centerPanelCardContainer, "center");
        });

        exploreButton.addActionListener(e -> {
            explorePanel.refresh();
            centerPanelCardLayout.show(centerPanelCardContainer, "explore");
        }); //new

        searchButton.addActionListener(e -> {
            if (!searchField.getText().isBlank()) {
                populateRecipeGridPanel(RecipeSearch.searchByString(currentRecipes, searchField.getText()));
            } else {
                populateRecipeGridPanel(currentRecipes);
            }
        });

        filterButton.addActionListener(e -> openFilterDialog());

        returnButton.addActionListener(e -> {
            recipeInfoLabel.setText("no recipe selected");
            currentRecipes.clear();
            Application.activeInstance.logout();
        });

        // ====== REMOVE -> DELETE /recipes/{id} ======
        removeRecipeButton.addActionListener(e -> {
            if (currentSelectedCardPanel == null
                    || currentSelectedCardPanel.getRecipe() == null
                    || currentSelectedCardPanel.getRecipe().getId() == null) {
                return;
            }

            Recipe target = currentSelectedCardPanel.getRecipe();
            int confirm = JOptionPane.showConfirmDialog(mainPanel,
                    "Delete recipe \"" + target.getName() + "\"?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            removeRecipeButton.setEnabled(false);
            ApiTask.run(
                    () -> { Application.apiClient.deleteRecipe(target.getId()); return null; },
                    ignored -> {
                        removeRecipeButton.setEnabled(true);
                        recipeInfoLabel.setText("no recipe selected");
                        currentSelectedCardPanel = null;
                        refreshRecipes();
                    },
                    error -> {
                        removeRecipeButton.setEnabled(true);
                        JOptionPane.showMessageDialog(mainPanel,
                                "Failed to delete: " + friendlyError(error));
                    }
            );
        });
    }

    // =================================================================
    // CARD CLICK HANDLER
    // =================================================================
    private final ActionListener recipeCardClickListener = e -> {
        currentSelectedCardPanel = (RecipeCardPanel) e.getSource();

        if (currentSelectedCardPanel.getIsPlusButton()) {
            centerPanelCardLayout.show(centerPanelCardContainer, "create");
            return;
        }

        Recipe target = currentSelectedCardPanel.getRecipe();
        if (target == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(target.getName()).append("\n\n");
        sb.append("Description:\n").append(target.getDescription()).append("\n\n");
        sb.append("Instructions:\n").append(target.getInstructions()).append("\n\n");
        sb.append("Calories: ").append(target.getCalories()).append("\n\n");
        sb.append("Diet Type: ").append(target.getDietType()).append("\n\n");
        sb.append("Ingredients:\n");
        if (target.getIngredients() != null) {
            for (Ingredient ing : target.getIngredients()) {
                sb.append("- ").append(ing.getQuantifier()).append(" ")
                  .append(ing.getMeasurementType()).append(" ")
                  .append(ing.getName()).append("\n");
            }
        }
        recipeInfoLabel.setText(sb.toString());
    };

    // =================================================================
    // DATA REFRESH (called on login + after every mutation)
    // =================================================================

    /** Pulls the latest recipes from the API and rebuilds the grid. */
    public void refreshRecipes() {
        if (Application.activeUser == null) return;
        Long userId = Application.activeUser.getId();

        ApiTask.run(
                () -> Application.apiClient.getRecipesForUser(userId),
                recipes -> {
                    currentRecipes = recipes;
                    populateRecipeGridPanel(recipes);
                },
                error -> JOptionPane.showMessageDialog(mainPanel,
                        "Couldn't load recipes: " + friendlyError(error))
        );
    }

    public void populateRecipeGridPanel(List<Recipe> recipes) {
        // wipe existing cards
        for (RecipeCardPanel card : recipeCards) {
            recipeGridPanel.remove(card);
        }
        recipeCards.clear();

        // always start with the "+" tile
        RecipeCardPanel plus = RecipeCardPanel.plusTile();
        plus.addActionListener(recipeCardClickListener);
        recipeCards.add(plus);
        recipeGridPanel.add(plus);

        if (recipes != null) {
            for (Recipe r : recipes) {
                RecipeCardPanel card = RecipeCardPanel.forRecipe(r);
                card.addActionListener(recipeCardClickListener);
                recipeCards.add(card);
                recipeGridPanel.add(card);
            }
        }

        recipeGridPanel.revalidate();
        recipeGridPanel.repaint();
    }

    // =================================================================
    // FILTER DIALOG (operates on currentRecipes — same as before)
    // =================================================================
    private void openFilterDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(centerPanel), "Filter", true);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(Color.decode("#121417"));
        dialog.setSize(300, 220);
        dialog.setLocationRelativeTo(centerPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel sortLabel = new JLabel("Filter Options");
        sortLabel.putClientProperty(FlatClientProperties.STYLE,
                "foreground:#C5D1BA; font:bold 16;");
        dialog.add(sortLabel, gbc);

        gbc.gridy++;
        String[] sortOptions = {
                "None",
                "Calories Ascending",
                "Calories Descending",
                "Alphabetical Ascending",
                "Alphabetical Descending",
                "Vegetarian Only",
                "Personalized"
        };
        JComboBox<String> sortCombo = new JComboBox<>(sortOptions);
        sortCombo.putClientProperty(FlatClientProperties.STYLE,
                "arc:20;background:#121417;foreground:#C5D1BA;borderColor:#C5D1BA;"
                + "borderWidth:2;focusWidth:0;buttonArrowColor:#C5D1BA;"
                + "buttonBackground:#252A2F;popupBackground:#121417;"
                + "buttonEditableBackground:#121417;padding:5,5,5,5;");
        dialog.add(sortCombo, gbc);

        gbc.gridy++;
        JButton confirmButton = new JButton("Apply Filter");
        confirmButton.putClientProperty(FlatClientProperties.STYLE,
                "arc:20;font:bold 18;foreground:#C5D1BA;background:#252A2F;"
                + "borderColor:#C5D1BA;borderWidth:2;margin:5,5,5,5;");
        dialog.add(confirmButton, gbc);

        confirmButton.addActionListener(ev -> {
            String selection = sortCombo.getSelectedItem().toString();
            List<Recipe> filtered = new ArrayList<>(currentRecipes);
            int n = filtered.size();

            switch (selection) {
                case "None":
                    break;
                case "Calories Ascending":
                    filtered = RecipeSearch.quickSortCaloriesAscending(filtered, 0, n - 1);
                    break;
                case "Calories Descending":
                    filtered = RecipeSearch.quickSortCaloriesDescending(filtered, 0, n - 1);
                    break;
                case "Alphabetical Ascending":
                    filtered = RecipeSearch.quickSortNameAscending(filtered, 0, n - 1);
                    break;
                case "Alphabetical Descending":
                    filtered = RecipeSearch.quickSortNameDescending(filtered, 0, n - 1);
                    break;
                case "Vegetarian Only":
                    filtered = RecipeSearch.filterByDietType(filtered, DietType.VEGETARIAN);
                    break;
                case "Personalized":
                    if (Application.activeUser.getDietType() != null
                            && Application.activeUser.getDietType() != DietType.NONE) {
                        filtered = RecipeSearch.filterByDietType(filtered, Application.activeUser.getDietType());
                    }
                    if (Application.activeUser.getBodyGoal() == BodyGoal.BULK) {
                        filtered = RecipeSearch.quickSortCaloriesDescending(filtered, 0, filtered.size() - 1);
                    } else if (Application.activeUser.getBodyGoal() == BodyGoal.CUT) {
                        filtered = RecipeSearch.quickSortCaloriesAscending(filtered, 0, filtered.size() - 1);
                    }
                    break;
            }

            populateRecipeGridPanel(filtered);
            dialog.dispose();
        });

        dialog.setVisible(true);
    }

    // =================================================================
    // HELPERS
    // =================================================================

    private JButton makeIconButton(String resourcePath, String fallbackText) {
        JButton btn;
        java.net.URL url = getClass().getResource(resourcePath);
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            Image scaled = icon.getImage().getScaledInstance(
                    resourcePath.contains("Search") || resourcePath.contains("dropdown") ? 50 : 100,
                    resourcePath.contains("Search") || resourcePath.contains("dropdown") ? 50 : 100,
                    Image.SCALE_SMOOTH);
            btn = new JButton(new ImageIcon(scaled));
        } else {
            btn = new JButton(fallbackText);
        }
        int size = (resourcePath.contains("Search") || resourcePath.contains("dropdown")) ? 50 : 100;
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(size, size));
        btn.putClientProperty(FlatClientProperties.STYLE,
                "arc:20;font:bold;foreground:#C5D1BA;background:#00000000;"
                + "hoverBackground:#757E75;borderWidth:0;focusWidth:0;margin:5,5,5,5;");
        return btn;
    }

    private static String friendlyError(Throwable error) {
        if (error instanceof ApiException) return ((ApiException) error).userMessage();
        return error.getMessage() != null ? error.getMessage() : error.toString();
    }

    public JPanel getMainPanel() { return mainPanel; }
}
