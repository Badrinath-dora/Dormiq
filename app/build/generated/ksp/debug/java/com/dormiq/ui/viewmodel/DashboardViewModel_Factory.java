package com.dormiq.ui.viewmodel;

import com.dormiq.domain.repository.AuthRepository;
import com.dormiq.domain.repository.HostelRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import io.github.jan.supabase.SupabaseClient;
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
public final class DashboardViewModel_Factory implements Factory<DashboardViewModel> {
  private final Provider<HostelRepository> hostelRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<SupabaseClient> supabaseProvider;

  public DashboardViewModel_Factory(Provider<HostelRepository> hostelRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider, Provider<SupabaseClient> supabaseProvider) {
    this.hostelRepositoryProvider = hostelRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.supabaseProvider = supabaseProvider;
  }

  @Override
  public DashboardViewModel get() {
    return newInstance(hostelRepositoryProvider.get(), authRepositoryProvider.get(), supabaseProvider.get());
  }

  public static DashboardViewModel_Factory create(
      Provider<HostelRepository> hostelRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider, Provider<SupabaseClient> supabaseProvider) {
    return new DashboardViewModel_Factory(hostelRepositoryProvider, authRepositoryProvider, supabaseProvider);
  }

  public static DashboardViewModel newInstance(HostelRepository hostelRepository,
      AuthRepository authRepository, SupabaseClient supabase) {
    return new DashboardViewModel(hostelRepository, authRepository, supabase);
  }
}
