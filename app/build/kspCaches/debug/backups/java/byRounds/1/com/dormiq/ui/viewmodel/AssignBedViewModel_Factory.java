package com.dormiq.ui.viewmodel;

import com.dormiq.domain.repository.BedRepository;
import com.dormiq.domain.repository.FloorRepository;
import com.dormiq.domain.repository.RoomRepository;
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
public final class AssignBedViewModel_Factory implements Factory<AssignBedViewModel> {
  private final Provider<FloorRepository> floorRepositoryProvider;

  private final Provider<RoomRepository> roomRepositoryProvider;

  private final Provider<BedRepository> bedRepositoryProvider;

  public AssignBedViewModel_Factory(Provider<FloorRepository> floorRepositoryProvider,
      Provider<RoomRepository> roomRepositoryProvider,
      Provider<BedRepository> bedRepositoryProvider) {
    this.floorRepositoryProvider = floorRepositoryProvider;
    this.roomRepositoryProvider = roomRepositoryProvider;
    this.bedRepositoryProvider = bedRepositoryProvider;
  }

  @Override
  public AssignBedViewModel get() {
    return newInstance(floorRepositoryProvider.get(), roomRepositoryProvider.get(), bedRepositoryProvider.get());
  }

  public static AssignBedViewModel_Factory create(Provider<FloorRepository> floorRepositoryProvider,
      Provider<RoomRepository> roomRepositoryProvider,
      Provider<BedRepository> bedRepositoryProvider) {
    return new AssignBedViewModel_Factory(floorRepositoryProvider, roomRepositoryProvider, bedRepositoryProvider);
  }

  public static AssignBedViewModel newInstance(FloorRepository floorRepository,
      RoomRepository roomRepository, BedRepository bedRepository) {
    return new AssignBedViewModel(floorRepository, roomRepository, bedRepository);
  }
}
