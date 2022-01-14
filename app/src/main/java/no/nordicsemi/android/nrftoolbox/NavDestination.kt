package no.nordicsemi.android.nrftoolbox

sealed class NavDestination

object FinishDestination : NavDestination()

sealed class ForwardDestination : NavDestination() {
    abstract val id: NavigationId
}

object HomeDestination : ForwardDestination() {
    override val id: NavigationId = NavigationId.HOME
}

data class ScannerDestination(val profile: Profile) : ForwardDestination() {
    override val id: NavigationId = NavigationId.SCANNER
}

data class ProfileDestination(
    override val id: NavigationId,
    val isPairingRequired: Boolean
) : ForwardDestination()
