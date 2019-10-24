package pl.mateuszkalinowski.robotremotecontroller.model

class BluetoothListElement(val deviceName: String, val deviceMacAddress: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BluetoothListElement

        if (deviceMacAddress != other.deviceMacAddress) return false

        return true
    }

    override fun hashCode(): Int {
        return deviceMacAddress.hashCode()
    }
}