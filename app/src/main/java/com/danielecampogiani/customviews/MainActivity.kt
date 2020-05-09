package com.danielecampogiani.customviews

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progress.text = "Progress View"

        plus.setOnClickListener {
            progress.progress += 0.1f
        }

        minus.setOnClickListener {
            progress.progress -= 0.1f
        }

        theme_dark bindTo MODE_NIGHT_YES
        theme_light bindTo MODE_NIGHT_NO
        theme_auto bindTo MODE_NIGHT_FOLLOW_SYSTEM

        chart.items = randomChartItems()

        animate_chart.setOnClickListener {
            chart.animateChart()
        }

        shuffle_chart.setOnClickListener {
            chart.items = randomChartItems()
            chart.animateChart()
        }
    }

}

private infix fun Button.bindTo(@NightMode mode: Int) {
    setOnClickListener { setDefaultNightMode(mode) }
}

private fun randomChartItems(random: Random = Random.Default): List<Item> {
    return (1 until random.nextInt(3, 20)).map {
        Item(it.toString(), random.nextFloat())
    }
}