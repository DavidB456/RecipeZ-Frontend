package com.recipez.ui;

import com.formdev.flatlaf.FlatClientProperties;
import com.recipez.recipe.Recipe;

import javax.swing.*;
import java.awt.*;

/**
 * Card representing one recipe (or the "+" tile).
 * Now stores the whole Recipe object so we can grab the backend id when
 * deleting — the old "lookup by name" approach doesn't work against a real DB.
 */
public class RecipeCardPanel extends JButton {

    private final Dimension cardDimension = new Dimension(200, 120);
    private final Recipe recipe;
    private final boolean isPlusButton;
    private final JLabel recipeNameLabel;

    /** Plus-tile constructor. */
    public static RecipeCardPanel plusTile() {
        return new RecipeCardPanel(null, true);
    }

    /** Recipe-tile constructor. */
    public static RecipeCardPanel forRecipe(Recipe recipe) {
        return new RecipeCardPanel(recipe, false);
    }

    private RecipeCardPanel(Recipe recipe, boolean isPlusButton) {
        this.recipe = recipe;
        this.isPlusButton = isPlusButton;

        setLayout(new BorderLayout());
        setPreferredSize(cardDimension);
        setMinimumSize(cardDimension);
        setMaximumSize(cardDimension);

        if (isPlusButton) {
            recipeNameLabel = new JLabel("+");
            recipeNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            recipeNameLabel.setVerticalAlignment(SwingConstants.CENTER);
            recipeNameLabel.putClientProperty(FlatClientProperties.STYLE,
                    "font:bold 50; foreground:#FFE96B;");

            putClientProperty(FlatClientProperties.STYLE,
                    "arc:20; borderWidth: 4; borderColor: #FFE96B; "
                  + "background:#121417; focusedBackground:#121417");
        } else {
            recipeNameLabel = new JLabel(recipe.getName());
            recipeNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            recipeNameLabel.setVerticalAlignment(SwingConstants.CENTER);
            recipeNameLabel.putClientProperty(FlatClientProperties.STYLE,
                    "font:bold 20; foreground:#121417;");

            putClientProperty(FlatClientProperties.STYLE,
                    "arc:20; borderWidth: 0; "
                  + "background:#FFE96B; focusedBackground:#FFE96B");
        }

        add(recipeNameLabel, BorderLayout.CENTER);
    }

    public Recipe getRecipe() { return recipe; }
    public boolean getIsPlusButton() { return isPlusButton; }

    /** Backwards-compatible accessor — null for the plus tile. */
    public String getRecipeName() {
        return recipe != null ? recipe.getName() : null;
    }
}
