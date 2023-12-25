package zuper.dev.android.dashboard.data.model

sealed class Screen(var route: String){
    object MainScreen: Screen("main_screen")
    object JobScreen: Screen("job_screen")

    fun withArg(vararg args: String): String{
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }

}
