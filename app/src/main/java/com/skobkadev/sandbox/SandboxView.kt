package com.skobkadev.sandbox

import android.content.Context
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

object Config {
    const val SCALE_PERCENT = 30f
    const val BRUSH_RADIUS = 3
}

enum class Material { EMPTY, SAND, FIRE, BLUE_FIRE, GLASS, WATER, STEAM, RESET }

class SandboxView(context: Context) : View(context), SensorEventListener {
    private var cellSize = 1f
    private var cols = 0; private var rows = 0
    private var typeGrid = Array(0) { Array(0) { Material.EMPTY } }
    private var colorGrid = Array(0) { IntArray(0) }
    private val paint = Paint().apply { isAntiAlias = false }
    private val activeTouches = mutableMapOf<Int, Pair<Float, Float>>()
    private var currentTool = Material.SAND
    private var showMenu = false
    private var lastClickTime = 0L

    private val menuItems = listOf(Material.RESET, Material.EMPTY, Material.SAND, Material.WATER, Material.FIRE, Material.BLUE_FIRE, Material.GLASS)
    private val itemLabels = listOf("Сброс", "Ластик", "Песок", "Вода", "Огонь", "Син.Огонь", "Стекло")
    private var scrollOffset = 0f; private var targetScroll = 0f; private var lastTouchX = 0f

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var dX = 0; private var dY = 1

    init { sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME) }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cellSize = (2f + (38f) * (Config.SCALE_PERCENT / 100f))
        cols = (w / cellSize).toInt().coerceAtLeast(1)
        rows = (h / cellSize).toInt().coerceAtLeast(1)
        typeGrid = Array(cols) { Array(rows) { Material.EMPTY } }
        colorGrid = Array(cols) { IntArray(rows) }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val rx = -event.values[0]
            val ry = event.values[1]
            dX = if (rx > 1.2f) 1 else if (rx < -1.2f) -1 else 0
            dY = if (ry > 1.2f) 1 else if (ry < -1.2f) -1 else 0
            if (dX == 0 && dY == 0) dY = 1
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLACK)

        activeTouches.values.forEach { if (!checkMenuClick(it.first, it.second, false)) spawn(it.first, it.second) }

        val nextType = Array(cols) { Array(rows) { Material.EMPTY } }
        val nextColor = Array(cols) { IntArray(rows) }

        // Для предотвращения башен меняем направление прохода каждый кадр
        val frameShift = (System.currentTimeMillis() / 16 % 2 == 0L)

        for (y in rows - 1 downTo 0) {
            val xRange = if (frameShift) (0 until cols) else (cols - 1 downTo 0)
            for (x in xRange) {
                val type = typeGrid[x][y]
                if (type == Material.EMPTY) continue

                paint.color = colorGrid[x][y]
                canvas.drawRect(x * cellSize, y * cellSize, (x + 1) * cellSize + 0.5f, (y + 1) * cellSize + 0.5f, paint)

                when (type) {
                    Material.WATER -> updateWater(x, y, nextType, nextColor)
                    Material.SAND -> updateSand(x, y, nextType, nextColor)
                    Material.FIRE, Material.BLUE_FIRE -> updateFire(x, y, type, nextType, nextColor)
                    Material.STEAM -> updateSteam(x, y, nextType, nextColor)
                    else -> {
                        nextType[x][y] = type
                        nextColor[x][y] = colorGrid[x][y]
                    }
                }
            }
        }
        typeGrid = nextType; colorGrid = nextColor
        if (showMenu) { scrollOffset += (targetScroll - scrollOffset) * 0.2f; drawModernMenu(canvas) }
        invalidate()
    }

    private fun updateWater(x: Int, y: Int, nextType: Array<Array<Material>>, nextColor: Array<IntArray>) {
        val c = colorGrid[x][y]

        // 1. Пытаемся упасть вниз (с учетом акселерометра)
        if (isFree(x + dX, y + dY, nextType)) {
            nextType[x + dX][y + dY] = Material.WATER; nextColor[x + dX][y + dY] = c
            return
        }

        // 2. Если внизу занято, ищем путь в стороны (растекание)
        // Чтобы не было башен, вода проверяет несколько клеток вбок
        val dir = if (Random.nextBoolean()) 1 else -1
        for (dist in 1..3) { // Проверяем до 3 клеток в сторону
            if (isFree(x + dir * dist, y, nextType)) {
                nextType[x + dir * dist][y] = Material.WATER; nextColor[x + dir * dist][y] = c
                return
            } else if (isFree(x - dir * dist, y, nextType)) {
                nextType[x - dir * dist][y] = Material.WATER; nextColor[x - dir * dist][y] = c
                return
            }
        }

        // 3. Если совсем некуда течь, остаемся на месте
        nextType[x][y] = Material.WATER; nextColor[x][y] = c
    }

    private fun updateSand(x: Int, y: Int, nextType: Array<Array<Material>>, nextColor: Array<IntArray>) {
        val c = colorGrid[x][y]
        if (isFree(x + dX, y + dY, nextType)) {
            nextType[x + dX][y + dY] = Material.SAND; nextColor[x + dX][y + dY] = c
        } else if (isFree(x + dX + 1, y + dY, nextType)) {
            nextType[x + dX + 1][y + dY] = Material.SAND; nextColor[x + dX + 1][y + dY] = c
        } else if (isFree(x + dX - 1, y + dY, nextType)) {
            nextType[x + dX - 1][y + dY] = Material.SAND; nextColor[x + dX - 1][y + dY] = c
        } else {
            nextType[x][y] = Material.SAND; nextColor[x][y] = c
        }
    }

    private fun updateFire(x: Int, y: Int, type: Material, nextType: Array<Array<Material>>, nextColor: Array<IntArray>) {
        if (Random.nextFloat() < 0.15f) return
        for (nx in x-1..x+1) for (ny in y-1..y+1) {
            if (nx in 0 until cols && ny in 0 until rows && typeGrid[nx][ny] == Material.WATER) {
                nextType[nx][ny] = Material.STEAM; nextColor[nx][ny] = Color.GRAY
            }
        }
        val nx = x + Random.nextInt(-1, 2); val ny = y - 1
        if (isFree(nx, ny, nextType)) {
            nextType[nx][ny] = type
            nextColor[nx][ny] = if (type == Material.FIRE) Color.rgb(255, Random.nextInt(50, 200), 0) else Color.rgb(0, Random.nextInt(100, 255), 255)
        } else { nextType[x][y] = type; nextColor[x][y] = colorGrid[x][y] }
    }

    private fun updateSteam(x: Int, y: Int, nextType: Array<Array<Material>>, nextColor: Array<IntArray>) {
        if (Random.nextFloat() < 0.015f) return
        val tx = x + Random.nextInt(-1, 2); val ty = y - 1
        if (isFree(tx, ty, nextType)) {
            nextType[tx][ty] = Material.STEAM; nextColor[tx][ty] = colorGrid[x][y]
        } else { nextType[x][y] = Material.STEAM; nextColor[x][y] = colorGrid[x][y] }
    }

    private fun isFree(x: Int, y: Int, nextType: Array<Array<Material>>): Boolean {
        return x in 0 until cols && y in 0 until rows && typeGrid[x][y] == Material.EMPTY && nextType[x][y] == Material.EMPTY
    }

    private fun spawn(touchX: Float, touchY: Float) {
        val centerX = (touchX / cellSize).toInt(); val centerY = (touchY / cellSize).toInt()
        val r = Config.BRUSH_RADIUS
        for (i in -r..r) for (j in -r..r) {
            val nx = centerX + i; val ny = centerY + j
            if (nx in 0 until cols && ny in 0 until rows && (i*i + j*j <= r*r)) {
                if (currentTool == Material.EMPTY) typeGrid[nx][ny] = Material.EMPTY
                else if (typeGrid[nx][ny] == Material.EMPTY) {
                    typeGrid[nx][ny] = currentTool
                    colorGrid[nx][ny] = when(currentTool) {
                        Material.SAND -> Color.HSVToColor(floatArrayOf(45f + Random.nextFloat() * 5f, 0.8f, 1f))
                        Material.WATER -> Color.rgb(0, 100, 255)
                        Material.FIRE -> Color.rgb(255, 100, 0)
                        Material.BLUE_FIRE -> Color.rgb(0, 180, 255)
                        Material.GLASS -> Color.argb(160, 200, 230, 255)
                        else -> 0
                    }
                }
            }
        }
    }

    private fun drawModernMenu(canvas: Canvas) {
        val itemWidth = width / 3f; val menuHeight = 260f; val yPos = 60f
        canvas.save(); canvas.clipRect(0f, 0f, width.toFloat(), yPos + menuHeight + 40f)
        val p = Paint().apply { isAntiAlias = true }
        menuItems.forEachIndexed { i, material ->
            val x = i * itemWidth + scrollOffset
            val rect = RectF(x + 15, yPos, x + itemWidth - 15, yPos + menuHeight)
            p.color = if (i < 2) Color.parseColor("#333333") else Color.WHITE
            canvas.drawRoundRect(rect, 35f, 35f, p)
            if (currentTool == material && material != Material.RESET) {
                p.style = Paint.Style.STROKE; p.color = Color.YELLOW; p.strokeWidth = 10f
                canvas.drawRoundRect(rect, 35f, 35f, p); p.style = Paint.Style.FILL
            }
            p.color = when(material) {
                Material.SAND -> Color.parseColor("#FFD700"); Material.WATER -> Color.BLUE
                Material.FIRE -> Color.RED; Material.BLUE_FIRE -> Color.CYAN
                Material.GLASS -> Color.LTGRAY; else -> Color.GRAY
            }
            canvas.drawCircle(rect.centerX(), rect.centerY() - 30f, 40f, p)
            p.color = if (i < 2) Color.WHITE else Color.BLACK; p.textSize = 32f; p.textAlign = Paint.Align.CENTER
            canvas.drawText(itemLabels[i], rect.centerX(), rect.bottom - 40f, p)
        }
        canvas.restore()
    }

    private fun checkMenuClick(x: Float, y: Float, real: Boolean): Boolean {
        if (!showMenu || y > 400) return false
        if (!real) return true
        val idx = ((x - scrollOffset) / (width / 3f)).toInt()
        if (idx in menuItems.indices) {
            if (menuItems[idx] == Material.RESET) {
                typeGrid = Array(cols) { Array(rows) { Material.EMPTY } }
                showMenu = false
            } else currentTool = menuItems[idx]
        }
        return true
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_DOWN) {
            lastTouchX = e.x
            if (System.currentTimeMillis() - lastClickTime < 300 && e.y < 400) { showMenu = !showMenu; return true }
            lastClickTime = System.currentTimeMillis()
            if (showMenu && checkMenuClick(e.x, e.y, true)) return true
        } else if (e.action == MotionEvent.ACTION_MOVE && showMenu && e.y < 400) {
            targetScroll += (e.x - lastTouchX)
            targetScroll = targetScroll.coerceIn(-(menuItems.size - 3) * (width / 3f), 0f)
            lastTouchX = e.x; return true
        }
        val id = e.getPointerId(e.actionIndex)
        if (e.actionMasked == MotionEvent.ACTION_DOWN || e.actionMasked == MotionEvent.ACTION_POINTER_DOWN) activeTouches[id] = e.x to e.y
        else if (e.actionMasked == MotionEvent.ACTION_MOVE) { (0 until e.pointerCount).forEach { activeTouches[e.getPointerId(it)] = e.getX(it) to e.getY(it) } }
        else activeTouches.remove(id)
        return true
    }
}