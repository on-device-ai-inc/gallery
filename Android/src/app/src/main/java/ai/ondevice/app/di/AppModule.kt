/*
 * Copyright 2025 OnDevice Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.ondevice.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.dataStoreFile
import ai.ondevice.app.AppLifecycleProvider
import ai.ondevice.app.GalleryLifecycleProvider
import ai.ondevice.app.SettingsSerializer
import ai.ondevice.app.UserDataSerializer
import ai.ondevice.app.data.DataStoreRepository
import ai.ondevice.app.data.DefaultDataStoreRepository
import ai.ondevice.app.data.DefaultDownloadRepository
import ai.ondevice.app.data.DownloadRepository
import ai.ondevice.app.data.SecureTokenStorage
import ai.ondevice.app.proto.Settings
import ai.ondevice.app.proto.UserData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import androidx.room.Room
import ai.ondevice.app.data.AppDatabase
import ai.ondevice.app.data.ConversationDao
import ai.ondevice.app.data.ALL_MIGRATIONS

@Module
@InstallIn(SingletonComponent::class)
internal object AppModule {

  // Provides the SettingsSerializer
  @Provides
  @Singleton
  fun provideSettingsSerializer(): Serializer<Settings> {
    return SettingsSerializer
  }

  // Provides the UserDataSerializer
  @Provides
  @Singleton
  fun provideUserDataSerializer(): Serializer<UserData> {
    return UserDataSerializer
  }

  // Provides DataStore<Settings>
  @Provides
  @Singleton
  fun provideSettingsDataStore(
    @ApplicationContext context: Context,
    settingsSerializer: Serializer<Settings>,
  ): DataStore<Settings> {
    return DataStoreFactory.create(
      serializer = settingsSerializer,
      produceFile = { context.dataStoreFile("settings.pb") },
    )
  }

  // Provides DataStore<UserData>
  @Provides
  @Singleton
  fun provideUserDataDataStore(
    @ApplicationContext context: Context,
    userDataSerializer: Serializer<UserData>,
  ): DataStore<UserData> {
    return DataStoreFactory.create(
      serializer = userDataSerializer,
      produceFile = { context.dataStoreFile("user_data.pb") },
    )
  }

  // Provides AppLifecycleProvider
  @Provides
  @Singleton
  fun provideAppLifecycleProvider(): AppLifecycleProvider {
    return GalleryLifecycleProvider()
  }

  // Provides SecureTokenStorage
  @Provides
  @Singleton
  fun provideSecureTokenStorage(@ApplicationContext context: Context): SecureTokenStorage {
    return SecureTokenStorage(context)
  }

  // Provides DataStoreRepository
  @Provides
  @Singleton
  fun provideDataStoreRepository(
    dataStore: DataStore<Settings>,
    userDataDataStore: DataStore<UserData>,
    secureTokenStorage: SecureTokenStorage,
  ): DataStoreRepository {
    return DefaultDataStoreRepository(dataStore, userDataDataStore, secureTokenStorage)
  }

  // Provides DownloadRepository
  @Provides
  @Singleton
  fun provideDownloadRepository(
    @ApplicationContext context: Context,
    lifecycleProvider: AppLifecycleProvider,
  ): DownloadRepository {
    return DefaultDownloadRepository(context, lifecycleProvider)
  }

  @Provides
  @Singleton
  fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
    return Room.databaseBuilder(
      context,
      AppDatabase::class.java,
      "ondevice_database"
    )
      .addMigrations(*ALL_MIGRATIONS)
      .build()
  }

  @Provides
  @Singleton
  fun provideConversationDao(database: AppDatabase): ConversationDao {
    return database.conversationDao()
  }

  // Prompt Engineering - Phase 1: Persona Management
  @Provides
  @Singleton
  fun providePersonaManager(): ai.ondevice.app.persona.PersonaManager {
    return ai.ondevice.app.persona.PersonaManager()
  }
}
