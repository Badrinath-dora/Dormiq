package com.dormiq.ui.viewmodel;

import com.dormiq.domain.repository.FoodMenuRepository;
import com.dormiq.domain.repository.GalleryRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class GalleryFoodViewModel_Factory implements Factory<GalleryFoodViewModel> {
  private final Provider<GalleryRepository> galleryRepositoryProvider;

  private final Provider<FoodMenuRepository> foodMenuRepositoryProvider;

  public GalleryFoodViewModel_Factory(Provider<GalleryRepository> galleryRepositoryProvider,
      Provider<FoodMenuRepository> foodMenuRepositoryProvider) {
    this.galleryRepositoryProvider = galleryRepositoryProvider;
    this.foodMenuRepositoryProvider = foodMenuRepositoryProvider;
  }

  @Override
  public GalleryFoodViewModel get() {
    return newInstance(galleryRepositoryProvider.get(), foodMenuRepositoryProvider.get());
  }

  public static GalleryFoodViewModel_Factory create(
      Provider<GalleryRepository> galleryRepositoryProvider,
      Provider<FoodMenuRepository> foodMenuRepositoryProvider) {
    return new GalleryFoodViewModel_Factory(galleryRepositoryProvider, foodMenuRepositoryProvider);
  }

  public static GalleryFoodViewModel newInstance(GalleryRepository galleryRepository,
      FoodMenuRepository foodMenuRepository) {
    return new GalleryFoodViewModel(galleryRepository, foodMenuRepository);
  }
}
