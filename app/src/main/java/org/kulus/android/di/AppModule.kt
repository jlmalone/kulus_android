package org.kulus.android.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.kulus.android.BuildConfig
import org.kulus.android.data.api.AuthInterceptor
import org.kulus.android.data.api.KulusApiService
import org.kulus.android.data.local.GlucoseReadingDao
import org.kulus.android.data.local.KulusDatabase
import org.kulus.android.data.local.TokenStore
import org.kulus.android.data.local.UserProfileDao
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideKulusDatabase(
        @ApplicationContext context: Context
    ): KulusDatabase {
        return Room.databaseBuilder(
            context,
            KulusDatabase::class.java,
            "kulus_database"
        )
            .addMigrations(
                KulusDatabase.MIGRATION_1_2,
                KulusDatabase.MIGRATION_2_3,
                KulusDatabase.MIGRATION_3_4
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideGlucoseReadingDao(database: KulusDatabase): GlucoseReadingDao {
        return database.glucoseReadingDao()
    }

    @Provides
    @Singleton
    fun provideUserProfileDao(database: KulusDatabase): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    @Singleton
    fun provideTokenStore(@ApplicationContext context: Context): TokenStore {
        return TokenStore(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideKulusApiService(retrofit: Retrofit): KulusApiService {
        return retrofit.create(KulusApiService::class.java)
    }
}
