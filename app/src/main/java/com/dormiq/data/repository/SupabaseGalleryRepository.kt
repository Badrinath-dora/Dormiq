package com.dormiq.data.repository

import com.dormiq.domain.model.HostelGallery
import com.dormiq.domain.repository.GalleryRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

import io.github.jan.supabase.storage.storage

@Singleton
class SupabaseGalleryRepository @Inject constructor(
    private val supabase: SupabaseClient
) : GalleryRepository {

    private val _images = MutableStateFlow<List<HostelGallery>>(emptyList())

    override fun getGalleryImages(hostelId: String): Flow<List<HostelGallery>> = flow {
        try {
            val response = supabase.postgrest["hostel_gallery"]
                .select { filter { eq("hostel_id", hostelId) } }
                .decodeList<HostelGallery>()
            _images.value = response
            emit(response)
        } catch (e: Exception) {
            e.printStackTrace()
            emit(_images.value.filter { it.hostelId == hostelId })
        }
    }

    override suspend fun addGalleryImage(gallery: HostelGallery): Result<HostelGallery> {
        return try {
            val inserted = supabase.postgrest["hostel_gallery"]
                .insert(gallery) { select() }
                .decodeSingle<HostelGallery>()
            _images.value = _images.value + inserted
            Result.success(inserted)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteGalleryImage(imageId: String): Result<Unit> {
        return try {
            supabase.postgrest["hostel_gallery"].delete {
                filter { eq("id", imageId) }
            }
            _images.value = _images.value.filter { it.id != imageId }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun uploadHostelImage(fileName: String, byteArray: ByteArray): String {
        val bucket = supabase.storage["hostel-gallery"]
        bucket.upload(fileName, byteArray) {
            upsert = true
        }
        return bucket.publicUrl(fileName)
    }
}
