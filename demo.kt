interface Vehicle {
    fun honk()
    fun getWheels(): Int
}

class Car : Vehicle {

    private val wheels = 4
    private var brandName: String? = null
    var isInAutopilotMode = false

    constructor (brandName: String) {
        this.brandName = brandName
    }

    override fun honk() {
        println("Tuut, tuut!")
    }

    override fun getWheels(): Int {
        return wheels
    }
}

fun main() {
    val car = Car("Mustang")
    car.isInAutopilotMode = true
    car.honk()
}
