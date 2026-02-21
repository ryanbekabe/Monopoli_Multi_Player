package com.hanyajasa.monopolimultiplayer.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.hanyajasa.monopolimultiplayer.model.Player
import com.hanyajasa.monopolimultiplayer.model.Square
import com.hanyajasa.monopolimultiplayer.model.SquareType

class MonopolyBoardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var squares: List<Square> = emptyList()
    private var players: List<Player> = emptyList()
    private var centerMessage: String = "Selamat Datang di Monopoli!"

    fun setCenterMessage(message: String) {
        this.centerMessage = message
        invalidate()
    }

    private val boardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 20f
        textAlign = Paint.Align.CENTER
    }

    private val playerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val centerCardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F3F4F6") // Off-white
        style = Paint.Style.FILL
        setShadowLayer(10f, 0f, 5f, Color.LTGRAY)
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 24f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    fun setSquares(squares: List<Square>) {
        this.squares = squares
        invalidate()
    }

    fun setPlayers(players: List<Player>) {
        this.players = players
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = minOf(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (squares.isEmpty()) return

        val size = width.toFloat()
        val squareSize = size / 11f // 11 squares per side (including corners)

        // Draw Board Background
        canvas.drawRect(0f, 0f, size, size, boardPaint)

        // Draw Squares
        for (i in 0 until 40) {
            val rect = getSquareRect(i, size, squareSize)
            drawSquare(canvas, rect, squares[i], i)
        }

        // Draw Players
        drawPlayers(canvas, size, squareSize)

        // Draw Center Area (The Card Area)
        val centerMargin = squareSize * 1.5f
        val centerRect = RectF(centerMargin, centerMargin, size - centerMargin, size - centerMargin)
        
        // Draw decorative center card
        canvas.drawRoundRect(centerRect, 20f, 20f, centerCardPaint)
        canvas.drawRoundRect(centerRect, 20f, 20f, borderPaint)

        // Draw Center Message
        val messageLines = centerMessage.split("\n")
        var messageY = centerRect.centerY() - (messageLines.size - 1) * 20f
        for (line in messageLines) {
            canvas.drawText(line, centerRect.centerX(), messageY, labelPaint)
            messageY += 40f
        }

        // Potential Logo (Simplified "MONOPOLY")
        val logoPaint = Paint(labelPaint).apply {
            color = Color.RED
            textSize = 60f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
        }
        canvas.drawText("MONOPOLI", centerRect.centerX(), centerRect.top + squareSize * 1.2f, logoPaint)
    }

    private fun getSquareRect(index: Int, totalSize: Float, squareSize: Float): RectF {
        return when {
            index in 0 until 10 -> { // Bottom side (Right to Left)
                RectF(totalSize - (index + 1) * squareSize, totalSize - squareSize, totalSize - index * squareSize, totalSize)
            }
            index in 10 until 20 -> { // Left side (Bottom to Top)
                val subIndex = index - 10
                RectF(0f, totalSize - (subIndex + 1) * squareSize, squareSize, totalSize - subIndex * squareSize)
            }
            index in 20 until 30 -> { // Top side (Left to Right)
                val subIndex = index - 20
                RectF(subIndex * squareSize, 0f, (subIndex + 1) * squareSize, squareSize)
            }
            else -> { // Right side (Top to Bottom)
                val subIndex = index - 30
                RectF(totalSize - squareSize, subIndex * squareSize, totalSize, (subIndex + 1) * squareSize)
            }
        }
    }

    private fun drawSquare(canvas: Canvas, rect: RectF, square: Square, index: Int) {
        // Draw background based on type
        val bgPaint = Paint().apply {
            style = Paint.Style.FILL
            color = when (square.type) {
                SquareType.GO -> Color.parseColor("#C8E6C9")
                SquareType.JAIL -> Color.parseColor("#FFCDD2")
                SquareType.FREE_PARKING -> Color.parseColor("#FFF9C4")
                SquareType.GO_TO_JAIL -> Color.parseColor("#E1BEE7")
                SquareType.TAX -> Color.parseColor("#F5F5F5")
                else -> Color.WHITE
            }
        }
        canvas.drawRect(rect, bgPaint)
        canvas.drawRect(rect, borderPaint)

        if (square.type == SquareType.PROPERTY && square.colorGroup != null) {
            val paint = Paint().apply {
                color = getColorFromString(square.colorGroup)
                style = Paint.Style.FILL
            }
            val barSize = if (index in 0..9 || index in 20..29) rect.height() * 0.25f else rect.width() * 0.25f
            val cRect = when {
                index in 0..9 -> RectF(rect.left, rect.top, rect.right, rect.top + barSize) // Bottom
                index in 10..19 -> RectF(rect.right - barSize, rect.top, rect.right, rect.bottom) // Left
                index in 20..29 -> RectF(rect.left, rect.bottom - barSize, rect.right, rect.bottom) // Top
                else -> RectF(rect.left, rect.top, rect.left + barSize, rect.bottom) // Right
            }
            canvas.drawRect(cRect, paint)
            
            // Draw a subtle line separating color bar
            canvas.drawRect(cRect, borderPaint)

            // Draw Price at bottom of square
            val pricePaint = Paint(textPaint).apply { textSize = 16f; color = Color.GRAY }
            val priceY = when {
                index in 0..9 -> rect.bottom - 5f
                index in 20..29 -> rect.top + 20f
                else -> rect.centerY() + 35f
            }
            canvas.drawText("$${square.price}", rect.centerX(), priceY, pricePaint)
        }

        // Small icon or text for special squares
        when (square.type) {
            SquareType.TAX -> {
                val taxPaint = Paint(labelPaint).apply { textSize = 30f; color = Color.RED }
                canvas.drawText("$", rect.centerX(), rect.centerY() - 10f, taxPaint)
            }
            SquareType.CHANCE -> {
                val chancePaint = Paint(labelPaint).apply { textSize = 40f; color = Color.BLUE }
                canvas.drawText("?", rect.centerX(), rect.centerY() + 10f, chancePaint)
            }
            SquareType.COMMUNITY_CHEST -> {
                val chestPaint = Paint(labelPaint).apply { textSize = 40f; color = Color.parseColor("#FF9800") }
                canvas.drawText("✉", rect.centerX(), rect.centerY() + 10f, chestPaint)
            }
            else -> {}
        }

        // Draw Name (Simplified)
        val nameLines = square.name.split(" ")
        var yOffset = rect.centerY() - (nameLines.size - 1) * 10f
        for (line in nameLines) {
            canvas.drawText(line, rect.centerX(), yOffset, textPaint)
            yOffset += 22f
        }
    }

    private fun drawPlayers(canvas: Canvas, totalSize: Float, squareSize: Float) {
        val playerPositions = mutableMapOf<Int, Int>() // Position -> Count at that position

        for (player in players) {
            val pos = player.position
            val count = playerPositions.getOrDefault(pos, 0)
            playerPositions[pos] = count + 1

            val rect = getSquareRect(pos, totalSize, squareSize)
            
            // Calculate player offset within square to avoid overlap
            val offsetX = (count % 2) * (squareSize / 3f) - (squareSize / 6f)
            val offsetY = (count / 2) * (squareSize / 3f) - (squareSize / 6f)

            playerPaint.color = player.color
            canvas.drawCircle(rect.centerX() + offsetX, rect.centerY() + offsetY, squareSize / 6f, playerPaint)
            
            // Draw border for player token
            val oldStyle = playerPaint.style
            playerPaint.style = Paint.Style.STROKE
            playerPaint.color = Color.BLACK
            playerPaint.strokeWidth = 2f
            canvas.drawCircle(rect.centerX() + offsetX, rect.centerY() + offsetY, squareSize / 6f, playerPaint)
            playerPaint.style = oldStyle
        }
    }

    private fun getColorFromString(color: String): Int {
        return when (color) {
            "BROWN" -> Color.parseColor("#955436")
            "LIGHTBLUE" -> Color.parseColor("#AAE0FA")
            "PINK" -> Color.parseColor("#D93A96")
            "ORANGE" -> Color.parseColor("#F7941D")
            "RED" -> Color.parseColor("#ED1B24")
            "YELLOW" -> Color.parseColor("#FEF200")
            "GREEN" -> Color.parseColor("#1FB25A")
            "DARKBLUE" -> Color.parseColor("#0072BB")
            else -> Color.GRAY
        }
    }
}
