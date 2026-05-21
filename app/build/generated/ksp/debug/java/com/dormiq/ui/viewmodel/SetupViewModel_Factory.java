package com.dormiq.ui.viewmodel;

import com.dormiq.domain.repository.AuthRepository;
import com.dormiq.domain.repository.BedRepository;
import com.dormiq.domain.repository.FloorRepository;
import com.dormiq.domain.repository.HostelRepository;
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
public final class SetupViewModel_Factory implements Factory<SetupViewModel> {
  private final Provider<HostelRepository> hostelRepositoryProvider;

  private final Provider<FloorRepository> floorRepositoryProvider;

  private final Provider<RoomRepository> roomRepositoryProvider;

  private final Provider<BedRepository> bedRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public SetupViewModel_Factory(Provider<HostelRepository> hostelRepositoryProvider,
      Provider<FloorRepository> floorRepositoryProvider,
      Provider<RoomRepository> roomRepositoryProvider,
      Provider<BedRepository> bedRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.hostelRepositoryProvider = hostelRepositoryProvider;
    this.floorRepositoryProvider = floorRepositoryProvider;
    this.roomRepositoryProvider = roomRepositoryProvider;
    this.bedRepositoryProvider = bedRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public SetupViewModel get() {
    return newInstance(hostelRepositoryProvider.get(), floorRepositoryProvider.get(), roomRepositoryProvider.get(), bedRepositoryProvider.get(), authRepositoryProvider.get());
  }

  public static SetupViewModel_Factory create(Provider<HostelRepository> hostelRepositoryProvider,
      Provider<FloorRepository> floorRepositoryProvider,
      Provider<RoomRepository> roomRepositoryProvider,
      Provider<BedRepository> bedRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new SetupViewModel_Factory(hostelRepositoryProvider, floorRepositoryProvider, roomRepositoryProvider, bedRepositoryProvider, authRepositoryProvider);
  }

  public static SetupViewModel newInstance(HostelRepository hostelRepository,
      FloorRepository floorRepository, RoomRepository roomRepository, BedRepository bedRepository,
      AuthRepository authRepository) {
    return new SetupViewModel(hostelRepository, floorRepository, roomRepository, bedRepository, authRepository);
  }
}
