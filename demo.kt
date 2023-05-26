protocol Vehicle {
    func honk()
    func getWheels() -> Int
}

class Car: Vehicle {
    
    private let wheels = 4
    private var brandName: String? = nil
    var isInAutopilotMode = false
    
    init (brandName: String) {
        self.brandName = brandName
    }
    
    func honk(){
        print("Tuut, tuut!")
    }
    
    func getWheels() -> Int {
        return wheels
    }
}

func main() {
    let car = Car(brandName: "Mustang")
    car.isInAutopilotMode = true
    car.honk()
}

