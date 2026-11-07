package com.unimal.uas_240170129_muhammad_raihann_azhari.util

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/** Thread pool untuk akses database / pemrosesan gambar di luar main thread. */
object AppExecutors {
    val io: ExecutorService = Executors.newFixedThreadPool(2)
}
