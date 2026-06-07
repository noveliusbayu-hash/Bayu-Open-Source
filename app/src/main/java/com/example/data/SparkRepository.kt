package com.example.data

import kotlinx.coroutines.flow.Flow

class SparkRepository(private val sparkDao: SparkDao) {
    val allSparks: Flow<List<Spark>> = sparkDao.getAllSparks()

    suspend fun insert(spark: Spark) {
        sparkDao.insertSpark(spark)
    }

    suspend fun delete(id: Int) {
        sparkDao.deleteSparkById(id)
    }

    suspend fun setFavorite(id: Int, isFavorite: Boolean) {
        sparkDao.updateFavoriteStatus(id, isFavorite)
    }
}
