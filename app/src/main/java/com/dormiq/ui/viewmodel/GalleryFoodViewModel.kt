package com.dormiq.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dormiq.domain.model.FoodMenu
import com.dormiq.domain.model.HostelGallery
import com.dormiq.domain.repository.FoodMenuRepository
import com.dormiq.domain.repository.GalleryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryFoodViewModel @Inject constructor(
    private val galleryRepository: GalleryRepository,
    private val foodMenuRepository: FoodMenuRepository
) : ViewModel() {

    private val _galleryImages = MutableStateFlow<List<HostelGallery>>(emptyList())
    val galleryImages: StateFlow<List<HostelGallery>> = _galleryImages.asStateFlow()

    private val _foodMenu = MutableStateFlow<List<FoodMenu>>(emptyList())
    val foodMenu: StateFlow<List<FoodMenu>> = _foodMenu.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadGallery(hostelId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            galleryRepository.getGalleryImages(hostelId).collect {
                _galleryImages.value = it
                _isLoading.value = false
            }
        }
    }

    fun addGalleryImage(hostelId: String, imageUrl: String, caption: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val gallery = HostelGallery(
                hostelId = hostelId,
                imageUrl = imageUrl,
                caption = caption
            )
            galleryRepository.addGalleryImage(gallery).onSuccess {
                loadGallery(hostelId)
            }
            _isLoading.value = false
        }
    }

    fun uploadAndAddGalleryImage(
        context: android.content.Context,
        hostelId: String,
        uri: android.net.Uri,
        caption: String,
        onSuccess: () -> Unit = {},
        onFailure: (Throwable) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val compressedBytes = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    com.dormiq.util.ImageCompressor.compressUri(context, uri)
                }
                if (compressedBytes == null) {
                    onFailure(Exception("Failed to compress image"))
                    _isLoading.value = false
                    return@launch
                }

                val fileName = "gallery/${hostelId}/${java.util.UUID.randomUUID()}.jpg"
                val publicUrl = galleryRepository.uploadHostelImage(fileName, compressedBytes)

                val gallery = HostelGallery(
                    hostelId = hostelId,
                    imageUrl = publicUrl,
                    caption = caption
                )

                galleryRepository.addGalleryImage(gallery).onSuccess {
                    loadGallery(hostelId)
                    onSuccess()
                }.onFailure {
                    onFailure(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onFailure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteGalleryImage(hostelId: String, imageId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            galleryRepository.deleteGalleryImage(imageId).onSuccess {
                loadGallery(hostelId)
            }
            _isLoading.value = false
        }
    }

    fun loadFoodMenu(hostelId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            foodMenuRepository.getFoodMenu(hostelId).collect {
                _foodMenu.value = it
                _isLoading.value = false
            }
        }
    }

    fun updateDailyMenu(
        hostelId: String,
        dayOfWeek: String,
        breakfast: String,
        lunch: String,
        snacks: String,
        dinner: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val existing = _foodMenu.value.find { it.dayOfWeek.equals(dayOfWeek, ignoreCase = true) }
            val menu = FoodMenu(
                id = existing?.id ?: "",
                hostelId = hostelId,
                dayOfWeek = dayOfWeek,
                breakfast = breakfast,
                lunch = lunch,
                snacks = snacks,
                dinner = dinner
            )
            foodMenuRepository.updateDailyMenu(menu).onSuccess {
                loadFoodMenu(hostelId)
            }
            _isLoading.value = false
        }
    }
}
