/*
 * Copyright (C) 2019 Google Inc.
 *gi
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

package com.example.android.devbyteviewer.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.android.devbyteviewer.database.VideosDatabase
import com.example.android.devbyteviewer.database.asDomainModel
import com.example.android.devbyteviewer.domain.DevByteVideo
import com.example.android.devbyteviewer.network.DevByteNetwork
import com.example.android.devbyteviewer.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for fetching devbyte videos from the network and storing them on disk
 */
class VideosRepository(private val database: VideosDatabase) {

    // Because the getVideos() method returns a list of database objects,
    // and not a list of DevByteVideo objects,
    // use Transformations.map to convert the list of database objects to
    // a list of domain objects using the asDomainModel() conversion function.
    val videos: LiveData<List<DevByteVideo>> = Transformations.map(
        database.videoDao.getVideos() ) { it.asDomainModel() }

    /*
     * Databases on Android are stored on the file system, or disk, and
     * in order to save they must perform a disk I/O operation.
     * Disk I/O, or reading and writing to disk, is slow and always blocks
     * the current thread until the operation is complete.
     * Because of this, you have to run the disk I/O in the I/O dispatcher.
     * This dispatcher is designed to offload blocking I/O tasks to
     * a shared pool of threads using withContext(Dispatchers.IO) { ... }.
     */
    suspend fun refreshVideos() {
        withContext(Dispatchers.IO) {
            val playlist = DevByteNetwork.devbytes.getPlaylist()
            database.videoDao.insertAll( playlist.asDatabaseModel() )
        }
    }

}
