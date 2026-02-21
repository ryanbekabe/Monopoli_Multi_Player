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

    // Map to track visual positions for animation
    private val visualPositions = mutableMapOf<String, Float>()
    
    // Floating Emojis
    private data class FloatingEmoji(val emoji: String, val playerId: String, var x: Float, var y: Float, var alpha: Int, val startTime: Long)
    private val activeEmojis = mutableListOf<FloatingEmoji>()

    fun showEmoji(playerId: String, emoji: String) {
        val player = players.find { it.id == playerId } ?: return
        val pos = visualPositions[playerId] ?: player.position.toFloat()
        val rect = getSquareRect(pos, width.toFloat(), width.toFloat() / 11f)
        
        activeEmojis.add(FloatingEmoji(emoji, playerId, rect.centerX(), rect.centerY(), 255, System.currentTimeMillis()))
        invalidate()
    }

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

    fun setPlayers(newPlayers: List<Player>) {
        if (this.players.isEmpty()) {
            newPlayers.forEach { visualPositions[it.id] = it.position.toFloat() }
        } else {
            newPlayers.forEach { player ->
                val lastPos = visualPositions[player.id] ?: 0f
                if (Math.abs(lastPos - player.position.toFloat()) > 0.1f) {
                    animatePlayerMovement(player.id, lastPos, player.position.toFloat())
                }
            }
        }
        this.players = newPlayers
        invalidate()
    }

    private fun animatePlayerMovement(playerId: String, start: Float, end: Float) {
        var targetEnd = end
        if (targetEnd < start) targetEnd += 40f 

        val animator = android.animation.ValueAnimator.ofFloat(start, targetEnd)
        animator.duration = 1000
        animator.interpolator = android.view.animation.DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            visualPositions[playerId] = (animation.animatedValue as Float) % 40f
            invalidate()
        }
        animator.start()
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
            val rect = getSquareRect(i.toFloat(), size, squareSize)
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

        // Draw and Update Emojis
        val iterator = activeEmojis.iterator()
        val emojiPaint = Paint(labelPaint).apply { textSize = 60f }
        while (iterator.hasNext()) {
            val emoji = iterator.next()
            val elapsed = System.currentTimeMillis() - emoji.startTime
            if (elapsed > 2000) {
                iterator.remove()
                continue
            }
            
            emoji.y -= 2f // Float up
            emoji.alpha = (255 * (1f - elapsed / 2000f)).toInt()
            emojiPaint.alpha = emoji.alpha
            
            canvas.drawText(emoji.emoji, emoji.x, emoji.y, emojiPaint)
        }
        if (activeEmojis.isNotEmpty()) invalidate()
    }

    private fun getSquareRect(index: Float, totalSize: Float, squareSize: Float): RectF {
        val i = index % 40
        return when {
            i < 10 -> { // Bottom side (Right to Left)
                RectF(totalSize - (i + 1) * squareSize, totalSize - squareSize, totalSize - i * squareSize, totalSize)
            }
            i < 20 -> { // Left side (Bottom to Top)
                val subIndex = i - 10
                RectF(0f, totalSize - (subIndex + 1) * squareSize, squareSize, totalSize - subIndex * squareSize)
            }
            i < 30 -> { // Top side (Left to Right)
                val subIndex = i - 20
                RectF(subIndex * squareSize, 0f, (subIndex + 1) * squareSize, squareSize)
            }
            else -> { // Right side (Top to Bottom)
                val subIndex = i - 30
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

            // Draw Houses/Hotels
            drawHouses(canvas, rect, square, index, barSize)
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

    private fun drawHouses(canvas: Canvas, rect: RectF, square: Square, index: Int, barSize: Float) {
        if (square.houses == 0) return

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        val padding = 4f
        val buildingSize = (barSize - padding * 2)

        if (square.houses == 5) { // Hotel
            paint.color = Color.RED
            val hotelRect = when {
                index in 0..9 -> RectF(rect.centerX() - buildingSize / 2, rect.top + padding, rect.centerX() + buildingSize / 2, rect.top + barSize - padding)
                index in 10..19 -> RectF(rect.right - barSize + padding, rect.centerY() - buildingSize / 2, rect.right - padding, rect.centerY() + buildingSize / 2)
                index in 20..29 -> RectF(rect.centerX() - buildingSize / 2, rect.bottom - barSize + padding, rect.centerX() + buildingSize / 2, rect.bottom - padding)
                else -> RectF(rect.left + padding, rect.centerY() - buildingSize / 2, rect.left + barSize - padding, rect.centerY() + buildingSize / 2)
            }
            canvas.drawRect(hotelRect, paint)
            canvas.drawRect(hotelRect, borderPaint)
        } else { // Houses
            paint.color = Color.parseColor("#4CAF50") // Material Green
            val houseWidth = (rect.width() - padding * 2) / 4f
            for (i in 0 until square.houses) {
                val houseRect = when {
                    index in 0..9 -> RectF(rect.left + padding + i * houseWidth, rect.top + padding, rect.left + (i + 1) * houseWidth - padding, rect.top + barSize - padding)
                    index in 10..19 -> RectF(rect.right - barSize + padding, rect.top + padding + i * houseWidth, rect.right - padding, rect.top + (i + 1) * houseWidth - padding)
                    index in 20..29 -> RectF(rect.left + padding + i * houseWidth, rect.bottom - barSize + padding, rect.left + (i + 1) * houseWidth - padding, rect.bottom - padding)
                    else -> RectF(rect.left + padding, rect.top + padding + i * houseWidth, rect.left + barSize - padding, rect.top + (i + 1) * houseWidth - padding)
                }
                canvas.drawRect(houseRect, paint)
                canvas.drawRect(houseRect, borderPaint)
            }
        }
    }

    private fun drawPlayers(canvas: Canvas, totalSize: Float, squareSize: Float) {
        val playerPositions = mutableMapOf<Int, Int>() 

        for (player in players) {
            val visualPos = visualPositions[player.id] ?: player.position.toFloat()
            val gridPos = Math.round(visualPos) % 40
            
            val count = playerPositions.getOrDefault(gridPos, 0)
            playerPositions[gridPos] = count + 1

            val rect = getSquareRect(visualPos, totalSize, squareSize)
            
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
