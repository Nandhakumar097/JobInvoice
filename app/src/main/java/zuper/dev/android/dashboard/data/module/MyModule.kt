package zuper.dev.android.dashboard.data.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import zuper.dev.android.dashboard.data.DataRepository
import zuper.dev.android.dashboard.data.remote.ApiDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MyModule {

    @Singleton
    @Provides
    fun getRepository(apiDataSource: ApiDataSource) : DataRepository{
        return DataRepository(apiDataSource)
    }

    @Singleton
    @Provides
    fun getApiDataSource() : ApiDataSource{
        return ApiDataSource()
    }


}