package com.example.cafeglasspoc

import android.content.Context
import android.app.Presentation
import android.os.Bundle
import android.view.Display
import android.view.WindowManager
import android.widget.TextView

class GlassPresentation(context: Context, display: Display) : Presentation(context, display) {

    private lateinit var tvTitle: TextView
    private lateinit var tvStep1: TextView
    private lateinit var tvStep2: TextView
    private lateinit var tvStep3: TextView

    // Rush mode views
    private lateinit var tvRushTitle: TextView
    private lateinit var tvRushOrders: TextView

    private lateinit var recipeContainer: android.view.View
    private lateinit var rushContainer: android.view.View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Force fullscreen black background
        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        setContentView(R.layout.presentation_glass)

        recipeContainer = findViewById(R.id.recipeContainer)
        rushContainer   = findViewById(R.id.rushContainer)

        tvTitle   = findViewById(R.id.tvTitle)
        tvStep1   = findViewById(R.id.tvStep1)
        tvStep2   = findViewById(R.id.tvStep2)
        tvStep3   = findViewById(R.id.tvStep3)

        tvRushTitle  = findViewById(R.id.tvRushTitle)
        tvRushOrders = findViewById(R.id.tvRushOrders)
    }

    /**
     * Renders the recipe with the active step highlighted in cyan,
     * and future/past steps in dimmed magenta.
     */
    fun showRecipeStep(steps: List<String>, activeIndex: Int) {
        recipeContainer.visibility = android.view.View.VISIBLE
        rushContainer.visibility   = android.view.View.GONE

        tvTitle.text = "[ APPRENTICE MODE : CHAI MILK TEA (S) ]"

        val stepViews = listOf(tvStep1, tvStep2, tvStep3)
        val labels    = listOf("STEP 1", "STEP 2", "STEP 3")

        stepViews.forEachIndexed { index, tv ->
            if (index < steps.size) {
                tv.text = "▶  ${labels[index]}\n${steps[index]}"
                if (index == activeIndex) {
                    // Active step: bright cyan, larger
                    tv.setTextColor(android.graphics.Color.parseColor("#00FFCC"))
                    tv.alpha     = 1f
                    tv.textSize  = 32f
                } else {
                    // Inactive: dimmed magenta
                    tv.setTextColor(android.graphics.Color.parseColor("#FF3366"))
                    tv.alpha     = 0.45f
                    tv.textSize  = 24f
                }
            } else {
                tv.text = ""
            }
        }
    }

    /**
     * Switches the glass display to Rush Mode with a list of pending orders.
     */
    fun showRushMode() {
        recipeContainer.visibility = android.view.View.GONE
        rushContainer.visibility   = android.view.View.VISIBLE

        tvRushTitle.text = "⚡  RUSH MODE  ⚡"
        tvRushOrders.text = """
            ☕  1x  Espresso
            
            🍵  2x  Matcha Latte
            
            🧋  1x  Chai Milk Tea (S)
            
            ☕  1x  Americano
        """.trimIndent()
    }
}