package com.perftop.android.data.repository

import com.perftop.android.data.local.dao.HistoryDao
import com.perftop.android.data.local.entity.HistoryEntity
import com.perftop.android.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val historyDao: HistoryDao
) {
    fun getRecentHistory() = historyDao.getRecentHistory()
        .map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }

    suspend fun getHistoryByHardwareId(hardwareId: Int): HistoryEntry? {
        return historyDao.getHistoryByHardwareId(hardwareId)?.toDomainModel()
    }

    suspend fun addToHistory(hardwareId: Int) {
        val existing = historyDao.getHistoryByHardwareId(hardwareId)
        if (existing != null) {
            historyDao.updateHistoryVisitedAt(hardwareId, System.currentTimeMillis())
        } else {
            historyDao.insertHistory(
                HistoryEntity(hardwareId = hardwareId)
            )
        }
        trimHistory()
    }

    suspend fun removeFromHistory(hardwareId: Int) {
        historyDao.deleteHistoryByHardwareId(hardwareId)
    }

    suspend fun clearAll() {
        historyDao.clearAll()
    }

    suspend fun getCount() = historyDao.getCount()

    private suspend fun trimHistory() {
        historyDao.trimHistory()
    }

    private fun HistoryEntity.toDomainModel() = HistoryEntry(
        id = id,
        hardwareId = hardwareId,
        visitedAt = visitedAt
    )
}
