package agenda.de.myapplicationanim1.data.remote

import android.content.Context

object RetrofitClient {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }
}
