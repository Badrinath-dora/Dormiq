package com.dormiq.ui.viewmodel;

import com.dormiq.data.manager.ActiveHostelManager;
import com.dormiq.domain.repository.AuthRepository;
import com.dormiq.domain.repository.HostelRepository;
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
public final class ManagePropertiesViewModel_Factory implements Factory<ManagePropertiesViewModel> {
  private final Provider<HostelRepository> hostelRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<ActiveHostelManager> activeHostelManagerProvider;

  public ManagePropertiesViewModel_Factory(Provider<HostelRepository> hostelRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<ActiveHostelManager> activeHostelManagerProvider) {
    this.hostelRepositoryProvider = hostelRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.activeHostelManagerProvider = activeHostelManagerProvider;
  }

  @Override
  public ManagePropertiesViewModel get() {
    return newInstance(hostelRepositoryProvider.get(), authRepositoryProvider.get(), activeHostelManagerProvider.get());
  }

  public static ManagePropertiesViewModel_Factory create(
      Provider<HostelRepository> hostelRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<ActiveHostelManager> activeHostelManagerProvider) {
    return new ManagePropertiesViewModel_Factory(hostelRepositoryProvider, authRepositoryProvider, activeHostelManagerProvider);
  }

  public static ManagePropertiesViewModel newInstance(HostelRepository hostelRepository,
      AuthRepository authRepository, ActiveHostelManager activeHostelManager) {
    return new ManagePropertiesViewModel(hostelRepository, authRepository, activeHostelManager);
  }
}
