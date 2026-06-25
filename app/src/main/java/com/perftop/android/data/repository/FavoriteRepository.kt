package com.perftop.android.data.repository

import com.perftop.android.data.local.dao.FavoriteDao
import com.perftop.android.data.local.entity.FavoriteEntity
import com.perftop.android.domain.model.Favorite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val favoriteDao: FavoriteDao
) {
    fun getAllFavorites() = favoriteDao.getAllFavorites()
        .map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }

    fun getFavoritesByGroup(groupName: String) = favoriteDao.getFavoritesByGroup(groupName)
        .map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }

    suspend fun getAllGroups() = favoriteDao.getAllGroups()

    suspend fun isFavorite(hardwareId: Int): Boolean {
        return favoriteDao.getFavoriteByHardwareId(hardwareId) != null
    }

    suspend fun addFavorite(hardwareId: Int, groupName: String = "默认") {
        favoriteDao.insertFavorite(
            FavoriteEntity(
                hardwareId = hardwareId,
                groupName = groupName
            )
        )
    }

    suspend fun removeFavorite(hardwareId: Int) {
        favoriteDao.deleteFavoriteByHardwareId(hardwareId)
    }

    suspend fun removeFavoritesByGroup(groupName: String) {
        favoriteDao.deleteFavoritesByGroup(groupName)
    }

    suspend fun clearAll() {
        favoriteDao.clearAll()
    }

    private fun FavoriteEntity.toDomainModel() = Favorite(
        id = id,
        hardwareId = hardwareId,
        groupName = groupName,
        createdAt = createdAt
    )
}
