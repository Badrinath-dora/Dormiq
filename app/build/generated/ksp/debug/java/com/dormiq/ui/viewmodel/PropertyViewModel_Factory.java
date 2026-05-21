package com.dormiq.ui.viewmodel;

import com.dormiq.domain.repository.BedRepository;
import com.dormiq.domain.repository.FloorRepository;
import com.dormiq.domain.repository.RoomRepository;
import com.dormiq.domain.repository.TenantRepository;
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
public final class PropertyViewModel_Factory implements Factory<PropertyViewModel> {
  private final Provider<FloorRepository> floorRepositoryProvider;

  private final Provider<RoomRepository> roomRepositoryProvider;

  private final Provider<BedRepository> bedRepositoryProvider;

  private final Provider<TenantRepository> tenantRepositoryProvider;

  public PropertyViewModel_Factory(Provider<FloorRepository> floorRepositoryProvider,
      Provider<RoomRepository> roomRepositoryProvider,
      Provider<BedRepository> bedRepositoryProvider,
      Provider<TenantRepository> tenantRepositoryProvider) {
    this.floorRepositoryProvider = floorRepositoryProvider;
    this.roomRepositoryProvider = roomRepositoryProvider;
    this.bedRepositoryProvider = bedRepositoryProvider;
    this.tenantRepositoryProvider = tenantRepositoryProvider;
  }

  @Override
  public PropertyViewModel get() {
    return newInstance(floorRepositoryProvider.get(), roomRepositoryProvider.get(), bedRepositoryProvider.get(), tenantRepositoryProvider.get());
  }

  public static PropertyViewModel_Factory create(Provider<FloorRepository> floorRepositoryProvider,
      Provider<RoomRepository> roomRepositoryProvider,
      Provider<BedRepository> bedRepositoryProvider,
      Provider<TenantRepository> tenantRepositoryProvider) {
    return new PropertyViewModel_Factory(floorRepositoryProvider, roomRepositoryProvider, bedRepositoryProvider, tenantRepositoryProvider);
  }

  public static PropertyViewModel newInstance(FloorRepository floorRepository,
      RoomRepository roomRepository, BedRepository bedRepository,
      TenantRepository tenantRepository) {
    return new PropertyViewModel(floorRepository, roomRepository, bedRepository, tenantRepository);
  }
}
