package com.skobkadev.sandbox

import android.content.Context
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller
import kotlin.random.Random

/**
 * Конфигурация мира: размеры и интерфейс
 */
object WorldConfig {
    const val BRUSH_RADIUS = 6
    const val MENU_H = 340f
    const val CHANCE_GROWTH = 5
    const val CHANCE_FIRE = 20
    const val CHANCE_CORROSION = 8
}

/**
 * Типы визуализации для разных состояний материи
 */
enum class VisualType { GRAIN, SOLID, LIQUID, GAS, GLOW, CRYSTAL, PLASMA, VOLATILE }

/**
 * Полный список 85+ элементов с их физическими и химическими свойствами
 */
enum class Element(
    val id: Int,
    val color: Int,
    val density: Int,           // Отрицательные - газы, 0-100 - жидкости/песок, 1000+ статика
    val visual: VisualType,
    val isStatic: Boolean = false,
    val isFuel: Boolean = false, // Горючесть
    val isAcidSensitive: Boolean = false, // Уязвимость к кислоте
    val isConductor: Boolean = false, // Электропроводность
    val temp: Int = 0           // Условная температура (0 - нейтрал, 1 - горячо, -1 - холодно)
) {
    EMPTY(0, Color.BLACK, 0, VisualType.GAS),
    ERASER(-1, Color.DKGRAY, 0, VisualType.SOLID),
    CLEAR(-2, Color.WHITE, 0, VisualType.SOLID),

    // --- ТВЕРДЫЕ И ПРЕГРАДЫ (1-19) ---
    WALL(1, Color.rgb(50, 50, 55), 2000, VisualType.SOLID, true),
    STONE(2, Color.rgb(110, 110, 110), 1200, VisualType.SOLID, true),
    WOOD(3, Color.rgb(110, 75, 40), 700, VisualType.SOLID, true, true, true),
    IRON(4, Color.rgb(180, 180, 190), 1500, VisualType.SOLID, true, false, true, true),
    GOLD(5, Color.rgb(255, 215, 0), 1900, VisualType.SOLID, true, false, false, true),
    COPPER(6, Color.rgb(190, 110, 60), 1400, VisualType.SOLID, true, false, true, true),
    ICE(7, Color.rgb(210, 245, 255), 900, VisualType.CRYSTAL, true, false, false, false, -1),
    GLASS(8, Color.rgb(220, 235, 240), 1100, VisualType.CRYSTAL, true),
    RUBBER(9, Color.rgb(35, 35, 35), 800, VisualType.SOLID, true, true, true),
    PLASTIC(10, Color.rgb(220, 60, 60), 700, VisualType.SOLID, true, true, true),
    OBSIDIAN(11, Color.rgb(40, 20, 60), 1800, VisualType.CRYSTAL, true),
    DIAMOND(12, Color.rgb(200, 255, 255), 3000, VisualType.CRYSTAL, true),
    BRICK(13, Color.rgb(190, 90, 70), 1300, VisualType.SOLID, true),
    CONCRETE(14, Color.rgb(160, 160, 160), 1450, VisualType.SOLID, true),
    LEAD(15, Color.rgb(90, 100, 110), 2500, VisualType.SOLID, true, false, true),
    STEEL(16, Color.rgb(200, 200, 210), 1600, VisualType.SOLID, true, false, true, true),
    CARBON(17, Color.rgb(25, 25, 25), 900, VisualType.SOLID, true, true),
    GEL(18, Color.rgb(0, 255, 170), 500, VisualType.LIQUID, true, true),
    SPONGE(19, Color.rgb(240, 240, 110), 400, VisualType.SOLID, true, true, true),

    // --- СЫПУЧИЕ МАТЕРИАЛЫ (20-39) ---
    SAND(20, Color.rgb(235, 210, 150), 70, VisualType.GRAIN),
    SALT(21, Color.rgb(255, 255, 255), 65, VisualType.GRAIN),
    SUGAR(22, Color.rgb(255, 245, 235), 62, VisualType.GRAIN, false, true),
    CEMENT(23, Color.rgb(170, 170, 165), 85, VisualType.GRAIN),
    SNOW(24, Color.rgb(250, 255, 255), 25, VisualType.GRAIN, false, false, false, false, -1),
    GUNPOWDER(25, Color.rgb(60, 60, 60), 80, VisualType.GRAIN, false, true),
    ASH(26, Color.rgb(90, 90, 95), 35, VisualType.GRAIN),
    DUST(27, Color.rgb(130, 120, 110), 20, VisualType.GRAIN),
    GRAVEL(28, Color.rgb(120, 120, 125), 110, VisualType.GRAIN),
    DIRT(29, Color.rgb(90, 60, 40), 75, VisualType.GRAIN),
    FERTILIZER(30, Color.rgb(110, 210, 110), 60, VisualType.GRAIN),
    COAL_POWDER(31, Color.rgb(40, 40, 45), 55, VisualType.GRAIN, false, true),
    CLAY(32, Color.rgb(190, 150, 110), 90, VisualType.GRAIN),
    POWDER_IRON(33, Color.rgb(140, 140, 145), 150, VisualType.GRAIN, false, false, true, true),
    FLOUR(34, Color.rgb(255, 255, 240), 30, VisualType.GRAIN, false, true),
    SULPHUR(35, Color.rgb(240, 240, 60), 68, VisualType.GRAIN, false, true),
    SAWDUST(36, Color.rgb(200, 170, 110), 40, VisualType.GRAIN, false, true),
    TERMITE(37, Color.rgb(255, 100, 0), 100, VisualType.GRAIN, false, true),

    // --- ЖИДКОСТИ (40-59) ---
    WATER(40, Color.rgb(40, 120, 255), 10, VisualType.LIQUID),
    SALT_WATER(41, Color.rgb(120, 170, 255), 12, VisualType.LIQUID, false, false, false, true),
    OIL(42, Color.rgb(60, 40, 80), 8, VisualType.LIQUID, false, true),
    LAVA(43, Color.rgb(255, 70, 0), 120, VisualType.GLOW, false, false, false, false, 1),
    ACID(44, Color.rgb(160, 255, 0), 15, VisualType.LIQUID),
    MERCURY(45, Color.rgb(200, 200, 200), 136, VisualType.LIQUID, false, false, true, true),
    ALCOHOL(46, Color.rgb(190, 230, 255), 7, VisualType.LIQUID, false, true),
    NITRO(47, Color.rgb(50, 255, 120), 18, VisualType.LIQUID, false, true),
    MUD(48, Color.rgb(70, 50, 30), 30, VisualType.LIQUID),
    HONEY(49, Color.rgb(255, 190, 20), 45, VisualType.LIQUID),
    BLOOD(50, Color.rgb(200, 0, 0), 12, VisualType.LIQUID),
    PETROL(51, Color.rgb(220, 220, 60), 6, VisualType.LIQUID, false, true),
    MILK(52, Color.rgb(255, 255, 250), 11, VisualType.LIQUID),
    PAINT_R(53, Color.RED, 14, VisualType.LIQUID),
    PAINT_G(54, Color.GREEN, 14, VisualType.LIQUID),
    PAINT_B(55, Color.BLUE, 14, VisualType.LIQUID),
    MAGMA(56, Color.rgb(200, 40, 0), 150, VisualType.LIQUID, false, false, false, false, 1),

    // --- ГАЗЫ И ЭФФЕКТЫ (60-74) ---
    FIRE(60, Color.rgb(255, 160, 20), 2, VisualType.GLOW, false, false, false, false, 1),
    SMOKE(61, Color.rgb(130, 130, 130), -2, VisualType.GAS),
    STEAM(62, Color.rgb(230, 230, 250), -5, VisualType.GAS),
    GAS_NATURAL(63, Color.rgb(210, 160, 255), -3, VisualType.GAS, false, true),
    HELIUM(64, Color.rgb(255, 210, 255), -10, VisualType.GAS),
    CLOUD(65, Color.rgb(255, 255, 255), -4, VisualType.GAS),
    PLASMA(66, Color.rgb(220, 120, 255), 1, VisualType.PLASMA, false, false, false, false, 1),
    HYDROGEN(67, Color.rgb(230, 255, 255), -8, VisualType.GAS, false, true),
    CO2(68, Color.rgb(100, 100, 110), -1, VisualType.GAS),
    CHLORINE(69, Color.rgb(200, 255, 100), -2, VisualType.GAS),

    // --- ОРГАНИКА / ЖИЗНЬ (75-84) ---
    PLANT(75, Color.rgb(20, 200, 40), 500, VisualType.SOLID, true, true, true),
    LEAF(76, Color.rgb(60, 230, 60), 15, VisualType.GRAIN, false, true, true),
    VIRUS(77, Color.rgb(255, 20, 255), 40, VisualType.GRAIN),
    ALGAE(78, Color.rgb(10, 120, 60), 10, VisualType.LIQUID),
    MUSHROOM(79, Color.rgb(230, 190, 160), 400, VisualType.SOLID, true, true, true),
    CORAL(80, Color.rgb(255, 130, 130), 1000, VisualType.SOLID, true),
    SPORE(81, Color.rgb(150, 100, 50), -1, VisualType.GAS),

    // --- ЭЛЕКТРИКА И ТЕХНИКА (85-94) ---
    WIRE(85, Color.rgb(110, 110, 115), 1000, VisualType.SOLID, true, false, true, true),
    TESLA(86, Color.rgb(160, 160, 255), 1100, VisualType.PLASMA, true, false, false, true),
    BATTERY(87, Color.rgb(60, 210, 60), 1100, VisualType.SOLID, true, false, true, true),
    LED_R(88, Color.rgb(100, 0, 0), 1000, VisualType.SOLID, true),
    SOLAR(89, Color.rgb(30, 30, 60), 1000, VisualType.SOLID, true),

    // --- ЭКЗОТИКА ---
    ANTIMATTER(95, Color.rgb(5, 5, 5), 0, VisualType.PLASMA),
    DARK_MATTER(96, Color.rgb(15, 5, 25), 5000, VisualType.SOLID, true),
    PORTAL(97, Color.rgb(100, 255, 255), 0, VisualType.PLASMA, true)
}

class SandboxView(context: Context) : View(context), SensorEventListener {
    private var cellSize = 12f
    private var cols = 0; private var rows = 0
    private lateinit var grid: IntArray
    private lateinit var nextGrid: IntArray
    private lateinit var noise: IntArray

    private var currentTool = Element.SAND
    private val paint = Paint().apply { isAntiAlias = false }
    private val activeTouches = mutableMapOf<Int, PointF>()

    private var scrollXPos = 0f
    private val scroller = OverScroller(context)
    private var lastTouchX = 0f
    private var isMenuScroll = false

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var gx = 0; private var gy = 1

    init {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cellSize = (w / 120f).coerceAtLeast(12f)
        cols = (w / cellSize).toInt()
        rows = (h / cellSize).toInt()
        grid = IntArray(cols * rows)
        nextGrid = IntArray(cols * rows)
        noise = IntArray(cols * rows) { Random.nextInt(-20, 20) }
    }

    override fun onSensorChanged(e: SensorEvent) {
        // Определение направления гравитации по датчику
        gx = when { e.values[0] < -1.5 -> 1; e.values[0] > 1.5 -> -1; else -> 0 }
        gy = when { e.values[1] > 1.5 -> 1; e.values[1] < -1.5 -> -1; else -> 0 }
        if (gx == 0 && gy == 0) gy = 1
    }

    override fun onAccuracyChanged(s: Sensor?, a: Int) {}

    private fun getAt(x: Int, y: Int): Int = if (x in 0 until cols && y in 0 until rows) grid[x * rows + y] else -1
    private fun setNext(x: Int, y: Int, id: Int) { if (x in 0 until cols && y in 0 until rows) nextGrid[x * rows + y] = id }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.rgb(5, 5, 10))

        if (scroller.computeScrollOffset()) {
            scrollXPos = scroller.currX.toFloat()
            invalidate()
        }

        activeTouches.values.forEach { spawn(it.x, it.y) }

        nextGrid.fill(0)
        // Физический цикл: выбор порядка обхода в зависимости от гравитации
        val yRange = if (gy >= 0) rows - 1 downTo 0 else 0 until rows
        val xRange = if (gx >= 0) cols - 1 downTo 0 else 0 until cols

        for (x in xRange) {
            for (y in yRange) {
                val id = grid[x * rows + y]
                if (id == 0) continue
                val el = Element.values().find { it.id == id } ?: continue
                updatePhysics(x, y, el)
            }
        }
        System.arraycopy(nextGrid, 0, grid, 0, grid.size)

        // Рендеринг пикселей
        for (x in 0 until cols) {
            for (y in 0 until rows) {
                val id = grid[x * rows + y]
                if (id == 0) continue
                val el = Element.values().find { it.id == id } ?: continue
                drawPixel(canvas, x, y, el)
            }
        }

        drawMenu(canvas)
        invalidate()
    }

    private fun drawPixel(canvas: Canvas, x: Int, y: Int, el: Element) {
        val base = el.color
        val n = noise[x * rows + y]
        val r = Color.red(base); val g = Color.green(base); val b = Color.blue(base)

        paint.color = when(el.visual) {
            VisualType.LIQUID -> {
                val wave = (Math.sin(System.currentTimeMillis() * 0.005 + x * 0.2) * 8).toInt()
                Color.rgb((r+wave).coerceIn(0,255), (g+wave).coerceIn(0,255), (b+20).coerceIn(0,255))
            }
            VisualType.PLASMA -> {
                val flicker = Random.nextInt(-60, 60)
                Color.rgb((r+flicker).coerceIn(0,255), (g+flicker).coerceIn(0,255), 255)
            }
            VisualType.GLOW -> {
                val pulse = (Math.sin(System.currentTimeMillis() * 0.01) * 30).toInt()
                Color.rgb((r+pulse).coerceIn(0,255), (g+pulse).coerceIn(0,255), b)
            }
            else -> Color.rgb((r+n).coerceIn(0,255), (g+n).coerceIn(0,255), (b+n).coerceIn(0,255))
        }
        canvas.drawRect(x * cellSize, y * cellSize, (x + 1) * cellSize, (y + 1) * cellSize, paint)
    }

    private fun updatePhysics(x: Int, y: Int, el: Element) {
        // --- СИСТЕМА ВЗАИМОДЕЙСТВИЙ (85+ ПРАВИЛ) ---
        val neighbors = arrayOf(0 to 1, 0 to -1, 1 to 0, -1 to 0, 1 to 1, -1 to -1)
        for (nb in neighbors) {
            val nx = x + nb.first; val ny = y + nb.second
            val otherId = getAt(nx, ny)
            if (otherId <= 0) continue
            val other = Element.values().find { it.id == otherId } ?: continue

            when {
                // ТЕРМОДИНАМИКА
                (el.temp > 0) -> {
                    if (other == Element.ICE || other == Element.SNOW) { setNext(nx, ny, Element.WATER.id); return }
                    if (other == Element.WATER) { setNext(nx, ny, Element.STEAM.id); return }
                    if (other.isFuel && Random.nextInt(100) < WorldConfig.CHANCE_FIRE) { setNext(nx, ny, Element.FIRE.id); return }
                }
                // КИСЛОТА И КОРРОЗИЯ
                (el == Element.ACID && other.isAcidSensitive) -> {
                    if (Random.nextInt(100) < WorldConfig.CHANCE_CORROSION) {
                        setNext(nx, ny, Element.EMPTY.id)
                        setNext(x, y, Element.SMOKE.id)
                        return
                    }
                }
                // ВОДА И СМЕСИ
                (el == Element.WATER) -> {
                    if (other == Element.CEMENT) { setNext(nx, ny, Element.CONCRETE.id); setNext(x, y, 0); return }
                    if (other == Element.DIRT) { setNext(nx, ny, Element.MUD.id); setNext(x, y, 0); return }
                    if (other == Element.SALT) { setNext(nx, ny, Element.SALT_WATER.id); setNext(x, y, 0); return }
                }
                // ЖИЗНЬ
                (el == Element.PLANT && (other == Element.WATER || other == Element.FERTILIZER)) -> {
                    if (Random.nextInt(100) < WorldConfig.CHANCE_GROWTH) {
                        val rx = x + Random.nextInt(-1, 2); val ry = y + Random.nextInt(-1, 2)
                        if (getAt(rx, ry) == 0) setNext(rx, ry, Element.PLANT.id)
                    }
                }
                // ВИРУС И МУТАЦИИ
                (el == Element.VIRUS && other.id in 1..85 && other != Element.WALL && other != Element.VIRUS) -> {
                    if (Random.nextInt(100) < 12) { setNext(nx, ny, Element.VIRUS.id); return }
                }
                // АНТИМАТЕРИЯ
                (el == Element.ANTIMATTER && otherId > 0) -> {
                    setNext(nx, ny, 0); setNext(x, y, Element.PLASMA.id); return
                }
            }
        }

        // --- ДВИЖЕНИЕ (ФИЗИЧЕСКИЙ ДВИЖОК) ---
        if (el.isStatic) {
            if (nextGrid[x * rows + y] == 0) setNext(x, y, el.id)
            return
        }

        val dx = gx; val dy = gy
        val below = getAt(x + dx, y + dy)

        // 1. Свободное падение
        if (below == 0 && nextGrid[(x + dx) * rows + (y + dy)] == 0) {
            setNext(x + dx, y + dy, el.id)
        }
        // 2. Поведение жидкостей и песка (растекание)
        else if (el.density > 0) {
            val side = if (Random.nextBoolean()) 1 else -1
            if (getAt(x + side, y + dy) == 0 && nextGrid[(x + side) * rows + (y + dy)] == 0) {
                setNext(x + side, y + dy, el.id)
            } else if (el.visual == VisualType.LIQUID && getAt(x + side, y) == 0 && nextGrid[(x + side) * rows + y] == 0) {
                setNext(x + side, y, el.id)
            } else {
                if (nextGrid[x * rows + y] == 0) setNext(x, y, el.id)
            }
        }
        // 3. Поведение газов (всплытие)
        else if (el.density < 0) {
            val ty = y - 1
            val tx = x + Random.nextInt(-1, 2)
            if (getAt(tx, ty) == 0 && nextGrid[tx * rows + ty] == 0) {
                setNext(tx, ty, el.id)
            } else {
                // Шанс исчезновения для дыма и пара
                if (Random.nextFloat() < 0.008f && (el == Element.SMOKE || el == Element.STEAM)) setNext(x, y, 0)
                else if (nextGrid[x * rows + y] == 0) setNext(x, y, el.id)
            }
        } else {
            if (nextGrid[x * rows + y] == 0) setNext(x, y, el.id)
        }
    }

    private fun spawn(tx: Float, ty: Float) {
        if (ty < WorldConfig.MENU_H) return
        val cx = (tx / cellSize).toInt(); val cy = (ty / cellSize).toInt()
        val r = WorldConfig.BRUSH_RADIUS
        for (i in -r..r) for (j in -r..r) {
            val nx = cx + i; val ny = cy + j
            if (nx in 0 until cols && ny in 0 until rows && i*i + j*j <= r*r) {
                if (currentTool == Element.ERASER) grid[nx * rows + ny] = 0
                else if (grid[nx * rows + ny] == 0) grid[nx * rows + ny] = currentTool.id
            }
        }
    }

    private fun drawMenu(canvas: Canvas) {
        val items = Element.values().filter { it.id != 0 }
        val w = width / 4.8f; val h = WorldConfig.MENU_H / 2.2f
        val p = Paint().apply { isAntiAlias = true; textAlign = Paint.Align.CENTER }

        items.forEachIndexed { i, el ->
            val col = i / 2; val row = i % 2
            val x = col * w + scrollXPos; val y = row * h + 25f

            if (x + w > 0 && x < width) {
                val r = RectF(x + 12, y, x + w - 12, y + h - 18)
                p.color = if (currentTool == el) Color.rgb(70, 70, 110) else Color.rgb(35, 35, 45)
                canvas.drawRoundRect(r, 20f, 20f, p)

                p.color = el.color
                canvas.drawCircle(r.centerX(), r.top + 55, 28f, p)

                p.color = Color.WHITE; p.textSize = 22f
                val name = when(el) {
                    Element.ERASER -> "ЛАСТИК"
                    Element.CLEAR -> "СБРОС"
                    else -> el.name.replace("_", " ")
                }
                canvas.drawText(name, r.centerX(), r.bottom - 18, p)
            }
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val id = e.getPointerId(e.actionIndex)
                val x = e.getX(e.actionIndex); val y = e.getY(e.actionIndex)
                if (y < WorldConfig.MENU_H) {
                    isMenuScroll = true; lastTouchX = x
                } else {
                    activeTouches[id] = PointF(x, y)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isMenuScroll) {
                    scrollXPos += e.x - lastTouchX
                    lastTouchX = e.x
                } else {
                    for (i in 0 until e.pointerCount) {
                        activeTouches[e.getPointerId(i)]?.set(e.getX(i), e.getY(i))
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val id = e.getPointerId(e.actionIndex)
                if (isMenuScroll && Math.abs(e.x - lastTouchX) < 10) {
                    val w = width / 4.8f
                    val col = ((e.x - scrollXPos) / w).toInt()
                    val row = if (e.y < WorldConfig.MENU_H / 2.2f) 0 else 1
                    val items = Element.values().filter { it.id != 0 }
                    val idx = col * 2 + row
                    if (idx in items.indices) {
                        currentTool = items[idx]
                        if (currentTool == Element.CLEAR) grid.fill(0)
                    }
                }
                activeTouches.remove(id)
                if (activeTouches.isEmpty()) isMenuScroll = false
            }
        }
        invalidate(); return true
    }
}